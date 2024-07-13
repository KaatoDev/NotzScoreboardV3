package dev.kaato.manager

import dev.kaato.manager.DatabaseManager.deletePlayerDatabase
import dev.kaato.manager.DatabaseManager.insertPlayerDatabase
import dev.kaato.manager.DatabaseManager.loadPlayersDatabase
import dev.kaato.manager.DatabaseManager.updatePlayerDatabase
import dev.kaato.manager.ScoreboardManager.default_group
import dev.kaato.manager.ScoreboardManager.scoreboards
import notzapi.utils.MessageU.send
import notzapi.utils.MessageU.sendHeader
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object PlayerManager {
    val players = hashMapOf<String, String>()

    fun joinPlayer(player: Player) {
        if (players.containsKey(player.name)) {
            if (scoreboards.containsKey(players[player.name]))
                scoreboards[players[player.name]]!!.addPlayer(player)

            else players.remove(player.name)

        } else if (scoreboards.containsKey(default_group))
            scoreboards[default_group]!!.addPlayer(player)

        else send(Bukkit.getConsoleSender(), "&cNão foi possível atribuir uma scoreboard ao player &f${player.name}&c. Erro: pmanager1 ")
    }

    fun leavePlayer(player: Player) {
        if (players.containsKey(player.name)) {
            if (scoreboards.containsKey(players[player.name]))
                scoreboards[players[player.name]]!!.remPlayer(player)

            else players.remove(player.name)

        } else if (scoreboards.containsKey(default_group))
            scoreboards[default_group]!!.remPlayer(player)

        else send(Bukkit.getConsoleSender(), "&cNão foi possível remover/atribuir uma scoreboard ao player &f${player.name}&c. Erro: pmanager2 ")
    }

    fun updatePlayerGroup(player: Player, scoreboard: String?): Boolean {
        if (scoreboard != null && scoreboard != default_group) {
            if (players.containsKey(player.name)) {
                scoreboards[players[player.name]]!!.remPlayer(player)
                updatePlayerDatabase(player, scoreboard)

            } else insertPlayerDatabase(player, scoreboard)

            players[player.name] = scoreboard

        } else if (players.containsKey(player.name)) {
            deletePlayerDatabase(player)
            players.remove(player.name)

        } else return false

        return true
    }

    fun loadPlayers() {
        loadPlayersDatabase().forEach { players[it.key] = it.value }

        Bukkit.getOnlinePlayers().forEach(::joinPlayer)
    }

    fun seePlayers(player: Player, scoreboard: String = "") {
        val all = scoreboard.isBlank()

        val scores = scoreboards.values.filter { (if (all) it.name != default_group else it.name == scoreboard) && it.getPlayers().isNotEmpty() }

        if (scores.isNotEmpty()) {
            val scorePlayers = mutableListOf<String>()

            scores.forEach { it.getPlayers().forEach { p -> scorePlayers.add("&f${p.name}&e: &f${it.getDisplay()}") } }

            sendHeader(player, scorePlayers.joinToString(separator = "\n", prefix = "", postfix = ""))

        } else send(player, "&cNão há players com scoreboard além da padrão.")
    }
}