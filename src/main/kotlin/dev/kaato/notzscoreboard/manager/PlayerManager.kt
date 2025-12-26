package dev.kaato.notzscoreboard.manager

import dev.kaato.notzapi.utils.MessageU.Companion.join
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.messageU
import dev.kaato.notzscoreboard.database.DatabaseManager.getPlayerByUUIDDB
import dev.kaato.notzscoreboard.database.DatabaseManager.loadPlayersDB
import dev.kaato.notzscoreboard.entities.NotzPlayer
import dev.kaato.notzscoreboard.entities.ScoreboardE
import dev.kaato.notzscoreboard.manager.ScoreboardManager.checkVisibleGroupsBy
import dev.kaato.notzscoreboard.manager.ScoreboardManager.containScoreboard
import dev.kaato.notzscoreboard.manager.ScoreboardManager.default_group
import dev.kaato.notzscoreboard.manager.ScoreboardManager.getScoreboardByID
import dev.kaato.notzscoreboard.manager.ScoreboardManager.scoreboards
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

object PlayerManager {
    val players = hashMapOf<UUID, NotzPlayer>()

    fun joinPlayer(player: Player) {
        if (players.containsKey(player.uniqueId)) {
            val nPlayer = fastGetPlayer(player)

            if (containScoreboard(nPlayer.getScoreboardId()))
                getScoreboardByID(nPlayer.getScoreboardId())?.addPlayer(player)

            players[player.uniqueId] = nPlayer

        } else if (scoreboards.containsKey(default_group))
            scoreboards[default_group]!!.addPlayer(player)
        else messageU.send(Bukkit.getConsoleSender(), "&cUnable to assign a scoreboard to the player &f${player.name}&c. Error: pmanager1")
    }

    fun leavePlayer(player: Player) {
        if (players.containsKey(player.uniqueId)) {
            val nPlayer = fastGetPlayer(player)

            if (containScoreboard(nPlayer.getScoreboardId()))
                getScoreboardByID(nPlayer.getScoreboardId())?.remPlayer(player)

            players.remove(nPlayer.uuid)

        } else if (scoreboards.containsKey(default_group))
            scoreboards[default_group]!!.remPlayer(player)
        else messageU.send(Bukkit.getConsoleSender(), "&cUnable to remove/assign a scoreboard to the player &f${player.name}&c. Error: pmanager2")
    }

//    fun joinPlayer(player: Player) {
//        val nPlayer = fastGetPlayer(player)
//        players[player.uniqueId] = nPlayer
//
//        if (containScoreboard(nPlayer.getScoreboardId()))
//            scoreboards.filterValues { it.id == nPlayer.getScoreboardId() }.values.let {
//                if (it.isNotEmpty()) it.first().addPlayer(player)
//            }
//        else {
//            val score = getDefaultScoreboard()
//            println(default_group)
//            score?.addPlayer(player)
//            nPlayer.setScoreboardId(score?.id ?: 0)
//        }
//    }

//    fun leavePlayer(player: Player) {
//        val nPlayer = fastGetPlayer(player)
//        if (players.containsKey(player.uniqueId))
//            players.remove(player.uniqueId)
//
//        scoreboards.filterValues { it.id == nPlayer.getScoreboardId() }.values.let {
//            if (it.isNotEmpty()) it.first().remPlayer(player)
//        }
//    }

    fun fastGetPlayer(player: Player): NotzPlayer {
        return players.getOrDefault(player.uniqueId, getPlayerByUUIDDB(player.uniqueId))
//        return players.getOrDefault(player.uniqueId, if (containPlayerDB(player.uniqueId)) getPlayerByUUIDDB(player.uniqueId) else NotzPlayer(player).let { players[player.uniqueId] = it; it })
    }


    fun checkPlayer(player: Player, scoreboard: ScoreboardE? = null, isDefault: Boolean? = null) {
        if (scoreboard != null && !scoreboard.isDefault()) {
            if (players.containsKey(player.uniqueId)) {
                val nPlayer = fastGetPlayer(player)

                nPlayer.setScoreboardId(scoreboard.id)

                if (containScoreboard(nPlayer.getScoreboardId()))
                    getScoreboardByID(nPlayer.getScoreboardId())?.remPlayer(player)

            } else {
                val nPlayer = NotzPlayer(player)
                nPlayer.setScoreboardId(scoreboard.id)
                scoreboards[default_group]!!.remPlayer(player)
            }

            checkVisibleGroupsBy(scoreboard.name)
            val nPlayer = fastGetPlayer(player)
            nPlayer.setScoreboardId(scoreboard.id)

        } else if (isDefault != null && !isDefault && players.containsKey(player.uniqueId)) {
            val nPlayer = fastGetPlayer(player)

            getScoreboardByID(nPlayer.getScoreboardId()).let {
                if (it == null) return@let
                it.remPlayer(player)
                checkVisibleGroupsBy(it.name)
            }
            players.remove(player.uniqueId)
            nPlayer.delete()
            scoreboards[default_group]!!.addPlayer(player)
        }
    }

//    fun checkPlayer(player: Player, scoreboard: ScoreboardE? = null, isDefault: Boolean? = null) {
//        val nPlayer = fastGetPlayer(player)
//
//        if (scoreboard != null && !scoreboard.isDefault()) {
//            val scoreName = scoreboard.name
//
//            if (players.containsKey(player.uniqueId)) {
//                updatePlayerDB(nPlayer)
//                scoreboards[scoreName]!!.remPlayer(player)
//
//            } else {
//                insertPlayerDB(player, scoreboard.id)
//                scoreboards[default_group]!!.remPlayer(player)
//            }
//
//            checkVisibleGroupsBy(scoreboard.name)
//            nPlayer.setScoreboardId(scoreboard.id)
//
//        } else if (isDefault != null && !isDefault && players.containsKey(player.uniqueId)) {
//            scoreboards[default_group]!!.remPlayer(player)
//            checkVisibleGroupsBy(default_group)
//            players.remove(player.uniqueId)
//            deletePlayerDB(nPlayer.id)
//            scoreboards[default_group]!!.addPlayer(player)
//        }
//    }

    fun resetPlayer(sender: Player, player: Player) {
        if (players.containsKey(player.uniqueId)) {
            checkPlayer(player, isDefault = false)
            messageU.send(sender, "resetPlayer1", player.name)

        } else messageU.send(sender, "resetPlayer2", player.name)
    }

    fun loadPlayers() {
        loadPlayersDB().forEach { players[it.uuid] = it }
    }

    fun addConvertedPlayers(cPlayers: List<NotzPlayer>): Int {
        cPlayers.forEach {
            if (!players.containsKey(it.uuid)) players[it.uuid] = it
        }

        return cPlayers.size
    }

    fun initializePlayers() {
        Bukkit.getOnlinePlayers().forEach(::joinPlayer)
    }

    fun seePlayers(player: Player, scoreboard: String = "") {
        val all = scoreboard.isBlank()

        val scores = scoreboards.values.filter { (if (all) it.name != default_group else it.name == scoreboard) && it.getPlayers().isNotEmpty() }

        if (scores.isNotEmpty()) {
            val scorePlayers = mutableListOf<String>()

            scores.forEach { it.getPlayers().forEach { p -> scorePlayers.add("&f${p.name}&e: &f${it.getDisplay()}") } }

            messageU.sendHeader(player, join(scorePlayers.toList(), separator = "\n"))

        } else messageU.send(player, "seePlayers")
    }
}