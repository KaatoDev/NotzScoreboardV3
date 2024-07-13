package dev.kaato

import dev.kaato.commands.NScoreboardC
import dev.kaato.commands.NTestC
import dev.kaato.events.JoinLeaveE
import dev.kaato.manager.ScoreboardManager.load
import dev.kaato.manager.ScoreboardManager.shutdown
import notzapi.NotzAPI
import notzapi.NotzAPI.Companion.messageManager
import notzapi.apis.NotzYAML
import notzapi.utils.MessageU.send
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class Main : JavaPlugin() {
    companion object {
        lateinit var plugin: JavaPlugin
        lateinit var pathRaw: String

        lateinit var cf: NotzYAML
        lateinit var msgf: NotzYAML
        lateinit var sf: NotzYAML

        lateinit var notzAPI: NotzAPI
    }

    override fun onEnable() {
        plugin = this
        pathRaw = dataFolder.absolutePath

        cf = NotzYAML(plugin, "config")
        sf = NotzYAML(plugin, "scoreboard")

        notzAPI = NotzAPI(plugin)
        msgf = messageManager.messageFile

        object : BukkitRunnable() {
            override fun run() {
                load()
                start()
            }
        }.runTaskLater(this, 4 * 20L)
    }

    private fun start() {
        regCommands()
        regEvents()
        regTabs()

        letters()
    }

    private fun regCommands() {
        getCommand("nscoreboard").executor = NScoreboardC()
        getCommand("nt").executor = NTestC()
    }

    private fun regEvents() {
        getPluginManager().registerEvents(JoinLeaveE(), this)
    }

    private fun regTabs() {
        getCommand("nscoreboard").tabCompleter = NScoreboardC()
    }

    private fun letters() {
        send(Bukkit.getConsoleSender(),
            """
                &2Inicializado com sucesso.
                &f┳┓    &2┏┓       ┓        ┓&f  &2┓┏┏┓
                &f┃┃┏┓╋┓&2┗┓┏┏┓┏┓┏┓┣┓┏┓┏┓┏┓┏┫&f━━&2┃┃┏┛
                &f┛┗┗┛┗┗&2┗┛┗┗┛┛ ┗ ┗┛┗┛┗┻┛ ┗┻&f  &2┗┛┗━
            """.trimIndent())
    }

    override fun onDisable() {
        shutdown()
    }
}
