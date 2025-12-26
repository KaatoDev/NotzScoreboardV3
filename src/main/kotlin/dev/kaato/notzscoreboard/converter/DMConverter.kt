package dev.kaato.notzscoreboard.converter

import dev.kaato.notzapi.utils.MessageU.Companion.sendConsole
import dev.kaato.notzscoreboard.entities.ScoreboardE
import dev.kaato.notzscoreboard.entities.ScoreboardModel
import org.bukkit.entity.Player
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.sql.SQLException

class DMConverter {
    val c = DAOConverter().database()

    // ------ SCOREBOARD START -------

    @Throws(SQLException::class, IOException::class)
    fun loadScoreboards(): HashMap<String, ScoreboardModel> {
        val sql = "select * from scoreboardmodel"

        c.prepareStatement(sql).use { ps ->
            ps.executeQuery().use {
                val scoreboards = hashMapOf<String, ScoreboardModel>()

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
    private fun deserialize(inputStream: ByteArray): ScoreboardModel? {
        if (ByteArrayInputStream(inputStream).available() == 0) return null

        val dataInput = BukkitObjectInputStream(ByteArrayInputStream(inputStream))
        dataInput.readInt()

        val scoreboard =  try {
            dataInput.readObject() as ScoreboardModel
        } catch (e: NoSuchMethodError) {
            sendConsole("Erro ao recuperar o modelo da scoreboard da database, por favor recrie a database recriando as scoreboards (não há necessidade de mexer nas configuração .yml).")
            throw RuntimeException(e)
        }

        dataInput.close()

        return scoreboard
    }

    fun hasTables(): Boolean {
        if (c.isClosed) return false
        
        val metadata = c.metaData
        val tables = metadata.getTables(null, null, "%", arrayOf("TABLE"))
        val tbs = mutableListOf<String>()
        while (tables.next()) {
            val tb = tables.getString("TABLE_NAME")
            tbs.add(tb)
        }
        tables.close()
        return tbs.isNotEmpty()
    }

    fun dropTables() {
        c.prepareStatement("drop table playermodel").use { it.execute() }
        c.prepareStatement("drop table scoreboardmodel").use { it.execute() }
    }

    fun close() {
        c.close()
    }
}