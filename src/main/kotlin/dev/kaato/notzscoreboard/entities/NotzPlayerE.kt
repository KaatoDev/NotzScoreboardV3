package dev.kaato.notzscoreboard.entities

import dev.kaato.notzscoreboard.database.DatabaseManager.deletePlayerDB
import dev.kaato.notzscoreboard.database.DatabaseManager.getPlayerDB
import dev.kaato.notzscoreboard.database.DatabaseManager.insertPlayerDB
import dev.kaato.notzscoreboard.database.DatabaseManager.updatePlayerDB
import org.bukkit.entity.Player
import java.time.LocalDateTime
import java.util.*

class NotzPlayerE(val id: Int) {
    constructor(player: Player) : this(insertPlayerDB(player))

    val name: String
    val uuid: UUID
    private var scoreboardId: Int
    val created: LocalDateTime
    private var updated: LocalDateTime?

    init {
        val notzPlayer = getPlayerDB(id)
        name = notzPlayer.name
        uuid = notzPlayer.playerUuid
        scoreboardId = notzPlayer.scoreboardId
        created = notzPlayer.created
        updated = notzPlayer.updated
    }

    fun getScoreboardId() = scoreboardId
    fun setScoreboardId(newScoreboardId: Int) {
        scoreboardId = newScoreboardId
        save()
    }
    
    fun delete(): Boolean? {
        return deletePlayerDB(id)
    }
    
    fun save() {
        updatePlayerDB(this)
    }
}