package dev.kaato.notzscoreboard.utils

import dev.kaato.notzscoreboard.utils.MessageUtil.send
import io.papermc.paper.ServerBuildInfo
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player


object OthersUtil {
    fun hasPermission(player: Player, permission: String): Boolean = player.hasPermission("notzscoreboard.$permission")

    fun isAdmin(player: Player): Boolean {
        return hasPermission(player, "admin")
    }

    fun isntAdmin(player: Player): Boolean {
        val isntAdmin = !isAdmin(player)
        if (isntAdmin) send(player, "no-perm")
        return isntAdmin
    }

//    fun dump() = println(Thread.dumpStack())

    fun isFolia(): Boolean {
        return ServerBuildInfo.buildInfo().isBrandCompatible(Key.key("papermc", "folia"))
    }
}