package dev.kaato.entities

import dev.kaato.Main.Companion.sf
import dev.kaato.manager.DatabaseManager.deleteScoreboardDatabase
import dev.kaato.manager.DatabaseManager.insertScoreboardDatabase
import dev.kaato.manager.DatabaseManager.updateScoreboardDatabase
import dev.kaato.manager.ScoreboardManager.getPlayerFromGroup
import dev.kaato.manager.ScoreboardManager.getPlayersFromGroups
import dev.kaato.manager.ScoreboardManager.getTemplate
import notzapi.NotzAPI.Companion.placeholderManager
import notzapi.NotzAPI.Companion.plugin
import notzapi.utils.MessageU.c
import notzapi.utils.MessageU.set
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.DisplaySlot
import java.io.Serializable
import kotlin.random.Random

class ScoreboardM(val name: String, private var display: String, private var header: String, private var template: String, private var footer: String, private var color: String, private val visibleGroups: MutableList<String>) {
    data class ScoreboardModel(val name: String, val display: String, val header: String, val template: String, val footer: String, val color: String, val visibleGroups: MutableList<String>) : Serializable

    constructor(name: String, display: String) : this(name, display, "", "player", "staff-status", "&e", mutableListOf()) {
        insertScoreboardDatabase(toModel())
    }

    private var linesList = mutableListOf<String>()
    private var players = mutableListOf<Player>()
    private var isntDefault = true
    private var task: BukkitTask? = null

    init {
        update()
    }

// -------------------
    // getters - start

    fun getDisplay(): String {
        return display
    }

    fun getPlayers(): MutableList<Player> {
        return players
    }

    fun getVisibleGroups(): MutableList<String> {
        return visibleGroups
    }

    fun getHeader(): String {
        return header
    }

    fun getTemplate(): String {
        return template
    }

    fun getFooter(): String {
        return footer
    }

    fun getColor(): String {
        return color
    }

    fun isDefault(): Boolean {
        return !isntDefault
    }

    fun getScoreboard(player: Player) {
        scoreboardCreate(player)
    }


    // getters - end
// -------------------
    // setters - start

    fun setTemplate(header: String? = null, template: String? = null, footer: String? = null) {
        this.header = header ?: this.header
        this.template = template ?: this.template
        this.footer = footer ?: this.footer
        update()
        databaseUpdate()
    }

    fun setColor(color: String) {
        this.color = color
        update()
        databaseUpdate()
    }

    fun setDisplay(display: String) {
        this.display = display
        databaseUpdate()
    }

    fun setDefault(default: Boolean) {
        isntDefault = !default
    }

    // setters - end
// -------------------
    // adds - start

    fun addPlayer(player: Player): Boolean {
        return if (!players.contains(player)) {
            if (players.isEmpty())
                runTask()

            players.add(player)
            updatePlayer(player)

            true
        } else false
    }

    fun addGroup(group: String): Boolean {
        return if (!visibleGroups.contains(group)) {
            visibleGroups.add(group)
            databaseUpdate()
            update()

            true
        } else false
    }

    // adds - end
// -------------------
    // rems - start

    fun remPlayer(player: Player): Boolean {
        return if (players.contains(player)) {
            players.remove(player)
            player.scoreboard = Bukkit.getScoreboardManager().newScoreboard
            cancelTask()

            true
        } else false
    }

    fun remGroup(group: String): Boolean {
        return if (visibleGroups.contains(group)) {
            visibleGroups.remove(group)
            databaseUpdate()
            update()

            true
        } else false
    }

    // rems - end
// -------------------
    // updaters - start

    private fun updatePlaceholder() {
        val player = if (players.isNotEmpty()) players[Random.nextInt(players.size)].name!! else "&fOffline"

        placeholderManager.addPlaceholder("{staff_$name}", player)
        placeholderManager.addPlaceholder("{${name}_list}", players.size.toString())
    }

    fun updatePlayers() {
        if (players.isNotEmpty())
            players.forEach(::updatePlayer)
    }

    private fun updatePlayer(player: Player) {
        if (player.scoreboard?.getObjective(name) == null)
            scoreboardCreate(player)
        else scoreboardUpdate(player)

    }

    fun update() { // create in manager
        if (linesList.isNotEmpty())
            linesList.clear()

        if (header.isNotBlank()) linesList.addAll(getTemplate(header))
        if (template.isNotBlank()) linesList.addAll(getTemplate(template))
        if (footer.isNotBlank()) linesList.addAll(getTemplate(footer, visibleGroups))

        var blanks = ""

        linesList = linesList.map {
            if (it.isBlank() || it == " ") {
                blanks += " "
                it + blanks

            } else if (it[0] == '&') it
            else color + it
        }.toMutableList()

        updatePlaceholder()
        shutdown()
        updatePlayers()
    }

    // updaters - end
// -------------------
    // scoreboard - start

    private fun scoreboardCreate(player: Player) {
        val scoreboard = Bukkit.getScoreboardManager().newScoreboard
        val objective = scoreboard.registerNewObjective(name, "yummy")
        objective.displaySlot = DisplaySlot.SIDEBAR
        objective.displayName = set("{prefix}")

        linesList.forEachIndexed { i, line ->
            val r = if (line.contains("{")) 0 else if (line.contains("%")) 1 else null
            var l = c(line)
            val index = linesList.size - i -1

            if (line.contains("#"))
                l = l.replaceFirst("#", "")


            if (r != null) {
                val team = scoreboard.registerNewTeam(name + index)

                var prefix = l.substring(0, l.indexOf(if (r == 0) "{" else "%"))
                var suffix = set(l.removePrefix(prefix), player)

                if (suffix.contains("{staff}"))
                    suffix = suffix.replace("{staff}", staffLine("{staff}"))
                else if (suffix.contains("{supstaff}"))
                    suffix = suffix.replace("{supstaff}", staffLine("{supstaff}"))
                else if (suffix.contains("{staff_list}"))
                    suffix = suffix.replace("{staff_list}", staffsLine().toString())

                if (prefix.length > 30)
                    prefix = c("&cLine $index ")
                if (suffix.length > 16)
                    suffix = c("Suffix too big")

                team.addEntry(prefix)
                team.suffix = suffix

                objective.getScore(prefix).score = index

            } else if (l.length > 38)
                objective.getScore(c("&cLine $index is too big")).score = index

            else if (l.length > 2 && l[2] == '#')
                objective.getScore(l.replaceFirst("#", "")).score = index

            else objective.getScore(l).score = index
        }

        try {
            player.scoreboard = scoreboard
        } catch (e: Exception) {
            throw e
        }
    }

    private fun scoreboardUpdate(player: Player) {
        linesList.forEachIndexed { i, line ->

            if ((line.contains("{") || line.contains("%")) && !line.contains('#')) {
                val index = linesList.size - i -1

                var prefix = line.substring(0, line.indexOf(if (line.contains("{")) "{" else "%"))
                var suffix = set(line.removePrefix(prefix), player)

                if (suffix.contains("{staff}"))
                    suffix = suffix.replace("{staff}", staffLine("{staff}"))
                else if (suffix.contains("{supstaff}"))
                    suffix = suffix.replace("{supstaff}", staffLine("{supstaff}"))
                else if (suffix.contains("{staff_list}"))
                    suffix = suffix.replace("{staff_list}", staffsLine().toString())

                if (prefix.length > 30)
                    prefix = c("&cLine $index ")
                if (suffix.length > 16)
                    suffix = c("Suffix too big")

                player.scoreboard.getTeam(name + index).suffix = suffix
            }
        }
    }

    // scoreboard - end
// -------------------
    // managers - start

    private fun staffLine(placeholder: String): String {
        return if (getPlayersFromGroups(visibleGroups).isEmpty()) {
            if (placeholder == "{staff}") c("&fStaff offline")
            else c("&fSuperiores offline")
        } else getPlayerFromGroup(visibleGroups)
    }

    private fun staffsLine(): Int {
        return if (visibleGroups.isEmpty()) 0
        else getPlayersFromGroups(visibleGroups).size
    }

    private fun databaseUpdate() { //create
        updateScoreboardDatabase(toModel())
    }

    private fun toModel(): ScoreboardModel {
        return ScoreboardModel(name, display, header, template, footer, color, visibleGroups)
    }

    fun shutdown() {
        players.forEach { it.scoreboard = Bukkit.getScoreboardManager().newScoreboard }
    }

    fun delete() {
        shutdown()
        players.clear()
        cancelTask()
        deleteScoreboardDatabase(toModel())
    }

    fun contains(player: Player): Boolean {
        return players.contains(player)
    }

    // managers - end
// -------------------
    // task - start

    private fun runTask() {
        val time = (if (sf.config.contains("priority-time.$name")) sf.config.getLong("priority-time.$name") else 20) * 20

        task = object : BukkitRunnable() {
            override fun run() {
                updatePlayers()
            }
        }.runTaskTimer(plugin, 0, time)
    }

    private fun cancelTask() {
        if (players.isEmpty() && isntDefault && task != null)
            task!!.cancel()
    }

    fun pauseTask(minutes: Int = 1): Boolean {
        return if (task != null) {
            task!!.cancel()

            object : BukkitRunnable() {
                override fun run() {
                    runTask()
                }
            }.runTaskLater(plugin, minutes * 60  * 20L)

            true
        } else false
    }

    // task - end
// -------------------
}