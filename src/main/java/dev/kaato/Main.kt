package dev.kaato

import dev.kaato.commands.NScoreboardC
import dev.kaato.events.JoinLeaveE
import dev.kaato.manager.ScoreboardManager.load
import dev.kaato.manager.ScoreboardManager.shutdown
import notzapi.NotzAPI
import notzapi.NotzAPI.Companion.messageManager
import notzapi.apis.NotzYAML
import notzapi.utils.MessageU.createHover
import notzapi.utils.MessageU.createHoverURL
import notzapi.utils.MessageU.send
import notzapi.utils.MessageU.sendHoverURL
import notzapi.utils.MessageU.set
import notzapi.utils.OthersU.isAdmin
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class Main : JavaPlugin() {
    companion object {
        lateinit var pathRaw: String

        lateinit var cf: NotzYAML
        lateinit var msgf: NotzYAML
        lateinit var sf: NotzYAML

        lateinit var notzAPI: NotzAPI
    }

    override fun onEnable() {
        pathRaw = dataFolder.absolutePath
        notzAPI = NotzAPI(this)

        cf = NotzYAML("config")
        sf = NotzYAML("scoreboard")
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
                
                ${set("{prefix}")} &6Para mais plugins como este, acesse &bhttps://kaato.dev/plugins&6!!
                
            """.trimIndent())
       Bukkit.getOnlinePlayers().forEach {
           if (isAdmin(it)) {it.sendMessage(" ")
               sendHoverURL(it, set("{prefix}") + " &6Para mais plugins como este, acesse o &e&onosso site&6!", arrayOf("&b&okaato.dev/plugins"), "https://kaato.dev/plugins"); it.sendMessage(" ")} }
    }

    override fun onDisable() {
        shutdown()
    }
}
