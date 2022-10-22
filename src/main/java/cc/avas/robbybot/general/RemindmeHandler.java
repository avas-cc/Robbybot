package cc.avas.robbybot.general;

import cc.avas.robbybot.utils.EmbedUtil;
import cc.avas.robbybot.utils.Logger;
import cc.avas.robbybot.utils.data.SQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

public class RemindmeHandler {
    public static void RemindMe(SlashCommandInteraction event) {
        int inDur = event.getOption("duration").getAsInt();
        int duration = inDur;
        String inUnit = event.getOption("unit").getAsString();
        String unitLong = " seconds";
        String content = event.getOption("content").getAsString();
        User user = event.getUser();

        switch (inUnit) {
            case "m" -> {
                duration = inDur * 60;
                unitLong = " minutes";
            }
            case "h" -> {
                duration = inDur * 3600;
                unitLong = " hours";
            }
            case "d" -> {
                duration = inDur * 86400;
                unitLong = " days";
            }
        }

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] Reminders")
                .setDescription("I'll remind you about " + content + " in " + inDur + unitLong);
        new EmbedUtil().ReplyEmbed(event, eb, true, false);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                user.openPrivateChannel()
                        .flatMap(channel -> channel.sendMessage("Hey " + user.getName() + ", " + content))
                        .queue();
                RemoveReminder(user, content);
            }
        }, duration * 1000L);

        AddReminder(user, content, Instant.now().getEpochSecond(), duration);
    }

    public static void Load (JDA jda) {
        String sql = "SELECT * FROM reminders;";
        int count = 0;
        try {
            ResultSet rs = SQL.getConnectionObj().prepareStatement(sql).executeQuery();
            while (rs.next()) {
                User user = jda.getUserById(rs.getString("userId"));
                String content = rs.getString("content");
                long start = rs.getLong("start");
                long duration = rs.getLong("duration");
                long now = Instant.now().getEpochSecond();

                // If their reminder already passed
                if (now > (start + duration)) {
                    user.openPrivateChannel()
                            .flatMap(channel -> channel.sendMessage("Hey " + user.getName() + ", " + content))
                            .queue();
                    RemoveReminder(user, content);
                }
                else {
                    long newDuration = (start + duration) - now;

                    Timer timer = new Timer();
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            user.openPrivateChannel()
                                    .flatMap(channel -> channel.sendMessage("Hey " + user.getName() + ", " + content))
                                    .queue();
                            RemoveReminder(user, content);
                        }
                    };
                    timer.schedule(task, newDuration * 1000L);
                }
                count++;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        Logger.log("[+] Loaded " + count + " reminders.", 1);
    }

    static void AddReminder (User user, String content, long start, long duration) {
        String sql = "INSERT INTO reminders (userId, content, start, duration) VALUES ('" +user.getId()+"','"+content+"',"+start+","+duration+");";
        try { SQL.getConnectionObj().createStatement().execute(sql); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    static void RemoveReminder (User user, String content) {
        String sql = "DELETE FROM reminders WHERE userId='" + user.getId() +"' AND content='" + content + "';";
        try { SQL.getConnectionObj().createStatement().execute(sql); } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
