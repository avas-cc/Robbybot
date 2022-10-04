package cc.avas.robbybot.utils.data;

import cc.avas.robbybot.utils.Logger;
import net.dv8tion.jda.api.entities.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SQL {

    private static Connection conn = null;

    public void Load () {
        Connect();
        try {
            CreateTables();
            Logger.log("[+] Tables validated.", 1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            Logger.log("[-] Unable to validate tables!.", 1);
        }

        Logger.log("[+] SQL loaded.", 1);
    }

    public void Connect () {
        try {
            String url = "jdbc:sqlite:.\\data\\rb.db";
            conn = DriverManager.getConnection(url);
            Logger.log("[+] Connection to [rb.db] has been established.", 1);
        } catch (SQLException e) {
            Logger.log("[-] Failed to connect to rb.db!\n" + e.getMessage(), 1);
        }
    }

    public void CreateTables () throws SQLException {
        Statement stmt = conn.createStatement();
        String sql = """
                CREATE TABLE IF NOT EXISTS mutes (
                    id integer PRIMARY KEY,
                    user text NOT NULL,
                    reason text NOT NULL,
                    start long NOT NULL,
                    duration long NOT NULL
                );""";
        stmt.execute(sql);

        sql = """
                CREATE TABLE IF NOT EXISTS tickets (
                    id integer PRIMARY KEY,
                    title text NOT NULL,
                    category integer NOT NULL,
                    priority integer NOT NULL,
                    status integer NOT NULL,
                    assignee text,
                    lastUpdated long,
                    channelId text,
                    infoEmbedId text,
                    trackerEmbedId text
                );""";
        stmt.execute(sql);
    }

    public static Connection getConnectionObj () {
        return conn;
    }

    public void GetTickets () {

    }

    public void AddTicket () {

    }

    public void RemoveTicket() {

    }

    public void SetTicket () {

    }
}
