package dev.kaato.manager

import dev.kaato.database.DM
import dev.kaato.entities.ScoreboardM
import dev.kaato.entities.ScoreboardM.ScoreboardModel
import org.bukkit.entity.Player

object DatabaseManager {
    val dm = DM()

    fun insertScoreboardDatabase(scoreboard: ScoreboardModel) {
        dm.insertScoreboard(scoreboard)
    }

    fun deleteScoreboardDatabase(scoreboard: ScoreboardModel) {
        dm.deleteScoreboard(scoreboard)
    }

    fun updateScoreboardDatabase(scoreboard: ScoreboardModel) {
        dm.updateScoreboard(scoreboard)
    }

    fun loadScoreboardsDatabase(): HashMap<String, ScoreboardM> {
        return dm.loadScoreboards()
    }

    fun insertPlayerDatabase(player: Player, scoreboard: String) {
        dm.insertPlayer(player, scoreboard)
    }

    fun deletePlayerDatabase(player: Player) {
        dm.deletePlayer(player)
    }

    fun updatePlayerDatabase(player: Player, scoreboard: String) {
        dm.updatePlayer(player, scoreboard)
    }

    fun loadPlayersDatabase(): HashMap<String, String> {
        return dm.loadPlayers()
    }
}