package dev.kaato.manager

import dev.kaato.Main.Companion.plugin
import dev.kaato.Main.Companion.sf
import dev.kaato.entities.ScoreboardM
import dev.kaato.manager.DatabaseManager.loadScoreboardsDatabase
import dev.kaato.manager.PlayerManager.loadPlayers
import dev.kaato.manager.PlayerManager.updatePlayerGroup
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import kotlin.random.Random

object ScoreboardManager {
    val templates = hashMapOf<String, List<String>>()
    val scoreboards = hashMapOf<String, ScoreboardM>()
    lateinit var staffStatus: Array<String>
    lateinit var default_group: String
    private var isStaffStatus = true
    private var priorityList = hashMapOf<String, priorityClass>()
    data class priorityClass(var task: BukkitTask?, var time: Long)

    fun createScoreboard(name: String, display: String): Boolean {
        return if (!scoreboards.containsKey(name)) {
            val scoreboard = ScoreboardM(name, display)
            scoreboards[name] = scoreboard

            true
        } else false
    }

    fun deleteScoreboard(scoreboard: String): Boolean? {
        return if (scoreboards.contains(scoreboard)) {
            if (scoreboard != default_group) {
                val score = scoreboards[scoreboard]!!
                score.getPlayers().toList().forEach { updatePlayerGroup(it, null) }
                checkScoreboardsTask(score.getPriority())

                true
            } else null
        } else false
    }

    fun update() {
        load()
    }

    fun getTemplate(template: String): List<String> {
        return if (templates.containsKey(template))
            templates[template]!!
        else listOf(template)
    }

    fun getPlayerFromGroup(visibleGroups: List<String>): String {
        val playerList = scoreboards.filterKeys { visibleGroups.contains(it) }.flatMap { it.value.getPlayers() }
        return playerList[Random.nextInt(playerList.size)].name!!
    }

    fun load() {
        val templatesConfig = sf.config.getMapList("templates")
        default_group = sf.config.getString("default-group")
        arrayOf("low", "medium", "high").forEach { if (priorityList[it] != null) priorityList[it]!!.time = sf.config.getLong("priority-time.$it") * 20 }

        templatesConfig.forEach { map -> map.forEach {
            val scoreLines = mutableListOf<String>()
            var blanks = " "

            (it.value as List<*>).forEach { line ->
                var l = line.toString()

                if (l.isBlank()) l += blanks
                blanks += " "

                scoreLines.add(l)
            }

            templates[it.key.toString()] = scoreLines
        }}

        staffStatus = sf.config.getStringList("staff-status").toTypedArray()
        isStaffStatus = sf.config.getBoolean("staff-status-enabled")

        loadScoreboards()
    }

    private fun checkScoreboardsPlayers(priority: Boolean?): Boolean {
        return scoreboardsFromPriority(priority).flatMap { it.getPlayers() }.isNotEmpty()
    }

    fun scoreboardsFromPriority(priority: Boolean?): List<ScoreboardM> {
        return scoreboards.filterValues { it.getPriority() == priority }.map { it.value }
    }

    fun checkScoreboardsTask(priority: Boolean?) {
        val priorityName = if (priority == null) "low" else if (!priority) "medium" else "high"

        if (priorityList[priorityName]!!.task == null && checkScoreboardsPlayers(priority))
            priorityList[priorityName]!!.task = object : BukkitRunnable() {
                override fun run() {
                    scoreboardsFromPriority(priority).forEach { it.updatePlayers() }
                }
            }.runTaskTimer(plugin, 0, priorityList[priorityName]!!.time)

        else if (priorityList[priorityName]!!.task == null && !checkScoreboardsPlayers(priority)) {
            priorityList[priorityName]!!.task!!.cancel()
            priorityList[priorityName]!!.task = null
        }
    }

    fun shutdown() {
        priorityList.forEach { it.value.task!!.cancel(); it.value.task == null }
    }

    fun loadScoreboards() {
        val scores = loadScoreboardsDatabase()

        if (scores.isNotEmpty())
            scores.forEach { scoreboards[it.key] = it.value }

        else createScoreboard("player", "&e&lPlayer")

        loadPlayers()
    }
}