package cc.avas.robbybot.tickets;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;

import java.util.Timer;

public class Ticket {
    int id;
    int category;
    int priority = 2;
    int status = 0;
    String title;
    String channelId;
    String closeConfirmMessageId;
    String infoEmbedMessageId;
    String trackerEmbedId;
    String lastUpdated;
    String assigneeId;
    Timer timer;

    public Message getChannel(JDA jda) {
        return null;
    } // etc...
}
