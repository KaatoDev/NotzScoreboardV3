package dev.kaato.notzscoreboard.manager

import com.viaversion.viaversion.api.Via
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.af
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.cf
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.dao
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.globalScheduler
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.hasViaVersion
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.luckPerms
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.msgf
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.plugin
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.prefix
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.sf
import dev.kaato.notzscoreboard.apis.NotzYAML
import dev.kaato.notzscoreboard.commands.NScoreboardC
import dev.kaato.notzscoreboard.database.DAO
import dev.kaato.notzscoreboard.events.JoinLeaveE
import dev.kaato.notzscoreboard.manager.AnimationManager.loadAnimations
import dev.kaato.notzscoreboard.manager.ScoreboardManager.loadScoreboardManager
import dev.kaato.notzscoreboard.manager.ScoreboardManager.shutdownScoreboard
import dev.kaato.notzscoreboard.utils.MessageUtil.letters
import dev.kaato.notzscoreboard.utils.MessageUtil.log
import dev.kaato.notzscoreboard.utils.MessageUtil.sendAdmin
import dev.kaato.notzscoreboard.utils.MessageUtil.set
import net.luckperms.api.LuckPerms
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.event.HandlerList
import kotlin.system.measureTimeMillis


object MainManager {
    fun shutdown() {
        shutdownScoreboard()
//        Bukkit.getScheduler().cancelTasks(plugin)
        globalScheduler.cancelTasks(plugin)
        HandlerList.unregisterAll(plugin)
        dao.close()
    }

    fun startup() {
        val load = measureTimeMillis {
            startConfig()

            if (getPluginManager().getPlugin("ViaVersion") != null) try {
                hasViaVersion = Via.getManager().isInitialized
            } catch (_: IllegalArgumentException) {
                log("ViaVersion detected, but not responding correctly. Try updating ViaVersion.")
            }
            val provider = Bukkit.getServicesManager().getRegistration<LuckPerms?>(LuckPerms::class.java)
            if (provider != null) {
                luckPerms = provider.getProvider()
            }

            try {
                dao = DAO()
                dao.init()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        globalScheduler.runDelayed(plugin, {
            loadAnimations()
            loadScoreboardManager()
            start()
            sendAdmin("&2NotzScoreboard &ainitialized! (${load / 1000.0}s)")
        }, 5 * 20L)
    }

    private fun start() {
        plugin.getCommand("nscoreboard")?.setExecutor(NScoreboardC())
        plugin.getCommand("nscoreboard")?.tabCompleter = NScoreboardC()
        getPluginManager().registerEvents(JoinLeaveE(), plugin)
        letters()
        bStats()
    }

    private fun startConfig() {
        cf = NotzYAML("config")
        af = NotzYAML("animations")
        sf = NotzYAML("scoreboard")
        msgf = NotzYAML("messages")
        prefix = set("{prefix}")
    }

    fun reloadConfig() {
        shutdownScoreboard()
        globalScheduler.cancelTasks(plugin)
        startConfig()
        loadAnimations()
        loadScoreboardManager()
    }

    private fun bStats() {
        val pluginId = 28538
        if (cf.config.getBoolean("bStats")) Metrics(plugin, pluginId)
    }
}