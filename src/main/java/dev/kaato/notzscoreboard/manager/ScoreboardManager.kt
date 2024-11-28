package dev.kaato.notzscoreboard.manager

import dev.kaato.notzscoreboard.Main.Companion.sf
import dev.kaato.notzscoreboard.entities.ScoreboardM
import dev.kaato.notzscoreboard.manager.PlayerManager.checkPlayer
import dev.kaato.notzscoreboard.manager.PlayerManager.initializePlayers
import dev.kaato.notzscoreboard.manager.PlayerManager.loadPlayers
import dev.kaato.notzscoreboard.manager.PlayerManager.players
import me.clip.placeholderapi.PlaceholderAPI.setPlaceholders
import notzapi.NotzAPI.Companion.placeholderManager
import notzapi.NotzAPI.Companion.plugin
import notzapi.utils.MessageU.c
import notzapi.utils.MessageU.join
import notzapi.utils.MessageU.send
import notzapi.utils.MessageU.sendHeader
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import kotlin.collections.forEach
import kotlin.random.Random

object ScoreboardManager {
    val blacklist = arrayOf("create", "delete", "remove", "list", "null", "players", "reload")
    val scoreboards = hashMapOf<String, ScoreboardM>()
    private val templates = hashMapOf<String, List<String>>()
    private val staffStatus = hashMapOf<Boolean, List<String>>()
    lateinit var default_group: String
    private var priorityList = hashMapOf<String, PriorityClass>()

    data class PriorityClass(var task: BukkitTask?, var time: Long)

// -------------------
    // scoreboard - start

    fun createScoreboard(name: String, display: String, player: Player? = null): Boolean {
        return if (!scoreboards.containsKey(name)) {
            val scoreboard = ScoreboardM(name, display)
            scoreboards[name] = scoreboard

            if (name == default_group) scoreboard.setDefault(true)
            if (player != null) {
                addPlayerTo(player, player, name)
                send(player, "createScoreboard", display)
            }

            true
        } else false
    }

    fun deleteScoreboard(scoreboard: String): Boolean? {
        return if (scoreboards.contains(scoreboard)) {
            if (scoreboard != default_group) {
                val score = scoreboards[scoreboard]!!
                score.getPlayers().forEach { checkPlayer(it, isDefault = score.isDefault()) }
                score.delete()
                scoreboards.remove(scoreboard)

                true
            } else null
        } else false
    }

    fun viewScoreboard(player: Player, scoreboard: String) {
        if (scoreboards.contains(scoreboard)) {
            scoreboards[scoreboard]!!.getScoreboard(player)
            send(player, "viewScoreboard1", display(scoreboard))

        } else send(player, "viewScoreboard2")
    }

    fun pauseScoreboard(player: Player, scoreboard: String, minutes: Int = 1) {
        if (scoreboards.contains(scoreboard)) {
            scoreboards[scoreboard]!!.pauseTask(minutes)
            send(player, "pauseScoreboard1", defaults = listOf(display(scoreboard), minutes.toString(), if (minutes > 1) "s" else ""))

        } else send(player, "pauseScoreboard2")
    }

    fun addPlayerTo(sender: Player, player: Player, scoreboard: String) {
        val score = scoreboards[scoreboard]!!

        if (score.addPlayer(player)) {
            send(sender, "addPlayerTo1", defaults = listOf(score.getDisplay(), player.name))
            checkPlayer(player, score)

            if (scoreboards[default_group]!!.getVisibleGroups().contains(scoreboard))
                scoreboards[default_group]!!.update()

        } else send(sender, "addPlayerTo2", player.name)
    }

    fun remPlayerFrom(sender: Player, player: Player, scoreboard: String) {
        val score = scoreboards[scoreboard]!!

        if (score.remPlayer(player)) {
            send(sender, "remPlayerFrom1", defaults = listOf(score.getDisplay(), player.name))
            checkPlayer(player, isDefault = score.isDefault())

        } else send(sender, "remPlayerFrom2", defaults = listOf(player.name, if (players.containsKey(player.name)) players[player.name]!! else default_group))
    }

    fun addGroupTo(player: Player, scoreboard: String, group: String) {
        val score = scoreboards[scoreboard]!!

        if (score.addGroup(group))
            send(player, "addGroupTo1", defaults = listOf(display(group), score.getDisplay()))
        else send(player, "addGroupTo2", defaults = listOf(display(group), score.getDisplay()))
    }

    fun remGroupFrom(player: Player, scoreboard: String, group: String) {
        val score = scoreboards[scoreboard]!!

        if (score.remGroup(group))
            send(player, "remGroupFrom1", defaults = listOf(display(group), score.getDisplay()))
        else send(player, "remGroupFrom2", defaults = listOf(display(group), score.getDisplay()))
    }

    fun setDisplay(player: Player, scoreboard: String, display: String) {
        val score = scoreboards[scoreboard]!!
        val temp = score.getDisplay()

        if (display == temp) {
            score.setDisplay(display)
            send(player, "setDisplay1", defaults = listOf(scoreboard, temp, display))

        } else send(player, "setDisplay2", scoreboard)
    }

    fun setTemplate(scoreboard: String, header: String? = null, template: String? = null, footer: String? = null) {
        scoreboards[scoreboard]!!.setTemplate(header, template, footer)
    }

    fun setTemplate(player: Player, scoreboard: String, header: String? = null, template: String? = null, footer: String? = null) {
        val score = scoreboards[scoreboard]!!

        if (header != null) {
            if (header != score.getHeader())
                send(player, "setTemplate1", defaults = listOf("header", score.getDisplay(), score.getHeader(), header))
            else send(player, "setTemplate2", defaults = listOf("header", score.getDisplay()))
        }

        if (template != null) {
            if (template != score.getTemplate())
                send(player, "setTemplate1", defaults = listOf("template", score.getDisplay(), score.getTemplate(), template))
            else send(player, "setTemplate2", defaults = listOf("template", score.getDisplay()))
        }

        if (footer != null) {
            if (footer != score.getFooter())
                send(player, "setTemplate1", defaults = listOf("footer", score.getDisplay(), score.getFooter(), footer))
            else send(player, "setTemplate2", defaults = listOf("footer", score.getDisplay()))
        }

        if (header == null && template == null && footer == null)
            send(player, "setTemplate3")

        scoreboards[scoreboard]!!.setTemplate(header, template, footer)
    }

    fun setColor(player: Player, scoreboard: String, color: String) {
        val score = scoreboards[scoreboard]!!
        val temp = score.getColor()

        if (color != temp) {
            score.setColor(color)
            send(player, "setColor1", defaults = listOf(display(scoreboard), join(temp.map { temp + it })/*"$temp${temp[0]}$temp${temp[1]}"*/, join(color.map { color + it })/*"$color${color[0]}$color${color[1]}"*/))
        } else send(player, "setColor2", score.getDisplay())

    }

    fun display(scoreboard: String): String {
        return scoreboards[scoreboard]!!.getDisplay()
    }

    fun seeVisibleGroups(player: Player, scoreboard: String = "") {
        sendHeader(player, join(scoreboards[scoreboard]!!.getVisibleGroups(), prefix = "&e ⧽ &f$scoreboard&e: &f", separator = "&e, &f") {
            if (it == scoreboard) "&a$it" else it
        })
    }

    // scoreboard - end
// -------------------
    // geral - start

    fun reload() {
        plugin.pluginLoader.disablePlugin(plugin)
        plugin.pluginLoader.enablePlugin(plugin)
    }

    fun updateAllScoreboards(p: Player) {
        scoreboards.values.forEach { it.update() }
        send(p, "updateAllScoreboards")
    }

    fun getTemplate(template: String, visibleGroups: List<String>? = null): List<String> {
        return if (templates.containsKey(template))
            templates[template]!!
        else if (template == "staff-status" && visibleGroups != null)
            staffStatus[checkVisibleGroups(visibleGroups)]!!
        else listOf(template)
    }

    fun getPlayerFromGroup(visibleGroups: List<String>): String {
        val playerList = getPlayersFromGroups(visibleGroups)
        return playerList[Random.nextInt(playerList.size)].name!!
    }

    fun getPlayersFromGroups(visibleGroups: List<String>): List<Player> {
        return scoreboards.filterKeys { visibleGroups.contains(it) }.flatMap { it.value.getPlayers() }
    }

    private fun checkVisibleGroups(visibleGroups: List<String>): Boolean {
        return visibleGroups.any { scoreboards.containsKey(it) && scoreboards[it]!!.getPlayers().isNotEmpty() }
    }

    fun checkVisibleGroupsBy(scoreboard: String) {
        if (scoreboard != default_group)
            scoreboards.values.forEach { if (it.getVisibleGroups().contains(scoreboard)) it.update() }
    }

    fun shutdown() {
        scoreboards.values.forEach {
            it.forceCancelTask()
            it.shutdownSB()
        }
    }

    // geral - end
// -------------------
    // loaders - start

    fun load() {
        loadPlaceholders()

        val templatesConfig = sf.config.getMapList("templates")
        default_group = sf.config.getString("default-group")
        arrayOf("low", "medium", "high").forEach { priorityList[it] = PriorityClass(null, sf.config.getLong("priority-time.$it") * 20) }

        templatesConfig.forEach { map ->
            map.forEach {
                val scoreLines = mutableListOf<String>()

                (it.value as List<*>).forEach { l -> scoreLines.add(l.toString()) }
                (it.value.toString())

                templates[it.key.toString()] = scoreLines
            }
        }

        staffStatus[true] = sf.config.getStringList("staff-status.online")
        staffStatus[false] = sf.config.getStringList("staff-status.offline")

        loadScoreboards()
    }

    private fun loadPlaceholders() {
        sf.config.getMapList("placeholders").flatMap { it.entries }.forEach() { placeholderManager.addPlaceholder("{${it.key.toString()}}", it.value.toString()) }

        placeholderManager.addPlaceholders(
            hashMapOf(
                "{title}" to { sf.config.getString("title") },

                "{rank}" to { p: Any? ->
                    var rank = "&7Sem rank."

                    if (p != null && Bukkit.getServer().pluginManager.getPlugin("yPlugins") != null) {
                        val player = p as Player
                        rank = setPlaceholders(player, "%yrankup_rank_tag%") + setPlaceholders(player, "%yrankup_rank_name%")

                        if (rank.contains("5")) rank = setPlaceholders(player, "%yrankup_rank_tag%") + "&l" + setPlaceholders(player, "%yrankup_rank_name%")
                        else if (!rank.contains("I")) rank = "&8[$rank&8]"
                    }
                    rank
                },

                "{status_rankup}" to { p: Any? ->
                    var status = "&7Sem status."

                    if (p != null && Bukkit.getServer().pluginManager.getPlugin("yPlugins") != null)
                        status = setPlaceholders(p as Player, "%yrankup_rank_tag%") + setPlaceholders(p, "%yrankup_progressbar%")

                    status
                },

                "{clan}" to { p: Any? ->
                    var clan = "&7Sem clan."

                    if (p != null && Bukkit.getServer().pluginManager.getPlugin("simpleclans") != null && setPlaceholders(p as Player, "%simpleclans_clan_name%").isNotEmpty()) {
                        val pa = setPlaceholders(p, "%simpleclans_clan_name%")
                        clan = c(pa)
                    }
                    clan
                },

                "{clan_tag}" to { p: Any? ->
                    var clan = "&7Sem clan."

                    if (p != null && Bukkit.getServer().pluginManager.getPlugin("simpleclans") != null && setPlaceholders(p as Player, "%simpleclans_tag_label%").isNotEmpty()) {
                        val pa = setPlaceholders(p, "%simpleclans_tag_label%")
                        clan = c(pa.substring(4, pa.length - 5))
                    }
                    clan
                },

                "{clankdr}" to { p: Any? ->
                    var kdr = "&7Sem KDR."

                    if (p != null && Bukkit.getServer().pluginManager.getPlugin("simpleclans") != null && setPlaceholders(p as Player, "%simpleclans_tag_label%").isNotEmpty()) {
                        kdr = setPlaceholders(p, "%simpleclans_kdr%")
                        if (kdr == "0") kdr = "&fSem KDR."
                    }
                    kdr
                }
            ))
    }

    private fun loadScoreboards() {
        val scores = DatabaseManager.loadScoreboardsDatabase()
        loadPlayers()

        if (scores == null) {
            send(Bukkit.getConsoleSender(), "&cErro smanager1")
            return
        }

        if (scores.isNotEmpty())
            scores.forEach { scoreboards[it.key] = it.value }
        else createScoreboard("player", "&e&lPlayer")

        initializePlayers()
    }

    // loaders - end
// -------------------
}