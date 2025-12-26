package dev.kaato.notzscoreboard.entities

import java.time.LocalDateTime
import java.util.*

data class NotzPlayerModel(
    val id: Int,
    val name: String,
    val playerUuid: UUID,
    val scoreboardId: Int,
    val created: LocalDateTime,
    val updated: LocalDateTime?
) 