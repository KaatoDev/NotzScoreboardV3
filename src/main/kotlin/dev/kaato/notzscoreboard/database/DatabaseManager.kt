package dev.kaato.notzscoreboard.database

import dev.kaato.notzapi.utils.MessageU.Companion.log
import dev.kaato.notzscoreboard.entities.*
import dev.kaato.notzscoreboard.manager.ScoreboardManager.getDefaultScoreboardId
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
//import org.json.simple.JSONArray
//import org.json.simple.parser.JSONParser
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

    fun getPlayerByUUIDDB(uuid: UUID): NotzPlayerE {
        return transaction {
            Players.selectAll().where { Players.playerUuid eq uuid }.first().let {
                NotzPlayerE(it[Players.id])
            }
        }
    }

    fun updatePlayerDB(player: NotzPlayerE) {
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

    fun loadPlayersDB(): List<NotzPlayerE> {
        return transaction { Players.select(Players.id).map { NotzPlayerE(it[Players.id]) } }
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

    fun getScoreboardDB(id: Int): ScoreboardModel {
        return transaction {
            Scoreboards.selectAll().where { Scoreboards.id eq id }.first().let {
                ScoreboardModel(
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
        val arr = JsonArray(list.map { JsonPrimitive(it) })
        return arr.toString()
    }

//    fun parseToJson(list: List<String>): String {
//        val arr = JSONArray()
//        list.forEach { arr.add(it) }
//        return arr.toJSONString()
//    }

    fun parseMaterialList(json: String): List<String> {
        return Json.decodeFromString(json)
    }

//    fun parseMaterialList(json: String): List<String> {
//        return (JSONParser().parse(json) as JSONArray).map { it as String }
//    }
}