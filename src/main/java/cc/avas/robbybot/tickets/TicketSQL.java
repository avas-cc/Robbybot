package cc.avas.robbybot.tickets;

import cc.avas.robbybot.utils.data.SQL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TicketSQL {
    public List<Ticket> GetTickets () {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM tickets;";

        try {
            ResultSet rs = SQL.getConnectionObj().prepareStatement(sql).executeQuery();
            while (rs.next()) {
                Ticket t = new Ticket();
                t.id = rs.getInt("id");
                t.title = rs.getString("title");
                t.category = rs.getInt("category");
                t.priority = rs.getInt("priority");
                t.status = rs.getInt("status");
                t.assigneeId = rs.getString("assignee");
                t.lastUpdated = rs.getLong("lastUpdated");
                t.channelId = rs.getString("channelId");
                t.infoEmbedMessageId = rs.getString("infoEmbedId");
                t.trackerEmbedId = rs.getString("trackerEmbedId");
                tickets.add(t);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }

        return tickets;
    }

    public void AddTicket (Ticket t) {
        String values = "(" + t.id + ",'" + t.title + "'," + t.category + "," + t.priority + "," + t.status + ",'" + t.assigneeId + "'," + t.lastUpdated + ",'" + t.channelId + "','" + t.infoEmbedMessageId + "','" + t.trackerEmbedId + "');";
        String sql = "INSERT INTO tickets (id, title, category, priority, status, assignee, lastUpdated, ChannelId, infoEmbedId, trackerEmbedId) VALUES " + values;
        try { SQL.getConnectionObj().createStatement().execute(sql); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void RemoveTicket(Ticket t) {
        String sql = "DELETE FROM tickets WHERE id=" + t.id;
        try { SQL.getConnectionObj().createStatement().execute(sql); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void UpdateTitle (Ticket t, String title) {
        String sql = "UPDATE tickets SET title='" + title + "' WHERE id=" + t.id;
        try { SQL.getConnectionObj().createStatement().execute(sql); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void UpdatePriority (Ticket t, int p) {
        String sql = "UPDATE tickets SET priority=" + p + " WHERE id=" + t.id;
        try { SQL.getConnectionObj().createStatement().execute(sql); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void UpdateStatus (Ticket t, int s) {
        String sql = "UPDATE tickets SET status=" + s + " WHERE id=" + t.id;
        try { SQL.getConnectionObj().createStatement().execute(sql); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void UpdateAssignee (Ticket t, String id) {
        String sql = "UPDATE tickets SET assignee='" + id + "' WHERE id=" + t.id;
        try { SQL.getConnectionObj().createStatement().execute(sql); } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
