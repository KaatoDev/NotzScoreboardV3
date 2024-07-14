package dev.kaato.manager

import dev.kaato.Main.Companion.sf
import dev.kaato.entities.ScoreboardM
import dev.kaato.manager.DatabaseManager.loadScoreboardsDatabase
import dev.kaato.manager.PlayerManager.checkPlayer
import dev.kaato.manager.PlayerManager.initializePlayers
import dev.kaato.manager.PlayerManager.loadPlayers
import dev.kaato.manager.PlayerManager.players
import me.clip.placeholderapi.PlaceholderAPI
import notzapi.NotzAPI.Companion.placeholderManager
import notzapi.utils.MessageU.c
import notzapi.utils.MessageU.send
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
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
                send(player, "&eA &fscoreboard $display&e foi criada com &asucesso&e!")
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
            send(player, "&eVisualizando &fscoreboard ${display(scoreboard)}&e.")

        } else send(player, "&cEsta scoreboard não existe.")
    }

    fun pauseScoreboard(player: Player, scoreboard: String, minutes: Int = 1) {
        if (scoreboards.contains(scoreboard)) {
            scoreboards[scoreboard]!!.pauseTask(minutes)
            send(player, "&ePausando &fscoreboard ${display(scoreboard)}&e por &a${minutes}&f minuto${if (minutes>1) "s" else ""}&e.")

        } else send(player, "&cEsta scoreboard não existe.")
    }

    fun addPlayerTo(sender: Player, player: Player, scoreboard: String) {
        val score = scoreboards[scoreboard]!!

        if (score.addPlayer(player)) {
            send(sender, "&eA &fscoreboard ${score.getDisplay()}&e foi adicionada ao player ${player.name}&e.")

            checkPlayer(player, score)

        } else send(sender, "&cO player ${player.name}&c já possui esta scoreboard.")
    }

    fun remPlayerFrom(sender: Player, player: Player, scoreboard: String) {
        val score = scoreboards[scoreboard]!!

        if (score.remPlayer(player)) {
            send(sender, "&eA &fscoreboard ${display(scoreboard)}&e foi removida do player ${player.name}&e.")
            checkPlayer(player, isDefault = score.isDefault())

        } else send(sender, "&cO player ${player.name}&c possui a &fscoreboard ${if (players.containsKey(player.name)) players[player.name] else default_group}&c.")
    }

    fun addGroupTo(player: Player, scoreboard: String, group: String) {
        val score = scoreboards[scoreboard]!!

        if (score.addGroup(group))
            send(player, "&eO grupo ${display(group)}&e foi adicionado aos visiblegroups da &fscoreboard ${score.getDisplay()}&e.")
        else send(player, "&cO grupo ${display(group)}&c já faz parte dos visiblegroups da &fscoreboard ${score.getDisplay()}&c.")
    }

    fun remGroupFrom(player: Player, scoreboard: String, group: String) {
        val score = scoreboards[scoreboard]!!

        if (score.remGroup(group))
            send(player, "&eO grupo ${display(group)}&e foi removido dos visiblegroups da &fscoreboard ${score.getDisplay()}&e.")
        else send(player, "&cO grupo ${display(group)}&c não faz parte dos visiblegroups da &fscoreboard ${score.getDisplay()}&c.")
    }

    fun setDisplay(player: Player, scoreboard: String, display: String) {
        val score = scoreboards[scoreboard]!!
        val temp = score.getDisplay()

        if (display == temp) {
            score.setDisplay(display)
            send(player, "&eDisplay da &fscoreboard $scoreboard&e alterado de &c$temp&e para &a$display&e.")

        } else send(player, "&aEsta já é o display atual da &fscoreboard $scoreboard&c!")
    }

    fun setTemplate(scoreboard: String, header: String? = null, template: String? = null, footer: String? = null) {
        scoreboards[scoreboard]!!.setTemplate(header, template, footer)
    }

    fun setTemplate(player: Player, scoreboard: String, header: String? = null, template: String? = null, footer: String? = null) {
        val score = scoreboards[scoreboard]!!

        if (header != null) {
            if (header != score.getHeader())
                send(player, "&eA header da &fscoreboard ${score.getDisplay()}&e foi alterado de &f'&c${score.getHeader()}&f' &epara &a$header&e.")
            else send(player, "&aEsta já é a header atual da &fscoreboard ${score.getDisplay()}&c!")
        }

        if (template != null) {
            if (template != score.getTemplate())
                send(player, "&eO template da &fscoreboard ${score.getDisplay()}&e foi alterado de &f'&c${score.getTemplate()}&f' &epara &a$template&e.")
            else send(player, "&aEste já é o template atual da &fscoreboard ${score.getDisplay()}&c!")
        }

        if (footer != null) {
            if (footer != score.getFooter())
                send(player, "&eA footer da &fscoreboard ${score.getDisplay()}&e foi alterado de &f'&c${score.getHeader()}&f' &epara &a$footer&e.")
            else send(player, "&aEsta já é a footer atual da &fscoreboard ${score.getDisplay()}&c!")
        }

        if (header == null && template == null && footer == null)
            send(player, "&cVocê precisa inserir pelo menos 1 campo dos templates!")

        scoreboards[scoreboard]!!.setTemplate(header, template, footer)
    }

    fun setColor(player: Player, scoreboard: String, color: String) {
        val score = scoreboards[scoreboard]!!
        val temp = score.getColor()

        if (color != temp) {
            score.setColor(color)
            send(player, "&eA color da &fscoreboard ${display(scoreboard)}&e foi alterado de &c'$temp${temp[0]}$temp${temp[1]}&c'&e para &a'$color${color[0]}$color${color[1]}&a'&e.")
        } else send(player, "&aEsta já é a cor atual da &fscoreboard ${score.getDisplay()}&c!")

    }

    fun display(scoreboard: String): String {
        return scoreboards[scoreboard]!!.getDisplay()
    }

    // scoreboard - end
// -------------------
    // geral - start

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
        scoreboards.forEach { it.value.shutdown()}
    }

    // geral - end
// -------------------
    // loaders - start

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
        sf.config.getMapList("placeholders").flatMap { it.entries }.forEach() { placeholderManager.addPlaceholder("{${it.key.toString()}}", it.value.toString()) }

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

    private fun loadScoreboards() {
        val scores = loadScoreboardsDatabase()
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