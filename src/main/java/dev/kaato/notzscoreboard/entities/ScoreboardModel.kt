package dev.kaato.notzscoreboard.entities

import java.io.Serializable

data class ScoreboardModel(
    val name: String,
    val display: String,
    val header: String,
    val template: String,
    val footer: String,
    val color: String,
    val visibleGroups: MutableList<String>,
) : Serializable