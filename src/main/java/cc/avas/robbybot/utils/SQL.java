package cc.avas.robbybot.utils;

import java.sql.*;

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
                    handle text NOT NULL,
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

    public void GetMutes () {

    }

    public void AddMute () {

    }

    public void RemoveMute () {

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
