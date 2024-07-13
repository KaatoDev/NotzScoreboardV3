package dev.kaato.entities

import dev.kaato.manager.DatabaseManager.insertScoreboardDatabase
import dev.kaato.manager.DatabaseManager.updateScoreboardDatabase
import dev.kaato.manager.PlayerManager.updatePlayerGroup
import dev.kaato.manager.ScoreboardManager.checkScoreboardsTask
import dev.kaato.manager.ScoreboardManager.checkVisibleGroupsBy
import dev.kaato.manager.ScoreboardManager.getPlayerFromGroup
import dev.kaato.manager.ScoreboardManager.getTemplate
import notzapi.NotzAPI.Companion.placeholderManager
import notzapi.utils.MessageU.c
import notzapi.utils.MessageU.set
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import java.io.Serializable
import kotlin.random.Random

class ScoreboardM(val name: String, private var display: String, var header: String, var template: String, var footer: String, var color: String, private var useStaffStatus: Boolean, private val visibleGroups: MutableList<String>, private var priority: Boolean?) {
    data class ScoreboardModel(val name: String, val display: String, val header: String, val template: String, val footer: String, val color: String, val useStaffStatus: Boolean, val visibleGroups: MutableList<String>, val priority: Boolean?) : Serializable

    constructor(name: String, display: String) : this(name, display, "", "player", "staff-status", "&e", false, mutableListOf(), null) {
        insertScoreboardDatabase(getScoreboardModel())
    }

    private var linesList = mutableListOf<String>()
    private var players = mutableListOf<Player>()

    init {
        update()
    }

    fun alterStaffStatus(alter: Boolean): Boolean {
        return if (useStaffStatus != alter) {
            useStaffStatus = alter
            true
        } else false
    }

    fun getDisplay(): String {
        return display
    }

    fun getPlayers(): MutableList<Player> {
        return players
    }

    fun getVisibleGroups(): MutableList<String> {
        return visibleGroups
    }

    fun getPriority(): Boolean? {
        return priority
    }

    fun setDisplay(newDisplay: String) {
        display = newDisplay
        updateDatabase()
    }

    fun setPriority(newPriority: Boolean?): Boolean {
        if (newPriority == priority) return false
        priority = newPriority

        updateDatabase()
        return true
    }

    fun addPlayer(player: Player): Boolean {
        return if (!players.contains(player)) {
            players.add(player)
            checkVisibleGroupsBy(name)

            updatePlayerGroup(player, name)
            checkScoreboardsTask(priority)
            updatePlayer(player)

            true
        } else false
    }

    fun addGroup(group: String): Boolean {
        return if (!visibleGroups.contains(group)) {
            visibleGroups.add(group)
            updateDatabase()
            update()

            true
        } else false
    }

    fun remPlayer(player: Player): Boolean {
        return if (players.contains(player)) {
            players.remove(player)
            checkVisibleGroupsBy(name)

            updatePlayerGroup(player, null)
            player.scoreboard = Bukkit.getScoreboardManager().newScoreboard

            checkScoreboardsTask(priority)

            true
        } else false
    }

    fun remGroup(group: String): Boolean {
        return if (visibleGroups.contains(group)) {
            visibleGroups.remove(group)
            update()
            updateDatabase()
            true
        } else false
    }

    fun update() { // create in manager
        if (linesList.isNotEmpty())
            linesList.clear()

        if (header.isNotBlank()) linesList.addAll(getTemplate(header))
        if (template.isNotBlank()) linesList.addAll(getTemplate(template))
        if (footer.isNotBlank()) linesList.addAll(getTemplate(footer, visibleGroups))

        var blanks = " "

        linesList = linesList.map {
            var l = it

            if (l.isBlank() || l == " ") {
                l += blanks
                blanks += " "
            }

            l
        }.toMutableList()

        updatePlaceholder()
        updatePlayers()
    }

    private fun updatePlaceholder() {
        val player = if (players.isNotEmpty()) players[Random.nextInt(players.size)].name!! else "&cOffline"

        placeholderManager.addPlaceholder("staff_$name", player)
        placeholderManager.addPlaceholder("${name}_list", player)
    }

    fun updatePlayers() {
        if (players.isNotEmpty())
            players.forEach(::updatePlayer)
    }

    private fun createScoreboard(player: Player) {
        val scoreboard = Bukkit.getScoreboardManager().newScoreboard
        val objective = scoreboard.registerNewObjective(name, "yummy")
        objective.displaySlot = DisplaySlot.SIDEBAR
        objective.displayName = set("{prefix}")

        linesList.forEachIndexed { i, line ->
            val r = if (line.contains("{")) 0 else if (line.contains("%")) 1 else null
            val l = c(line)
            val index = linesList.size - i

            if (r != null) {
                val team = scoreboard.registerNewTeam(name + index)

                val prefix = l.substring(0, l.indexOf(if (r == 0) "{" else "%"))
                var suffix = set(l.removePrefix(prefix), player)

//                } catch (e: Exception) {
//                    println(l.removePrefix(prefix) + " aaaaaa")
//                    println(set(l.removePrefix(prefix), player) + " bbbbbbb")
//                    ""
//                }

//                println(placeholderManager.getPlaceholders().map { "key: ${it.key} = value: ${it.value.invoke(player)}" })
//                println(set("{player_displayname}", player))

                if (suffix.contains("{staff}"))
                    suffix = suffix.replace("{staff}", getStaffLine())

                team.addEntry(prefix)
                team.suffix = suffix

                objective.getScore(prefix).score = index

//                println("${(prefix+suffix).length} ${line.length}")
//                send(Bukkit.getConsoleSender(), "create $prefix$suffix")

            } else {
//                println("-$l-")
                objective.getScore(l).score = index
            }
        }

        try {
            player.scoreboard = scoreboard
        } catch (e: Exception) {
            throw e
        }
    }

    private fun updatePlayer(player: Player) {
        if (player.scoreboard?.getObjective(name) == null)
            createScoreboard(player)
        else updateScoreboard(player)

//        println(player.scoreboard.teams.toString())
    }

    private fun updateScoreboard(player: Player) {
        linesList.forEachIndexed { i, line ->

            if (line.contains("{") || line.contains("%")) {
                val index = linesList.size - i

                val prefix = line.substring(0, line.indexOf(if (line.contains("{")) "{" else "%"))
                var suffix = set(line.removePrefix(prefix), player)

                if (suffix.contains("{staff}"))
                    suffix = suffix.replace("{staff}", getStaffLine())

                player.scoreboard.getTeam(name + index).suffix = suffix

//                send(Bukkit.getConsoleSender(), "update $prefix$suffix")
            }
        }
    }

    private fun getStaffLine(): String {
        return if (visibleGroups.isEmpty()) c("&cOffline")
        else getPlayerFromGroup(visibleGroups)
    }

    fun shutdown() {
        players.forEach { it.scoreboard = Bukkit.getScoreboardManager().newScoreboard }
    }

    private fun updateDatabase() { //create
        updateScoreboardDatabase(getScoreboardModel())
    }

    private fun getScoreboardModel(): ScoreboardModel {
        return ScoreboardModel(name, display, header, template, footer, color, useStaffStatus, visibleGroups, priority)
    }
}