package dev.kaato.entities

import notzapi.utils.MessageU.c
import org.bukkit.Bukkit
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot

class Boardd : Runnable {

    override fun run() {
        Bukkit.getOnlinePlayers().forEach { p: Player ->
            if (p.scoreboard?.getObjective("Main") != null) updateScore(p)
            else createBoard(p)
        }
    }

    fun createBoard(p: Player) {
        val sc = Bukkit.getScoreboardManager().newScoreboard
        val ob = sc.registerNewObjective("Main", "yummy")

        ob.displaySlot = DisplaySlot.SIDEBAR
        ob.displayName = c("&aaaaaaa")

        ob.getScore("").score = 6
        ob.getScore("test1").score = 5
        ob.getScore(" ").score = 4
        ob.getScore("test2").score = 3
        ob.getScore("  ").score = 2

        // ---------------------
        val team1 = sc.registerNewTeam("team1")
        val tk1: String = c("&a")

        team1.addEntry(tk1)
        team1.prefix = "rola: "
        team1.suffix = "0 cm"

        ob.getScore(tk1).score = 1

        // ---------------------
        val team2 = sc.registerNewTeam("team2")
        val tk2: String = c("&b")

        team2.addEntry(tk2)
        team2.prefix = "grosso: "
        team2.suffix = "0 cm"

        ob.getScore(tk2).score = 0

        // ---------------------
        p.scoreboard = sc
    }

    private fun updateScore(p: Player) {
        val sc = p.scoreboard

        val team1 = sc.getTeam("team1")
        team1.suffix = p.getStatistic(Statistic.WALK_ONE_CM).toString() + " cm"

        val team2 = sc.getTeam("team2")
        team2.suffix = p.getStatistic(Statistic.SPRINT_ONE_CM).toString() + " cm"
    }
}