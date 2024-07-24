package dev.kaato.manager

import dev.kaato.database.DM
import dev.kaato.entities.ScoreboardM
import dev.kaato.entities.ScoreboardM.ScoreboardModel
import org.bukkit.entity.Player
import java.io.IOException
import java.sql.SQLException

object DatabaseManager {
    private val dm = DM()

    fun insertScoreboardDatabase(scoreboard: ScoreboardModel) {
        dm.insertScoreboard(scoreboard)
    }

    fun deleteScoreboardDatabase(scoreboard: ScoreboardModel) {
        dm.deleteScoreboard(scoreboard)
    }

    fun updateScoreboardDatabase(scoreboard: ScoreboardModel) {
        dm.updateScoreboard(scoreboard)
    }

    fun loadScoreboardsDatabase(): HashMap<String, ScoreboardM>? {
        return try {
            dm.loadScoreboards()
        } catch (e: SQLException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
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