package dev.kaato.notzscoreboard.utils

import dev.kaato.notzscoreboard.NotzScoreboard.Companion.luckPerms
import dev.kaato.notzscoreboard.utils.MessageUtil.send
import io.papermc.paper.ServerBuildInfo
import net.kyori.adventure.key.Key
import net.luckperms.api.model.user.User
import org.bukkit.entity.Player


object OthersUtil {
    fun hasPermission(player: Player, permission: String): Boolean {
        return if (luckPerms != null) hasPermission(luckPerms!!.userManager.getUser(player.uniqueId)!!, permission)
        else player.hasPermission("notzscoreboard.$permission")
    }

    fun hasPermission(user: User, permission: String): Boolean {
        return user.cachedData.permissionData.checkPermission("notzscoreboard.$permission").asBoolean()
    }

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