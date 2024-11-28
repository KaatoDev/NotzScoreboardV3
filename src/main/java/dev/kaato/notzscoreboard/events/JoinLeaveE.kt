package dev.kaato.notzscoreboard.events

import dev.kaato.notzscoreboard.manager.PlayerManager.joinPlayer
import dev.kaato.notzscoreboard.manager.PlayerManager.leavePlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class JoinLeaveE : Listener {
    @EventHandler
    fun joinEvent(e: PlayerJoinEvent) {
        joinPlayer(e.player)
    }

    @EventHandler
    fun leaveEvent(e: PlayerQuitEvent) {
        leavePlayer(e.player)
    }
}