package dev.kaato.manager

import dev.kaato.entities.ScoreboardM
import dev.kaato.manager.DatabaseManager.deletePlayerDatabase
import dev.kaato.manager.DatabaseManager.insertPlayerDatabase
import dev.kaato.manager.DatabaseManager.loadPlayersDatabase
import dev.kaato.manager.DatabaseManager.updatePlayerDatabase
import dev.kaato.manager.ScoreboardManager.checkVisibleGroupsBy
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

    fun checkPlayer(player: Player, scoreboard: ScoreboardM? = null, isDefault: Boolean? = null) {
        if (scoreboard != null && !scoreboard.isDefault()) {
            if (players.containsKey(player.name)) {
                updatePlayerDatabase(player, scoreboard.name)
                scoreboards[players[player.name]]!!.remPlayer(player)

            } else {
                insertPlayerDatabase(player, scoreboard.name)
                scoreboards[default_group]!!.remPlayer(player)
            }

            checkVisibleGroupsBy(scoreboard.name)
            players[player.name] = scoreboard.name

        } else if (isDefault != null && !isDefault && players.containsKey(player.name)) {
            scoreboards[players[player.name]]!!.remPlayer(player)
            checkVisibleGroupsBy(players[player.name]!!)
            players.remove(player.name)
            deletePlayerDatabase(player)
            scoreboards[default_group]!!.addPlayer(player)
        }
    }

    fun resetPlayer(sender: Player, player: Player) {
        if (players.containsKey(player.name)) {
            checkPlayer(player, isDefault = false)
            send(sender, "&eA scoreboard do &fplayer ${player.name}&e foi resetada para a padrão.")

        } else send(sender, "&cO &fplayer ${player.name}&c já está na scoreboard padrão!")
    }

    fun loadPlayers() {
        loadPlayersDatabase().forEach { players[it.key] = it.value }
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

            sendHeader(player, scorePlayers.joinToString(separator = "\n", prefix = "", postfix = ""))

        } else send(player, "&cNão há players com scoreboard além da padrão.")
    }
}