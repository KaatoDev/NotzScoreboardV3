package dev.kaato.entities

import dev.kaato.Main.Companion.cf
import dev.kaato.manager.DatabaseManager.insertScoreboardDatabase
import dev.kaato.manager.DatabaseManager.updateScoreboardDatabase
import dev.kaato.manager.PlayerManager.updatePlayerGroup
import dev.kaato.manager.ScoreboardManager.checkScoreboardsTask
import dev.kaato.manager.ScoreboardManager.getPlayerFromGroup
import dev.kaato.manager.ScoreboardManager.getTemplate
import notzapi.NotzAPI.Companion.placeholderManager
import notzapi.utils.MessageU.c
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import java.io.Serializable
import kotlin.random.Random

class ScoreboardM(val name: String, private var display: String, var header: String, var template: String, var footer: String, var color: String, private var useStaffStatus: Boolean, private val visibleGroups: MutableList<String>, private var priority: Boolean?) {
    data class ScoreboardModel(val name: String, val display: String, val header: String, val template: String, val footer: String, val color: String, val useStaffStatus: Boolean, val visibleGroups: MutableList<String>, val priority: Boolean?) : Serializable

    constructor(name: String, display: String) : this(name, display, "", "player", "staff-status", "&e", false, mutableListOf(), null) {
        objective.displaySlot = DisplaySlot.SIDEBAR
        objective.displayName = c(cf.config.getString("title"))
        insertScoreboardDatabase(getScoreboardModel())
    }

    init {
        update()
    }

    private val scoreboard = Bukkit.getScoreboardManager().newScoreboard
    private val objective = scoreboard.registerNewObjective(name, "yummy")
    private var linesList = mutableListOf<String>()
    private val players = mutableListOf<Player>()

    fun alterStaffStatus(alter: Boolean): Boolean {
        return if (useStaffStatus != alter) {
            useStaffStatus = alter
            true
        } else false
    }

    fun getDisplay(): String {
        return display
    }

    fun getPlayers(): MutableList<Player> { // /nsb <scoreboard> players
        return players
    }

    fun getVisibleGroups(): MutableList<String> { // /nsb <scoreboard> visiblegroups
        return visibleGroups
    }

    fun getPriority(): Boolean? { // /nsb <scoreboard> visiblegroups
        return priority
    }

    fun setDisplay(newDisplay: String) { // /nsb <scoreboard> setdisplay <display>
        display = newDisplay
        updateDatabase()
    }

    fun setPriority(newPriority: Boolean?): Boolean { // /nsb <scoreboard> setdisplay <display>
        if (newPriority == priority) return false
        priority = newPriority

        updateDatabase()
        return true
    }

    fun addPlayer(player: Player): Boolean { // /nsb <scoreboard> addplayer <player>
        return if (!players.contains(player)) {
            players.add(player)
            updatePlayerGroup(player, name)
            checkScoreboardsTask(priority)

            updatePlayer(player)

            true
        } else false
    }

    fun addGroup(group: String): Boolean { // /nsb <scoreboard> addgroup <player>
        return if (!visibleGroups.contains(group)) {
            visibleGroups.add(group)
            updateDatabase()

            true
        } else false
    }

    fun remPlayer(player: Player): Boolean { // /nsb <scoreboard> remplayer <player>
        return if (players.contains(player)) {
            players.remove(player)
            updatePlayerGroup(player, null)
            checkScoreboardsTask(priority)

            true
        } else false
    }

    fun remGroup(group: String): Boolean { // /nsb <scoreboard> remgroup <player>
        return if (visibleGroups.contains(group)) {
            visibleGroups.remove(group)
            updateDatabase()
            true
        } else false
    }

    fun update() { // create in manager
        linesList = mutableListOf<String>()

        if (header.isNotBlank()) linesList.addAll(getTemplate(header))
        if (template.isNotBlank()) linesList.addAll(getTemplate(template))
        if (footer.isNotBlank()) linesList.addAll(getTemplate(footer))

        updatePlaceholder()
        updatePlayers()
    }

    fun updatePlaceholder() {
        if (players != null) {
            val player = if (players.isNotEmpty()) players[Random.nextInt(players.size)].name!! else "&cOffline"

            placeholderManager.addPlaceholder("staff_$name", player)
            placeholderManager.addPlaceholder("${name}_list", player)
        }
    }

    fun updatePlayers() {
        if (players.isNotEmpty())
            players.forEach(::updatePlayer)
    }

    private fun updatePlayer(player: Player) {
        if (player.scoreboard?.getObjective(name) != null)
            updateScoreboard(player)
        else createScoreboard(player)
    }

    private fun updateScoreboard(player: Player) {
        linesList.forEachIndexed { i, line ->
            val index = linesList.size - i  - 1

            if (line.contains("{") || line.contains("%")) {
                val l = c(if (line.contains("{staff}")) line.replace("{staff}", getStaffLine()) else line)

                player.scoreboard.getTeam(name + index).suffix = placeholderManager.set(player, l)
            }
        }
    }

    private fun createScoreboard(player: Player) {
        linesList.forEach(::setLines)
    }

    private fun getStaffLine(): String {
        return if (visibleGroups.isEmpty()) "&cnone"
        else getPlayerFromGroup(visibleGroups)
    }

    private fun setLines(line: String) {
        val r = if (line.contains("{")) 0 else if (line.contains("%")) 1 else null
        val l = c(line)
        val index = linesList.size - linesList.indexOf(line) -1

        if (r != null) {
            val team = scoreboard.registerNewTeam(name + index)

            val prefix = l.substring(0, l.indexOf(if (r==0) "{" else "%"))
            val suffix = placeholderManager.set(l.removePrefix(prefix))

            team.addEntry(prefix)
            team.suffix = suffix
            objective.getScore(prefix).score = index

        } else objective.getScore(l).score = index
    }

    fun updateDatabase() { //create
        updateScoreboardDatabase(getScoreboardModel())
    }

    private fun getScoreboardModel(): ScoreboardModel {
        return ScoreboardModel(name, display, header, template, footer, color, useStaffStatus, visibleGroups, priority)
    }
}