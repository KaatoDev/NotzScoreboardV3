package dev.kaato.notzscoreboard.commands

import dev.kaato.notzapi.utils.MessageU.Companion.join
import dev.kaato.notzscoreboard.Main.Companion.messageU
import dev.kaato.notzscoreboard.Main.Companion.othersU
import dev.kaato.notzscoreboard.manager.PlayerManager.resetPlayer
import dev.kaato.notzscoreboard.manager.PlayerManager.seePlayers
import dev.kaato.notzscoreboard.manager.ScoreboardManager.addGroupTo
import dev.kaato.notzscoreboard.manager.ScoreboardManager.addPlayerTo
import dev.kaato.notzscoreboard.manager.ScoreboardManager.blacklist
import dev.kaato.notzscoreboard.manager.ScoreboardManager.createScoreboard
import dev.kaato.notzscoreboard.manager.ScoreboardManager.deleteScoreboard
import dev.kaato.notzscoreboard.manager.ScoreboardManager.display
import dev.kaato.notzscoreboard.manager.ScoreboardManager.pauseScoreboard
import dev.kaato.notzscoreboard.manager.ScoreboardManager.reload
import dev.kaato.notzscoreboard.manager.ScoreboardManager.remGroupFrom
import dev.kaato.notzscoreboard.manager.ScoreboardManager.remPlayerFrom
import dev.kaato.notzscoreboard.manager.ScoreboardManager.scoreboards
import dev.kaato.notzscoreboard.manager.ScoreboardManager.seeVisibleGroups
import dev.kaato.notzscoreboard.manager.ScoreboardManager.setColor
import dev.kaato.notzscoreboard.manager.ScoreboardManager.setDisplay
import dev.kaato.notzscoreboard.manager.ScoreboardManager.setTemplate
import dev.kaato.notzscoreboard.manager.ScoreboardManager.updateAllScoreboards
import dev.kaato.notzscoreboard.manager.ScoreboardManager.viewScoreboard
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import java.text.ParseException
import java.util.*

class NScoreboardC : TabExecutor {
    override fun onCommand(sender: CommandSender?, cmd: Command?, label: String?, args: Array<out String>?): Boolean {
        if (sender !is Player) return false

        val p: Player = sender

        if (othersU.isntAdmin(p)) {
            messageU.messageU.send(p, "no-perm")
            return true
        }

        val a = args?.map { it.lowercase() }
        val scoreboard = if (a?.isNotEmpty() == true && scoreboards.containsKey(a[0])) a[0] else null

        when (a!!.size) {
            1 -> if (scoreboard == null) when (a[0]) {
                "list" -> messageU.sendHeader(
                    p, "&6⧽ &eScoreboards:\n" +
                            join(scoreboards.values.mapIndexed { index, it ->
                                val str = if (scoreboards.size == 1) "⧽"
                                else if (index == 0) "⎧"
                                else if (index == scoreboards.size - 1) "⎩"
                                else "⎜"
                                "&e$str &f${it.name}&e: &f${it.getDisplay()}\n"
                            }, separator = "")
                )

                "players" -> seePlayers(p)

                "reload" -> {
                    reload()
                    messageU.send(p, "reload")
                }

                "update" -> updateAllScoreboards(p)

                else -> help(p)
            } else help(p, scoreboard)

            2 -> if (scoreboard != null) when (a[1]) {
                "clearheader" -> {
                    setTemplate(scoreboard, "")
                    messageU.send(p, "clearHeader", display(scoreboard))
                }

                "clearfooter" -> {
                    setTemplate(scoreboard, footer = "")
                    messageU.send(p, "clearFooter", display(scoreboard))
                }

                "cleartemplate" -> {
                    setTemplate(scoreboard, template = "")
                    messageU.send(p, "clearTemplate", display(scoreboard))
                }

                "pause" -> pauseScoreboard(p, scoreboard)

                "players" -> seePlayers(p, a[0])

                "view" -> viewScoreboard(p, scoreboard)

                "visiblegroups" -> seeVisibleGroups(p, scoreboard)

                else -> help(p, scoreboard)

            } else when (a[0]) {
                "delete" -> {
                    val isDeleted = deleteScoreboard(a[1])

                    if (isDeleted != null) {
                        if (isDeleted)
                            messageU.send(p, "delete1", a[1])
                        else messageU.send(p, "delete2")

                    } else messageU.send(p, "delete3")
                }

                "reset" -> if (Bukkit.getPlayerExact(a[1]) != null)
                    resetPlayer(p, Bukkit.getPlayerExact(a[1]))
                else messageU.send(p, "reset")

                "set" -> if (scoreboards.containsKey(a[1]))
                    addPlayerTo(p, p, a[1])
                else messageU.send(p, "&cEsta scoreboard não existe!")

                else -> help(p)
            }

            3 -> if (a[0] == "create") {
                if (!blacklist.contains(a[1])) {
                    if (!createScoreboard(a[1], args[2], p))
                        messageU.send(p, "create1")
                } else messageU.send(p, "create2")

            } else if (scoreboard != null) when (a[1]) {
                "addplayer" -> if (Bukkit.getPlayerExact(a[2]) != null)
                    addPlayerTo(p, Bukkit.getPlayerExact(a[2]), scoreboard)
                else messageU.send(p, "addplayer")

                "addgroup" -> if (scoreboards.containsKey(a[2]))
                    addGroupTo(p, scoreboard, a[2])
                else messageU.send(p, "addgroup")

                "pause" -> try {
                    pauseScoreboard(p, scoreboard, a[2].toInt())
                } catch (e: ParseException) {
                    messageU.send(p, "pause")
                }

                "remplayer" -> if (Bukkit.getPlayerExact(a[2]) != null)
                    remPlayerFrom(p, Bukkit.getPlayerExact(a[2]), scoreboard)
                else messageU.send(p, "remplayer")

                "remgroup" -> if (scoreboards.containsKey(a[2]))
                    remGroupFrom(p, scoreboard, a[2])
                else messageU.send(p, "remgroup")

                "setcolor" -> if (a[2].length == 2 && a[2].matches(Regex("&[a-f0-9]")))
                    setColor(p, scoreboard, a[2])
                else messageU.send(p, "setcolor")

                "setdisplay" -> setDisplay(p, scoreboard, args[2])

                "setheader" -> setTemplate(p, scoreboard, a[2])

                "setfooter" -> setTemplate(p, scoreboard, footer = a[2])

                "settemplate" -> setTemplate(p, scoreboard, template = a[2])

            } else help(p)

            4 -> if (a[0] == "create") {
                if (!blacklist.contains(a[1])) {
                    if (createScoreboard(a[1], args[2], p)) {
                        setTemplate(a[1], if (a[3] != "null") a[3] else null)
                        messageU.send(p, "template")

                    } else messageU.send(p, "create1", a[1])
                } else messageU.send(p, "create2")
            }

            5 -> if (a[0] == "create") {
                if (!blacklist.contains(a[1])) {
                    if (createScoreboard(a[1], args[2], p)) {
                        val header = if (a[3] != "null") a[3] else null
                        val template = if (a[4] != "null") a[4] else null

                        setTemplate(a[1], header, template)
                        messageU.send(p, "template")

                    } else messageU.send(p, "create1", a[1])
                } else messageU.send(p, "create2")
            }

            6 -> if (a[0] == "create") {
                if (!blacklist.contains(a[1])) {
                    if (createScoreboard(a[1], args[2], p)) {
                        val header = if (a[3] != "null") a[3] else null
                        val template = if (a[4] != "null") a[4] else null
                        val footer = if (a[5] != "null") a[5] else null

                        setTemplate(a[1], header, template, footer)
                        messageU.send(p, "template")

                    } else messageU.send(p, "create1", a[1])
                } else messageU.send(p, "create2")
            }

            else -> help(p, scoreboard)
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender?, cmd: Command?, label: String?, args: Array<out String>?): MutableList<String> {
        val a = args?.map { it.lowercase() }

        val scoreboard = if (a?.isNotEmpty() == true && scoreboards.containsKey(a[0])) scoreboards[a[0]] else null

        return Collections.emptyList()
    }

    /**
     * @param p Player.
     * @param scoreboard Scoreboard.
     *
     * Send the commands' instructions to the player.
     */
    private fun help(p: Player, scoreboard: String? = null) {
        if (scoreboard == null)
            messageU.sendHeader(
                p, """
                ${messageU.getMessage("commands.notzscoreboard")} &f/&enotzscoreboard &7+
                &7+ &ecreate &f<&ename&f> &f<&edisplay&f> (&eheader&f) (&etemplate&f) (&efooter&f) &7- ${messageU.getMessage("commands.create")}
                &7+ &edelete &f<&escoreboard&f> &7- ${messageU.getMessage("commands.delete")}
                &7+ &elist &7- ${messageU.getMessage("commands.list")}
                &7+ &eplayers &7- ${messageU.getMessage("commands.players")}
                &7+ &ereload &7- ${messageU.getMessage("commands.reload")}
                &7+ &ereset &f<&eplayer&f> &7- ${messageU.getMessage("commands.reset")}
                &7+ &eset &f<&escoreboard&f> &7- ${messageU.getMessage("commands.set")}
                &7+ &eupdate &7- ${messageU.getMessage("commands.update")}
            """.trimIndent()
            )
        else messageU.sendHeader(
            p, """
            &f/&enotzsb &a${scoreboard} &7+
            &7+ &eaddplayer &f<&eplayer&f> &7- ${messageU.getMessage("commands.scoreboard.addplayer")}
            &7+ &eaddgroup &f<&egroup&f> &7- ${messageU.getMessage("commands.scoreboard.addgroup")}
            &7+ &eclearheader &7- ${messageU.getMessage("commands.scoreboard.clearheader")}
            &7+ &eclearfooter &7- ${messageU.getMessage("commands.scoreboard.clearfooter")}
            &7+ &ecleartemplate &7- ${messageU.getMessage("commands.scoreboard.cleartemplate")}
            &7+ &epause &f(&eminutes&f) &7- ${messageU.getMessage("commands.scoreboard.pause")}
            &7+ &eplayers &7- ${messageU.getMessage("commands.scoreboard.players")}
            &7+ &eremplayer &f<&eplayer&f> &7- ${messageU.getMessage("commands.scoreboard.remplayer")}
            &7+ &eremgroup &f<&egroup&f> &7- ${messageU.getMessage("commands.scoreboard.remgroup")}
            &7+ &esetcolor &f<&ecolor&f> &7- ${messageU.getMessage("commands.scoreboard.setcolor")}
            &7+ &esetdisplay &f<&edisplay&f> &7- ${messageU.getMessage("commands.scoreboard.setdisplay")}
            &7+ &esetheader &f<&etemplate&f> &7- ${messageU.getMessage("commands.scoreboard.setheader")}
            &7+ &esetfooter &f<&etemplate&f> &7- ${messageU.getMessage("commands.scoreboard.setfooter")}
            &7+ &esettemplate &f<&etemplate&f> &7- ${messageU.getMessage("commands.scoreboard.settemplate")}
            &7+ &eview &7- ${messageU.getMessage("commands.scoreboard.view")}
            &7+ &evisiblegroups &7- ${messageU.getMessage("commands.scoreboard.visiblegroups")}
        """.trimIndent()
        )
    }
}