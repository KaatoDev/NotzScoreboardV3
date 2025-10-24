package dev.kaato.notzscoreboard

import dev.kaato.notzapi.NotzAPI.addPlugin
import dev.kaato.notzapi.NotzAPI.removePlugin
import dev.kaato.notzapi.apis.NotzYAML
import dev.kaato.notzapi.managers.ItemManager
import dev.kaato.notzapi.managers.MessageManager
import dev.kaato.notzapi.managers.NotzManager
import dev.kaato.notzapi.managers.PlaceholderManager
import dev.kaato.notzapi.utils.*
import dev.kaato.notzapi.utils.MessageU.Companion.sendHoverURL
import dev.kaato.notzscoreboard.commands.NScoreboardC
import dev.kaato.notzscoreboard.events.JoinLeaveE
import dev.kaato.notzscoreboard.manager.ScoreboardManager.load
import dev.kaato.notzscoreboard.manager.ScoreboardManager.shutdown
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

        lateinit var plugin: JavaPlugin
        lateinit var napi: NotzManager
        lateinit var itemManager: ItemManager
        lateinit var messageManager: MessageManager
        lateinit var placeholderManager: PlaceholderManager
        lateinit var eventU: EventU
        lateinit var mainU: MainU
        lateinit var menuU: MenuU
        lateinit var messageU: MessageU
        lateinit var othersU: OthersU
    }

    override fun onEnable() {
        pathRaw = dataFolder.absolutePath
        plugin = this
        napi = addPlugin(plugin)

        messageManager = napi.messageManager
        itemManager = napi.itemManager
        placeholderManager = napi.placeholderManager
        eventU = napi.eventU
        mainU = napi.mainU
        menuU = napi.menuU
        messageU = napi.messageU
        othersU = napi.othersU

        cf = NotzYAML(this, "config")
        sf = NotzYAML(this, "scoreboard")
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
        messageU.send(
            Bukkit.getConsoleSender(), """
                &2Inicializado com sucesso.
                &f┳┓    &2┏┓       ┓        ┓&f  &2┓┏┏┓
                &f┃┃┏┓╋┓&2┗┓┏┏┓┏┓┏┓┣┓┏┓┏┓┏┓┏┫&f━━&2┃┃ ┫
                &f┛┗┗┛┗┗&2┗┛┗┗┛┛ ┗ ┗┛┗┛┗┻┛ ┗┻&f  &2┗┛┗┛
                
                ${messageU.set("{prefix}")} &6Para mais plugins como este, acesse &bhttps://kaato.dev/plugins&6!!
                ${messageU.set("{prefix}")} &6For more plugins like this, visit &bhttps://kaato.dev/plugins&6!!
                
            """.trimIndent()
        )
        Bukkit.getOnlinePlayers().forEach {
            if (othersU.isAdmin(it)) {
                it.sendMessage(" ")
                sendHoverURL(it, messageU.set("{prefix}") + " &6Para mais plugins como este, acesse o &e&onosso site&6!", arrayOf("&b&okaato.dev/plugins"), "https://kaato.dev/plugins"); it.sendMessage(" ")
                sendHoverURL(it, messageU.set("{prefix}") + " &6For more plugins like this, visit &e&oour website&6!", arrayOf("&b&okaato.dev/plugins"), "https://kaato.dev/plugins"); it.sendMessage(" ")
            }
        }
    }

    override fun onDisable() {
        removePlugin(plugin)
        shutdown()
    }
}
