package dev.kaato.notzscoreboard.database

import dev.kaato.notzscoreboard.entities.ScoreboardM
import dev.kaato.notzscoreboard.entities.ScoreboardModel
import notzapi.utils.MessageU.send
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.sql.SQLException

class DM {
    val c = DAO().database()

    // ------ SCOREBOARD START -------

    @Throws(SQLException::class, IOException::class)
    fun insertScoreboard(scoreboard: ScoreboardModel) {
        val sql = "insert into scoreboardmodel values (null, ?, ?)"

        c.prepareStatement(sql).use {
            it.setString(1, scoreboard.name)
            it.setBytes(2, serialize(scoreboard))
            it.execute()
        }
    }

    @Throws(SQLException::class, IOException::class)
    fun deleteScoreboard(scoreboard: ScoreboardModel) {
        val sql = "delete from scoreboardmodel where name = ?"

        c.prepareStatement(sql).use {
            it.setString(1, scoreboard.name)
            it.execute()
        }
    }

    @Throws(SQLException::class, IOException::class)
    fun updateScoreboard(scoreboard: ScoreboardModel) {
        val sql = "update scoreboardmodel set scoreboard = ? where name = ?"

        c.prepareStatement(sql).use {
            it.setBytes(1, serialize(scoreboard))
            it.setString(2, scoreboard.name)
            it.execute()
        }
    }

    @Throws(SQLException::class, IOException::class)
    fun loadScoreboards(): HashMap<String, ScoreboardM> {
        val sql = "select * from scoreboardmodel"

        c.prepareStatement(sql).use { ps ->
            ps.executeQuery().use {
                val scoreboards = hashMapOf<String, ScoreboardM>()

                while (it.next()) {
//                    println(it.getString("name"))
                    scoreboards[it.getString("name")] = deserialize(it.getBytes("scoreboard"))!!
                }

                return scoreboards
            }
        }
    }

    // ------ SCOREBOARD END -------

    // -------------------------------------------------------

    // ------ PLAYER START -------

    @Throws(SQLException::class, IOException::class)
    fun insertPlayer(player: Player, scoreboard: String) {
        val sql = "insert into playermodel values (null, ?, ?)"

        c.prepareStatement(sql).use {
            it.setString(1, player.name)
            it.setString(2, scoreboard)
            it.execute()
        }
    }

    @Throws(SQLException::class, IOException::class)
    fun deletePlayer(player: Player) {
        val sql = "delete from playermodel where name = ?"

        c.prepareStatement(sql).use {
            it.setString(1, player.name)
            it.execute()
        }
    }

    @Throws(SQLException::class, IOException::class)
    fun updatePlayer(player: Player, scoreboard: String) {
        val sql = "update playermodel set scoreboardname = ? where name = ?"

        c.prepareStatement(sql).use {
            it.setString(1, scoreboard)
            it.setString(2, player.name)

            it.execute()
        }
    }

    @Throws(SQLException::class, IOException::class)
    fun loadPlayers(): HashMap<String, String> {
        val sql = "select * from playermodel"

        c.prepareStatement(sql).use {
            val players = hashMapOf<String, String>()

            it.executeQuery().use { rs ->
                while (rs.next())
                    players[rs.getString("name")] = rs.getString("scoreboardname")
            }

            return players
        }
    }

    // ------ PLAYER END -------

    // -------------------------------------------------------

    @Throws(RuntimeException::class)
    private fun serialize(scoreboardmodel: ScoreboardModel): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val dataOutput = BukkitObjectOutputStream(outputStream)

        dataOutput.writeInt(1)
        dataOutput.writeObject(scoreboardmodel)
        dataOutput.close()

        return outputStream.toByteArray()
    }

    @Throws(RuntimeException::class)
    private fun deserialize(inputStream: ByteArray): ScoreboardM? {
        if (ByteArrayInputStream(inputStream).available() == 0) return null

        val dataInput = BukkitObjectInputStream(ByteArrayInputStream(inputStream))
        dataInput.readInt()

        val s = try {
            dataInput.readObject() as ScoreboardModel
        } catch (e: NoSuchMethodError) {
            send(Bukkit.getConsoleSender(), "Erro ao recuperar o modelo da scoreboard da database, por favor recrie a database recriando as scoreboards (não há necessidade de mexer nas configuração .yml).")
            throw RuntimeException(e)
        }


        val scoreboard = ScoreboardM(s.name, s.display, s.header, s.template, s.footer, s.color, s.visibleGroups)

        dataInput.close()

        return scoreboard
    }
}