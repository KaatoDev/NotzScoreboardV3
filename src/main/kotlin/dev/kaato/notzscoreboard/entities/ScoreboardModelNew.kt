package dev.kaato.notzscoreboard.entities

import java.time.LocalDateTime

data class ScoreboardModelNew(
    val id: Int,
    val name: String,
    val display: String,
    val header: String,
    val template: String,
    val footer: String,
    val color: String,
    val visibleGroups: List<String>,
    val created: LocalDateTime,
    val updated: LocalDateTime?
)