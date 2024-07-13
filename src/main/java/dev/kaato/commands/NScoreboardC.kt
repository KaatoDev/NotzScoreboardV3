package dev.kaato.commands

import dev.kaato.entities.ScoreboardM
import dev.kaato.manager.PlayerManager.players
import dev.kaato.manager.PlayerManager.seePlayers
import dev.kaato.manager.ScoreboardManager.createScoreboard
import dev.kaato.manager.ScoreboardManager.default_group
import dev.kaato.manager.ScoreboardManager.deleteScoreboard
import dev.kaato.manager.ScoreboardManager.notScoreboards
import dev.kaato.manager.ScoreboardManager.scoreboards
import dev.kaato.manager.ScoreboardManager.update
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
        val scoreboard = if (a?.isNotEmpty() == true && scoreboards.containsKey(a[0])) scoreboards[a[0]] else null

        when (a!!.size) {
            1 ->  if (scoreboard == null) when (a[0]) {
                "list" -> {
                    sendHeader(p, scoreboards.values.joinToString(separator = "\n", prefix = "", postfix = "") { "${it.name}&e: &f${it.getDisplay()}" })
                }

                "players" -> seePlayers(p)

                "reload" -> {
                    update()
                    send(p, "&aPlugin reiniciado.")
                }
                else -> help(p, scoreboard)
            } else help(p, scoreboard)

            2 -> if (scoreboard != null && a[1] == "players")
                    seePlayers(p, a[0])
                else if (a[0] == "delete") {
                val isDeleted = deleteScoreboard(a[1])

                if (isDeleted != null) {
                    if (isDeleted)
                        send(p, "&eA &fscoreboard ${a[1]}&e foi &cdeletedata&e com &asucesso&e!")

                    else send(p, "&cA scoreboard inserida não existe!")

                } else send(p, "&eA scoreboard ${scoreboards[a[1]]!!.getDisplay()}&e está setada como padrão e por isso &cnão pode ser deletada&e!")

            } else help(p, scoreboard)

            3 -> if (a[0] == "create") {
                if (notScoreboards.contains(a[1]))
                    send(p, "&cUtilize outro nome para a scoreboard!")

                else if (createScoreboard(a[1], args[2]))
                    send(p, "&eA &fscoreboard ${args[2]}&e foi criada com &asucesso&e!")

                else send(p, "&cA &fscoreboard ${a[1]}&c já existe!")

            } else if (scoreboard != null)
                when (a[1]) {
                    "addplayer" -> if (Bukkit.getPlayer(a[2]) != null) {
                        if (scoreboard!!.addPlayer(Bukkit.getPlayer(a[2])))
                            send(p, "&eA &fscoreboard ${scoreboard!!.getDisplay()}&e foi adicionada ao player ${Bukkit.getPlayer(a[2]).name}&e.")

                        else send(p, "&cO player ${Bukkit.getPlayer(a[2]).name}&c já possui esta scoreboard.")

                    } else send(p, "&cO player inserido não existe ou está offline.")

                    "addgroup" -> if (scoreboards.containsKey(a[2])) {
                        if (scoreboard!!.addGroup(a[2]))
                            send(p, "&eO grupo ${scoreboards[a[2]]!!.getDisplay()}&e foi adicionado aos visiblegroups da &fscoreboard ${scoreboard!!.getDisplay()}&e.")

                        else send(p, "&cO grupo ${scoreboards[a[2]]!!.getDisplay()}&c já faz parte dos visiblegroups da &fscoreboard ${scoreboard!!.getDisplay()}&c.")

                    } else send(p, "&cO grupo inserido não existe.")

                    "remplayer" -> if (Bukkit.getPlayer(a[2]) != null) {
                        if (scoreboard!!.remPlayer(Bukkit.getPlayer(a[2])))
                            send(p, "&eA &fscoreboard ${scoreboard!!.getDisplay()}&e foi removida do player ${Bukkit.getPlayer(a[2]).name}&e.")

                        else send(p, "&cO player ${Bukkit.getPlayer(a[2]).name}&c já possui a &fscoreboard ${if (players.containsKey(a[2])) players[a[2]] else default_group}&c.")

                    } else send(p, "&cO player inserido não existe ou está offline.")

                    "remgroup" -> if (scoreboards.containsKey(a[2])) {
                        if (scoreboard!!.remGroup(a[2]))
                            send(p, "&eO grupo ${scoreboards[a[2]]!!.getDisplay()}&e foi removido dos visiblegroups da &fscoreboard ${scoreboard!!.getDisplay()}&e.")

                        else send(p, "&cO grupo ${scoreboards[a[2]]!!.getDisplay()}&c não faz parte dos visiblegroups da &fscoreboard ${scoreboard!!.getDisplay()}&c.")

                    } else send(p, "&cO grupo inserido não existe.")

                    "setcolor" -> if (a[2].length == 2 && a[2].matches(Regex("&[a-f0-9]"))) {
                        val temp = scoreboard!!.getDisplay()
                        if (a[2] != temp) {
                            scoreboard!!.color = a[2]
                            send(p, "&eA color da &fscoreboard ${scoreboard!!.getDisplay()}&e foi alterado de (&c${temp[0]} ${temp[1]}&e) para (&a${a[2][0]} ${a[2][1]}&e).")

                        } else send(p, "&aEsta já é a cor atual da &fscoreboard ${scoreboard!!.getDisplay()}&c!")

                    } else send(p, "&cUtilize um formato válido de cor. &7(/cores)")

                    "setdisplay" -> {
                        val temp = scoreboard!!.getDisplay()
                        if (args[2] != temp) {
                            scoreboard!!.setDisplay(args[2])
                            send(p, "&eDisplay da &fscoreboard ${scoreboard!!.name}&e alterado de &c$temp&e para &a${a[2]}&e.")
                        } else send(p, "&aEsta já é o display atual da &fscoreboard ${scoreboard!!.getDisplay()}&c!")
                    }

                    "setheader" -> {
                        val temp = scoreboard!!.header
                        if (a[2] != temp) {
                            scoreboard!!.header = a[2]
                            send(p, "&eA header da &fscoreboard ${scoreboard!!.getDisplay()}&e foi alterado de &c$temp&e para &a${a[2]}&e.")

                        } else send(p, "&aEsta já é a header atual da &fscoreboard ${scoreboard!!.getDisplay()}&c!")
                    }

                    "setfooter" -> {
                        val temp = scoreboard!!.footer
                        if (a[2] != temp) {
                            scoreboard!!.footer = a[2]
                            send(p, "&eA footer da &fscoreboard ${scoreboard!!.getDisplay()}&e foi alterado de &c$temp&e para &a${a[2]}&e.")

                        } else send(p, "&aEsta já é a footer atual da &fscoreboard ${scoreboard!!.getDisplay()}&c!")
                    }

                    "settemplate" -> {
                        val temp = scoreboard!!.template

                        if (a[2] != temp) {
                            scoreboard!!.template = a[2]
                            send(p, "&eO template da &fscoreboard ${scoreboard!!.getDisplay()}&e foi alterado de &c$temp&e para &a${a[2]}&e.")

                        } else send(p, "&aEste já é o template atual da &fscoreboard ${scoreboard!!.getDisplay()}&c!")
                    }

                    "setpriority" -> if (arrayOf("low", "medium", "high").contains(a[2])) {
                        val temp = scoreboard!!.getPriority()
                        var priority = ""

                        if (scoreboard!!.setPriority(
                                when (a[2]) {
                                    "medium" -> {
                                        priority = "medium"
                                        false
                                    }

                                    "high" -> {
                                        priority = "high"
                                        true
                                    }

                                    else -> {
                                        priority = "low"
                                        null
                                    }
                                })) {
                            send(p, "&eA prioridade da &fscoreboard ${scoreboard!!.getDisplay()}&e foi alterado de &c$temp&e para &a${a[2]}&e.")

                        } else send(p, "&cEsta já é a prioridade atual da &fscoreboard ${scoreboard!!.getDisplay()}&c!")

                    } else send(p, "&cUtilize &f/&cnsb setpriority &f<&clow&f/&cmedium&f/&chigh&f>")

                    "usestaffstatus" -> try {
                        val staff_status = a[2].toBoolean()

                        if (scoreboard!!.alterStaffStatus(staff_status)) {
                            send(p, "&eO staff-status da &fscoreboard ${scoreboard!!.getDisplay()}&e foi ${if (staff_status) "&aativado" else "&cdesativado"}&e com sucesso.")

                        } else send(p, "&eO staff-status da &fscoreboard ${scoreboard!!.getDisplay()}&e já está ${if (staff_status) "&aativado" else "&cdesativado"}&e.")

                    } catch (e: ParseException) {
                        send(p, "&cUtilize &c/&cnsb usestaffstatus &f<&ctrue&f/&cfalse&f>")
                    }
                }

            else help(p, scoreboard)
            else -> help(p, scoreboard)
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender?, cmd: Command?, label: String?, args: Array<out String>?): MutableList<String> {
        val a = args?.map { it.lowercase() }

        val scoreboard = if (a?.isNotEmpty() == true && scoreboards.containsKey(a[0])) scoreboards[a[0]] else null

        return Collections.emptyList()
    }

    private fun help(p: Player, scoreboard: ScoreboardM?) {
        if (scoreboard == null)
            sendHeader(p, """
                &eUtilize &f/&enotzscoreboard &7+
                &7+ &ecreate <name> <display>
                &7+ &edelete <scoreboard>
                &7+ &eremove <player>${"" /* criaarrrrrrrr */}
                &7+ &elist
                &7+ &eplayers
                &7+ &ereload
            """.trimIndent())

        else sendHeader(p, """
            &f/&ensb &a${scoreboard.name} &7+
            &7+ addplayer <player>
            &7+ addgroup <group>
            &7+ players
            &7+ remplayer <player>
            &7+ remgroup <group>
            &7+ setcolor <color>
            &7+ setdisplay <display>
            &7+ setheader <template>
            &7+ setfooter <template>
            &7+ settemplate <template>
            &7+ setpriority <low/medium/high>
            &7+ usestaffstatus <true/false>
        """.trimIndent())
    }
}