package dev.kaato.notzscoreboard.database

import dev.kaato.notzapi.utils.MessageU.Companion.log
import dev.kaato.notzscoreboard.converter.DatabaseManagerConverter
import dev.kaato.notzscoreboard.converter.DatabaseManagerConverter.closeConverterDB
import dev.kaato.notzscoreboard.converter.DatabaseManagerConverter.dropTablesConverterDB
import dev.kaato.notzscoreboard.converter.DatabaseManagerConverter.hasTablesConverterDB
import dev.kaato.notzscoreboard.entities.*
import dev.kaato.notzscoreboard.manager.PlayerManager.players
import dev.kaato.notzscoreboard.manager.ScoreboardManager.getDefaultScoreboardId
import dev.kaato.notzscoreboard.manager.ScoreboardManager.getScoreboard
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.json.simple.JSONArray
import org.json.simple.parser.JSONParser
import java.time.LocalDateTime
import java.util.*

object DatabaseManager {
    fun insertPlayerDB(player: Player, scoreboard: Int? = null): Int {
        val id = transaction {
            Players.insert {
                it[this.name] = player.name
                it[this.playerUuid] = player.uniqueId
                it[this.scoreboardId] = scoreboard ?: getDefaultScoreboardId()
            } get Players.id
        }
        return id
    }

    fun insertPlayerDB(playerName: String, playerUuid: UUID, scoreboard: Int? = null): Int {
        val id = transaction {
            Players.insert {
                it[this.name] = playerName
                it[this.playerUuid] = playerUuid
                it[this.scoreboardId] = scoreboard ?: getDefaultScoreboardId()
            } get Players.id
        }
        return id
    }

    fun getPlayerDB(id: Int): NotzPlayerModel {
        return transaction {
            Players.selectAll().where { Players.id eq id }.first().let {
                NotzPlayerModel(
                    it[Players.id],
                    it[Players.name],
                    it[Players.playerUuid],
                    it[Players.scoreboardId],
                    it[Players.created],
                    it[Players.updated]
                )
            }
        }
    }

    fun getPlayerByUUIDDB(uuid: UUID): NotzPlayer {
        return transaction {
            Players.selectAll().where { Players.playerUuid eq uuid }.first().let {
                NotzPlayer(it[Players.id])
            }
        }
    }

    fun updatePlayerDB(player: NotzPlayer) {
        transaction {
            Players.update({ Players.id eq player.id }) {
                it[name] = player.name
                it[playerUuid] = player.uuid
                it[scoreboardId] = player.getScoreboardId()
                it[updated] = LocalDateTime.now()
            }
        }
    }

    fun deletePlayerDB(id: Int): Boolean? {
        val res = transaction {
            Players.deleteWhere { Players.id eq id }
        }
        return when (res) {
            0 -> false
            1 -> true
            else -> {
                log("More Players were deleted!!!!! - $res")
                null
            }
        }
    }

    fun loadPlayersDB(): List<NotzPlayer> {
        return transaction { Players.select(Players.id).map { NotzPlayer(it[Players.id]) } }
    }

    fun containPlayerDB(uuid: UUID): Boolean {
        return transaction {
            Players.selectAll().where { Players.playerUuid eq uuid }.toList().isNotEmpty()
        }
    }

    fun containPlayersDB(): Boolean {
        return transaction {
            Players.selectAll().toList().isNotEmpty()
        }
    }


    fun insertScoreboardDB(name: String, display: String, color: String, header: String, template: String, footer: String, visibleGroups: List<String>): Int {
        val id = transaction {
            Scoreboards.insert {
                it[this.name] = name
                it[this.display] = display
                it[this.color] = color
                it[this.header] = header
                it[this.template] = template
                it[this.footer] = footer
                it[this.visibleGroups] = parseToJson(visibleGroups)
            } get Scoreboards.id
        }
        return id
    }

    fun insertScoreboardConvertedDB(scoreboard: ScoreboardModel): Int {
        val id = transaction {
            Scoreboards.insert {
                it[this.name] = scoreboard.name
                it[this.display] = scoreboard.display
                it[this.color] = scoreboard.color
                it[this.header] = scoreboard.header
                it[this.template] = scoreboard.template
                it[this.footer] = scoreboard.footer
                it[this.visibleGroups] = parseToJson(scoreboard.visibleGroups)
            } get Scoreboards.id
        }
        return id
    }

    fun getScoreboardDB(id: Int): ScoreboardModelNew {
        return transaction {
            Scoreboards.selectAll().where { Scoreboards.id eq id }.first().let {
                ScoreboardModelNew(
                    it[Scoreboards.id],
                    it[Scoreboards.name],
                    it[Scoreboards.display],
                    it[Scoreboards.header],
                    it[Scoreboards.template],
                    it[Scoreboards.footer],
                    it[Scoreboards.color],
                    parseMaterialList(it[Scoreboards.visibleGroups]),
                    it[Scoreboards.created],
                    it[Scoreboards.updated]
                )
            }
        }
    }

    fun updateScoreboardDB(scoreboard: ScoreboardE) {
        transaction {
            Scoreboards.update({ Scoreboards.id eq scoreboard.id }) {
                it[display] = scoreboard.getDisplay()
                it[header] = scoreboard.getHeader()
                it[template] = scoreboard.getTemplate()
                it[footer] = scoreboard.getFooter()
                it[color] = scoreboard.getColor()
                it[visibleGroups] = parseToJson(scoreboard.getVisibleGroups())
                it[updated] = LocalDateTime.now()
            }
        }
    }

    fun updateScoreboardConverterDB(scoreboard: ScoreboardModel) {
        transaction {
            Scoreboards.update({ Scoreboards.name eq scoreboard.name }) {
                it[name] = scoreboard.name
                it[display] = scoreboard.display
                it[header] = scoreboard.header
                it[template] = scoreboard.template
                it[footer] = scoreboard.footer
                it[color] = scoreboard.color
                it[visibleGroups] = parseToJson(scoreboard.visibleGroups)
                it[updated] = LocalDateTime.now()
            }
        }
    }

    fun deleteScoreboardDB(id: Int): Boolean? {
        val res = transaction {
            Scoreboards.deleteWhere { Scoreboards.id eq id }
        }
        return when (res) {
            0 -> false
            1 -> true
            else -> {
                log("More Scoreboards were deleted!!!!! - $res")
                null
            }
        }
    }

    fun loadScoreboardsDB(): List<ScoreboardE> {
        return transaction { Scoreboards.select(Scoreboards.id).map { ScoreboardE(it[Scoreboards.id]) } }
    }

    fun containScoreboardDB(name: String): Boolean {
        return transaction {
            Scoreboards.selectAll().where { Scoreboards.name eq name }.toList().isNotEmpty()
        }
    }

    fun containScoreboardsDB(): Boolean {
        return transaction {
            Scoreboards.selectAll().toList().isNotEmpty()
        }
    }


    fun parseToJson(list: List<String>): String {
        val arr = JSONArray()
        list.forEach { arr.add(it) }
        return arr.toJSONString()
    }

    fun parseMaterialList(json: String): List<String> {
        return (JSONParser().parse(json) as JSONArray).map { it as String }
    }


    fun checkOldDatabase() = hasTablesConverterDB()

    fun eraseOldDatabase() {
        dropTablesConverterDB()
        closeConverterDB()
    }

    fun convertPlayersDatabase(): List<NotzPlayer> {
        val pls = DatabaseManagerConverter.loadPlayersDatabase()
        val plIds = mutableListOf<Int>()

        pls.forEach {
            val player = Bukkit.getOfflinePlayer(it.key) ?: return@forEach
            if (containPlayerDB(player.uniqueId))
                (players[player.uniqueId] ?: getPlayerByUUIDDB(player.uniqueId)).let { notzPlayer ->
                    val score = getScoreboard(it.value)
                    if (score != null) {
                        notzPlayer.setScoreboardId(score.id)
                        updatePlayerDB(notzPlayer)
                    } else deletePlayerDB(notzPlayer.id)
                }
            else plIds.add(insertPlayerDB(player.name, player.uniqueId))
        }

        return plIds.map(::NotzPlayer)
    }

    fun convertScoreboardsDatabase(onlyScoreboard: String): List<ScoreboardE> {
        val sbs = DatabaseManagerConverter.loadScoreboardsDatabase()
        val sbIds = mutableListOf<Int>()

        sbs?.values?.forEach {
            if (onlyScoreboard != "all" && onlyScoreboard.isNotEmpty() && it.name != onlyScoreboard) return@forEach

            if (containScoreboardDB(it.name))
                updateScoreboardConverterDB(it)
            else sbIds.add(insertScoreboardConvertedDB(it))
        }

        return sbIds.map(::ScoreboardE)
    }
}