package dev.kaato.manager

import dev.kaato.Main.Companion.plugin
import dev.kaato.Main.Companion.sf
import dev.kaato.entities.ScoreboardM
import dev.kaato.manager.DatabaseManager.loadScoreboardsDatabase
import dev.kaato.manager.PlayerManager.loadPlayers
import dev.kaato.manager.PlayerManager.updatePlayerGroup
import me.clip.placeholderapi.PlaceholderAPI
import notzapi.NotzAPI.Companion.placeholderManager
import notzapi.utils.MessageU.c
import notzapi.utils.MessageU.send
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import kotlin.random.Random

object ScoreboardManager {
    val notScoreboards = arrayOf("create", "delete", "remove", "list", "players", "reload")
    val scoreboards = hashMapOf<String, ScoreboardM>()
    private val templates = hashMapOf<String, List<String>>()
    private val staffStatus = hashMapOf<Boolean, List<String>>()
    lateinit var default_group: String
    private var priorityList = hashMapOf<String, PriorityClass>()
    data class PriorityClass(var task: BukkitTask?, var time: Long)

    fun createScoreboard(name: String, display: String): Boolean {
        return if (!scoreboards.containsKey(name)) {
            val scoreboard = ScoreboardM(name, display)
            scoreboards[name] = scoreboard
//            scoreboard.update()

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
        shutdown()
        load()
    }

    fun getTemplate(template: String, visibleGroups: List<String>? = null): List<String> {
        return if (templates.containsKey(template))
            templates[template]!!
        else if (template == "staff-status" && visibleGroups != null)
            staffStatus[checkVisibleGroups(visibleGroups)]!!
        else listOf(template)
    }

    fun getPlayerFromGroup(visibleGroups: List<String>): String {
        val playerList = scoreboards.filterKeys { visibleGroups.contains(it) }.flatMap { it.value.getPlayers() }
        return playerList[Random.nextInt(playerList.size)].name!!
    }

    private fun checkVisibleGroups(visibleGroups: List<String>): Boolean {
        return visibleGroups.any { scoreboards[it]!!.getPlayers().isNotEmpty() }
    }

    fun checkVisibleGroupsBy(scoreboard: String) {
        if (scoreboard != default_group)
            scoreboards.values.forEach { if (it.getVisibleGroups().contains(scoreboard)) it.update() }
    }

    fun load() {
        val templatesConfig = sf.config.getMapList("templates")
        default_group = sf.config.getString("default-group")
        arrayOf("low", "medium", "high").forEach { priorityList[it] = PriorityClass(null, sf.config.getLong("priority-time.$it") * 20) }

        templatesConfig.forEach { map -> map.forEach {
            val scoreLines = mutableListOf<String>()

            (it.value as List<*>).forEach { l -> scoreLines.add(l.toString()) }
            (it.value.toString())

            templates[it.key.toString()] = scoreLines
        }}

        staffStatus[true] = sf.config.getStringList("staff-status.online")
        staffStatus[false] = sf.config.getStringList("staff-status.offline")

        loadPlaceholders()
        loadScoreboards()
    }

    private fun loadPlaceholders() {
        placeholderManager.addPlaceholders(
            hashMapOf(
                "{rank}" to { p: Any? ->
                    var rank = "&7Sem rank."

                    if (p != null && Bukkit.getServer().pluginManager.getPlugin("yPlugins") != null) {
                        val player = p as Player
                        rank = PlaceholderAPI.setPlaceholders(player, "%yrankup_rank_tag%") + PlaceholderAPI.setPlaceholders(player, "%yrankup_rank_name%")

                        if (rank.contains("5")) rank = PlaceholderAPI.setPlaceholders(player, "%yrankup_rank_tag%") + "&l" + PlaceholderAPI.setPlaceholders(player, "%yrankup_rank_name%")

                        else if (!rank.contains("I")) rank = "&8[$rank&8]"
                    }
                    rank
                },

                "{status_rankup}" to { p: Any? ->
                    var status = "&7Sem status."

                    if (p != null && Bukkit.getServer().pluginManager.getPlugin("yPlugins") != null)
                        status = PlaceholderAPI.setPlaceholders(p as Player, "%yrankup_rank_tag%") + PlaceholderAPI.setPlaceholders(p, "%yrankup_progressbar%")

                    status
                },

                "{clan}" to { p: Any? ->
                    var clan = "&7Sem clan."

                    if (p != null && Bukkit.getServer().pluginManager.getPlugin("simpleclans") != null && PlaceholderAPI.setPlaceholders(p as Player, "%simpleclans_tag_label%").isNotEmpty()) {
                        val pa = PlaceholderAPI.setPlaceholders(p, "%simpleclans_tag_label%")
                        clan = c(pa.substring(4, pa.length - 5))
                    }
                    clan
                },

                "{clankdr}" to { p: Any? ->
                    var kdr = "&7Sem KDR."

                    if (p != null && Bukkit.getServer().pluginManager.getPlugin("simpleclans") != null && PlaceholderAPI.setPlaceholders(p as Player, "%simpleclans_tag_label%").isNotEmpty()) {
                        kdr = PlaceholderAPI.setPlaceholders(p, "%simpleclans_kdr%")
                        if (kdr == "0") kdr = "&fSem KDR."
                    }
                    kdr
                }
            ))
    }

    private fun checkScoreboardsPlayers(priority: Boolean?): Boolean {
        return scoreboardsFromPriority(priority).flatMap { it.getPlayers() }.isNotEmpty()
    }

    fun scoreboardsFromPriority(priority: Boolean?): List<ScoreboardM> {
        return scoreboards.filterValues { it.getPriority() == priority }.map { it.value }
    }

    fun checkScoreboardsTask(priority: Boolean?) {
        val priorityName = if (priority == null) "low" else if (!priority) "medium" else "high"

        if (priorityList[priorityName]!!.task == null && checkScoreboardsPlayers(priority)) {
            send(Bukkit.getConsoleSender(), "&eInicializando scoreboards de &fpriority ${if (priority == null) "&clow" else if (priority) "&6medium" else "&ahigh"}&e!")

            priorityList[priorityName]!!.task = object : BukkitRunnable() {
                override fun run() {
                    scoreboardsFromPriority(priority).forEach {
                        it.updatePlayers()
//                        println(it.name)
                    }
                }
            }.runTaskTimer(plugin, 0, priorityList[priorityName]!!.time)

        } else if (priorityList[priorityName]!!.task == null && !checkScoreboardsPlayers(priority)) {
            println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
            println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
            println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
            priorityList[priorityName]!!.task!!.cancel()
            priorityList[priorityName]!!.task = null
        }
    }

    fun shutdown() {
        priorityList.forEach { it.value.task?.cancel(); it.value.task = null }
        scoreboards.forEach { it.value.shutdown()}
    }

    private fun loadScoreboards() {
        val scores = loadScoreboardsDatabase()

//        println(scores)
        if (scores == null) {
            send(Bukkit.getConsoleSender(), "&cErro smanager1")
            return
        }

        if (scores.isNotEmpty())
            scores.forEach { scoreboards[it.key] = it.value }

        else createScoreboard("player", "&e&lPlayer")

        loadPlayers()
    }
}