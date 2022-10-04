package cc.avas.robbybot.moderation;

import cc.avas.robbybot.utils.data.SQL;
import net.dv8tion.jda.api.entities.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MuteSQL {
    public List<String> GetMutes () {
        List<String> mutes = new ArrayList<>();
        String sql = "SELECT * FROM mutes;";
        try {
            ResultSet rs = SQL.getConnectionObj().prepareStatement(sql).executeQuery();
            while (rs.next()) {
                mutes.add(rs.getString("user") + ";" + rs.getString("reason") + ";" + rs.getLong("start") + ";" + rs.getLong("duration"));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }

        return mutes;
    }

    public void AddMute (User user, String reason, long start, long duration) {
        String sql = "INSERT INTO mutes (user, reason, start, duration) VALUES ('" +user.getId()+"','"+reason+"',"+start+","+duration+");";
        try { SQL.getConnectionObj().createStatement().execute(sql); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void RemoveMute (User user) {
        String sql = "DELETE FROM mutes WHERE user=" + user.getId() +";";
        try { SQL.getConnectionObj().createStatement().execute(sql); } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
