package dev.kaato.commands

import dev.kaato.manager.PlayerManager.resetPlayer
import dev.kaato.manager.PlayerManager.seePlayers
import dev.kaato.manager.ScoreboardManager.addGroupTo
import dev.kaato.manager.ScoreboardManager.addPlayerTo
import dev.kaato.manager.ScoreboardManager.blacklist
import dev.kaato.manager.ScoreboardManager.createScoreboard
import dev.kaato.manager.ScoreboardManager.deleteScoreboard
import dev.kaato.manager.ScoreboardManager.display
import dev.kaato.manager.ScoreboardManager.pauseScoreboard
import dev.kaato.manager.ScoreboardManager.remGroupFrom
import dev.kaato.manager.ScoreboardManager.remPlayerFrom
import dev.kaato.manager.ScoreboardManager.scoreboards
import dev.kaato.manager.ScoreboardManager.setColor
import dev.kaato.manager.ScoreboardManager.setDisplay
import dev.kaato.manager.ScoreboardManager.setTemplate
import dev.kaato.manager.ScoreboardManager.update
import dev.kaato.manager.ScoreboardManager.updateAllScoreboards
import dev.kaato.manager.ScoreboardManager.viewScoreboard
import notzapi.utils.MessageU.send
import notzapi.utils.MessageU.sendHeader
import notzapi.utils.OthersU.isntAdmin
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

        if (isntAdmin(p)) {
            send(p, "no-perm")
            return true
        }

        val a = args?.map { it.lowercase() }
        val scoreboard = if (a?.isNotEmpty() == true && scoreboards.containsKey(a[0])) a[0] else null

        when (a!!.size) {
            1 ->  if (scoreboard == null) when (a[0]) {
                "list" -> sendHeader(p, "&6⧽ &eScoreboards:\n" +
                        scoreboards.values.mapIndexed { index, it ->
                            val str = if (scoreboards.size == 1) "⎜"
                            else if (index == 0) "⎧"
                            else if (index == scoreboards.size-1) "⎩"
                            else "⎜"
                            "&e$str ${it.name}&e: &f${it.getDisplay()}\n"
                        })

                "players" -> seePlayers(p)

                "reload" -> {
                    update()
                    send(p, "reload")
                }

                "update" -> updateAllScoreboards(p)

                else -> help(p)
            } else help(p, scoreboard)

            2 -> if (scoreboard != null) when (a[1]) {
                "clearheader" -> {
                    setTemplate(scoreboard, "")
                    send(p, "clearHeader", display(scoreboard))
                }

                "clearfooter" -> {
                    setTemplate(scoreboard, footer = "")
                    send(p, "clearFooter", display(scoreboard))
                }

                "cleartemplate" -> {
                    setTemplate(scoreboard, template = "")
                    send(p, "clearTemplate", display(scoreboard))
                }

                "pause" -> pauseScoreboard(p, scoreboard)

                "players" -> seePlayers(p, a[0])

                "view" -> viewScoreboard(p, scoreboard)

                else -> help(p, scoreboard)

            } else when (a[0]) {
                "delete" -> {
                    val isDeleted = deleteScoreboard(a[1])

                    if (isDeleted != null) {
                        if (isDeleted)
                            send(p, "delete1", a[1])
                        else send(p, "delete2")

                    } else send(p, "delete3")
                }

                "reset" -> if (Bukkit.getPlayerExact(a[1]) != null)
                    resetPlayer(p, Bukkit.getPlayerExact(a[1]))
                else send(p, "reset")

                else -> help(p)
            }

            3 -> if (a[0] == "create") {
                if (!blacklist.contains(a[1])) {
                    if (!createScoreboard(a[1], args[2], p))
                        send(p, "create1")
                } else send(p, "create2")

            } else if (scoreboard != null) when (a[1]) {
                "addplayer" -> if (Bukkit.getPlayerExact(a[2]) != null)
                    addPlayerTo(p, Bukkit.getPlayerExact(a[2]), scoreboard)
                else send(p, "addplayer")

                "addgroup" -> if (scoreboards.containsKey(a[2]))
                    addGroupTo(p, scoreboard, a[2])
                else send(p, "addgroup")

                "pause" -> try {
                    pauseScoreboard(p, scoreboard, a[2].toInt())
                } catch (e: ParseException) {
                    send(p, "pause")
                }

                "remplayer" -> if (Bukkit.getPlayerExact(a[2]) != null)
                    remPlayerFrom(p, Bukkit.getPlayerExact(a[2]), scoreboard)
                else send(p, "remplayer")

                "remgroup" -> if (scoreboards.containsKey(a[2]))
                    remGroupFrom(p, scoreboard, a[2])
                else send(p, "remgroup")

                "setcolor" -> if (a[2].length == 2 && a[2].matches(Regex("&[a-f0-9]")))
                    setColor(p, scoreboard, a[2])
                else send(p, "setcolor")

                "setdisplay" -> setDisplay(p, scoreboard, args[2])

                "setheader" -> setTemplate(p, scoreboard, a[2])

                "setfooter" -> setTemplate(p, scoreboard, footer = a[2])

                "settemplate" -> setTemplate(p, scoreboard, template = a[2])

            }  else help(p)

            4 -> if (a[0] == "create") {
                if (!blacklist.contains(a[1])) {
                    if (createScoreboard(a[1], args[2], p)) {
                        setTemplate(a[1], if (a[3] != "null") a[3] else null)
                        send(p, "template")

                    } else send(p, "create1", a[1])
                } else send(p, "create2")
            }

            5 -> if (a[0] == "create") {
                if (!blacklist.contains(a[1])) {
                    if (createScoreboard(a[1], args[2], p)) {
                        val header = if (a[3] != "null") a[3] else null
                        val template = if (a[4] != "null") a[4] else null

                        setTemplate(a[1], header, template)
                        send(p, "template")

                    } else send(p, "create1", a[1])
                } else send(p, "create2")
            }

            6 -> if (a[0] == "create") {
                if (!blacklist.contains(a[1])) {
                    if (createScoreboard(a[1], args[2], p)) {
                        val header = if (a[3] != "null") a[3] else null
                        val template = if (a[4] != "null") a[4] else null
                        val footer = if (a[5] != "null") a[5] else null

                        setTemplate(a[1], header, template, footer)
                        send(p, "template")

                    } else send(p, "create1", a[1])
                } else send(p, "create2")
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
     * Send the commands' instructions for the player.
     */
    private fun help(p: Player, scoreboard: String? = null) {
        if (scoreboard == null)
            sendHeader(p, """
                &eUtilize &f/&enotzscoreboard &7+
                &7+ &ecreate &f<&ename&f> &f<&edisplay&f> (&eheader&f) (&etemplate&f) (&efooter&f) &7-  
                &7+ &edelete &f<&escoreboard&f> &7- Deleta uma scoreboard
                &7+ &elist &7- Lista todas as scoreboards criadas.
                &7+ &eplayers &7- Lista todos os players registrados e suas respectivas scoreboards.
                &7+ &ereload &7- Recarrega partes o plugin.
                &7+ &ereset &f<&eplayer&f> &7- Reseta a scoreboard do player para a scoreboard padrão.
                &7+ &eset &f<&escoreboard&f> &7- Seta a própria scoreboard.
                &7+ &eupdate &7- Atualiza todas as scoreboards.
            """.trimIndent())

        else sendHeader(p, """
            &f/&ensb &a${scoreboard} &7+
            &7+ &eaddplayer &f<&eplayer&f> &7- Adiciona um player à scoreboard.
            &7+ &eaddgroup &f<&egroup&f> &7- Adiciona um grupo ao VisibleGroups da scoreboard
            &7+ &eclearheader &7- Limpa a header da scoreboard.
            &7+ &eclearfooter &7- Limpa a footer da scoreboard.
            &7+ &ecleartemplate &7- Limpa o template da scoreboard.
            &7+ &epause &f(&eminutes&f) &7- Pausa a atualização da scoreboard por X minutos (por padrão é pausado por 1 minuto).
            &7+ &eplayers &7- Vê os players cadastrados na scoreboard.
            &7+ &eremplayer &f<&eplayer&f> &7- Remove um player da scoreboard.
            &7+ &eremgroup &f<&egroup&f> &7- Remove um grupo do VisibleGroups da scoreboard
            &7+ &esetcolor &f<&ecolor&f> &7- Seta uma nova cor da scoreboard.
            &7+ &esetdisplay &f<&edisplay&f> &7- Seta um novo display na scoreboard.
            &7+ &esetheader &f<&etemplate&f> &7- Seta uma nova header na scoreboard.
            &7+ &esetfooter &f<&etemplate&f> &7- Seta uma nova footer na scoreboard.
            &7+ &esettemplate &f<&etemplate&f> &7- Seta um novo template na scoreboard.
            &7+ &eview &7- Visualiza a scoreboard sem precisar setar.
        """.trimIndent())
    }
}