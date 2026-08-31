package dev.kaato.notzscoreboard

import dev.kaato.notzscoreboard.apis.NotzYAML
import dev.kaato.notzscoreboard.database.DAO
import dev.kaato.notzscoreboard.manager.MainManager.shutdown
import dev.kaato.notzscoreboard.manager.MainManager.startup
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler
import net.luckperms.api.LuckPerms
import org.bukkit.plugin.java.JavaPlugin


class NotzScoreboard : JavaPlugin() {
    companion object {
        lateinit var pathRaw: String
        lateinit var prefix: String
        lateinit var globalScheduler: GlobalRegionScheduler
        var hasViaVersion: Boolean = false

        lateinit var af: NotzYAML
        lateinit var cf: NotzYAML
        lateinit var sf: NotzYAML
        lateinit var msgf: NotzYAML

        lateinit var plugin: JavaPlugin
        lateinit var dao: DAO
        var luckPerms: LuckPerms? = null
    }

    override fun onEnable() {
        pathRaw = dataFolder.absolutePath
        plugin = this
        globalScheduler = server.globalRegionScheduler


        startup()
    }

    override fun onDisable() {
        shutdown()
    }
}
