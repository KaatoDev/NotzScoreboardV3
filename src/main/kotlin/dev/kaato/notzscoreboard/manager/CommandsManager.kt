package dev.kaato.notzscoreboard.manager

import dev.kaato.notzscoreboard.NotzScoreboard.Companion.messageU
import dev.kaato.notzscoreboard.database.DatabaseManager.checkOldDatabase
import dev.kaato.notzscoreboard.database.DatabaseManager.convertPlayersDatabase
import dev.kaato.notzscoreboard.database.DatabaseManager.convertScoreboardsDatabase
import dev.kaato.notzscoreboard.database.DatabaseManager.eraseOldDatabase
import dev.kaato.notzscoreboard.manager.PlayerManager.addConvertedPlayers
import dev.kaato.notzscoreboard.manager.ScoreboardManager.addConvertedScoreboards
import org.bukkit.entity.Player

object CommandsManager {
    fun convertDatabaseCMD(player: Player, scoreboard: String) {
        if (checkOldDatabase()) {
            val pls = addConvertedPlayers(convertPlayersDatabase())
            val sbs = addConvertedScoreboards(convertScoreboardsDatabase(scoreboard))
            messageU.send(player, messageU.set("Converted a total of {default0} players and {default1} Scoreboards successfully!", defaults = listOf(pls.toString(), sbs.toString())))
        } else messageU.send(player, "There is no database to be deleted.")
    }

    fun removeOldDatabase(player: Player) {
        if (checkOldDatabase()) {
            eraseOldDatabase()
            messageU.send(player, "The old database was successfully deleted.!")
        } else messageU.send(player, "There is no old database to be deleted.")
    }

}