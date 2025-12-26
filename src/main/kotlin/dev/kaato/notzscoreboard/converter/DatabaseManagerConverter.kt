package dev.kaato.notzscoreboard.converter

import dev.kaato.notzscoreboard.entities.ScoreboardModel
import java.io.IOException
import java.sql.SQLException

object DatabaseManagerConverter {
    private val dm = DMConverter()

    fun loadScoreboardsDatabase(): HashMap<String, ScoreboardModel>? {
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

    fun loadPlayersDatabase(): HashMap<String, String> {
        return dm.loadPlayers()
    }

    fun hasTablesConverterDB() = dm.hasTables()
    fun dropTablesConverterDB() = dm.dropTables()
    fun closeConverterDB() = dm.close()
}