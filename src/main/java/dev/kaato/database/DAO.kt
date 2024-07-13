package dev.kaato.database

import dev.kaato.Main.Companion.cf
import dev.kaato.Main.Companion.pathRaw
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DAO {
    private val c: Connection
    private val sql = if (cf.config.getBoolean("useMySQL"))
        arrayOf("""
            create table if not exists scoreboardmodel(
            id int primary key auto_increment,
            name varchar(36) unique not null,
            scoreboard blob not null)
        """.trimIndent(), """
            create table if not exists playermodel(
            id int primary key auto_increment,
            name varchar(36) unique not null,
            scoreboardid int not null,
            constraint scoreboardidfk foreign key (scoreboardid) references scoreboardmodel(id) on delete cascade)
        """.trimIndent())

    else arrayOf("""
            create table if not exists scoreboardmodel(
            id integer primary key autoincrement,
            name varchar(36) unique not null,
            scoreboard blob not null)
        """.trimIndent(), """
            create table if not exists playermodel(
            id integer primary key autoincrement,
            name varchar(36) unique not null,
            scoreboardid int not null,
            constraint scoreboardidfk foreign key (scoreboardid) references scoreboardmodel(id) on delete cascade)
        """.trimIndent())


    init {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:$pathRaw/notzscoreboard.db")
            sql.forEach { c.prepareStatement(it).use { ps -> ps.execute() } }

        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    fun database(): Connection {
        return c
    }
}