package dev.kaato.commands

import dev.kaato.Main.Companion.msgf
import dev.kaato.entities.Boardd
import dev.kaato.entities.Boarddd
import notzapi.NotzAPI.Companion.placeholderManager
import notzapi.utils.MessageU.c
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard

class NTestC : CommandExecutor {
    val linesList = mutableListOf<String>()
    var index = 0

    override fun onCommand(sender: CommandSender?, cmd: Command?, label: String?, args: Array<out String>?): Boolean {
        if (sender !is Player) return false
        val p = sender

        println(index)

        when (index) {
            0 -> {
                val scoreboard = Bukkit.getScoreboardManager().newScoreboard
                val objective = scoreboard.registerNewObjective("a", "yummy")

                objective.displaySlot = DisplaySlot.SIDEBAR
                objective.displayName = c(msgf.config.getString("prefix"))

                update()

                setLines(p, scoreboard, objective)

//                p.scoreboard = scoreboard
                index++
            }
            1 -> {
                Boardd().createBoard(p)
                index++
            }
            else  -> {
                Boarddd().createBoard(p)
                index = 0
            }
        }

        return true
    }

    private fun setLines(p: Player, scoreboard: Scoreboard, objective: Objective) {
        Bukkit.getPlayer("Gago3242").sendMessage(linesList.toString())
        Bukkit.getPlayer("Gago3242").sendMessage(linesList.size.toString())
        arrayOf("",
        "&6⧽ {player_displayname}",
        "",
        "&9⎧ {rank}",
        "&9⎩ %ezrankspro_rankprefix%{status_rankup}",
        "",
        "&2⎧ &aMoney&f: {money}",
        "&2⎩ &aCash&f: {cash}",
        "",
        "&5⎧ &dClan&f: %simpleclans_tag_label%",
        "&5⎩ &dKDR&f: %simpleclans_clan_total_kdr%").forEach { line ->
            val r = if (line.contains("{")) 0 else if (line.contains("%")) 1 else null
            val l = c(line)
            val index = linesList.size - linesList.indexOf(line)
//            println("${linesList.size} - ${linesList.indexOf(line)} $l")

            Bukkit.getPlayer("Gago3242").sendMessage("a - $line $index")
            Bukkit.getPlayer("Gago3242").sendMessage("b - $l $index")

            if (c(line).length > 23) {
                val error = c("&cLine &af$index&c > 23")
                val aaa = "&e⎧ Caso precise de ajuda"

                if (index == 3) {
                    objective.getScore(c(aaa)).score = index
                    println("-------- ${aaa.length} ${c(aaa).length} -------")
                } else objective.getScore(error).score = index


//                Bukkit.getPlayer("Gago3242").sendMessage("-----------")
//                Bukkit.getPlayer("Gago3242").sendMessage(line.map { it.code }.toString())
//                Bukkit.getPlayer("Gago3242").sendMessage(l.map { it.code }.toString())
//                Bukkit.getPlayer("Gago3242").sendMessage(aaa.hashCode().toString() + " - " + line.hashCode())
//                Bukkit.getPlayer("Gago3242").sendMessage(aaa.map { it.code }.toString())
//                Bukkit.getPlayer("Gago3242").sendMessage(c(aaa).map { it.code }.toString())
//                Bukkit.getPlayer("Gago3242").sendMessage("-----------")


            } else if (r != null) {

                val team = scoreboard.registerNewTeam("a$index")

                val prefix = l.substring(0, l.indexOf(if (r == 0) "{" else "%"))
                val suffix = placeholderManager.set(l.removePrefix(prefix))



                team.addEntry(prefix)
                team.suffix = suffix
                objective.getScore(prefix).score = index

            } else objective.getScore(l).score = index
        }
        p.scoreboard = scoreboard
    }

    fun update() { // create in manager
//        if (linesList.isNotEmpty())
//            linesList.clear()
//
//        linesList.addAll(getTemplate("player"))
//        linesList.addAll(getTemplate("staff-status"))

        var blanks = " "

        linesList.addAll(arrayOf("",
        "&6⧽ {player_displayname}",
        "",
        "&9⎧ {rank}",
        "&9⎩ %ezrankspro_rankprefix%{status_rankup}",
        "",
        "&2⎧ &aMoney&f: {money}",
        "&2⎩ &aCash&f: {cash}",
        "",
        "&5⎧ &dClan&f: %simpleclans_tag_label%",
        "&5⎩ &dKDR&f: %simpleclans_clan_total_kdr%").map {
            var l = it

//            Bukkit.getPlayer("Gago3242").sendMessage(c(l))

            if (l.isBlank()) {
                l += blanks
                blanks += " "
            }

            l.toString()
        })
    }
}