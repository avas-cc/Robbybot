package cc.avas.robbybot.tickets;

import net.dv8tion.jda.api.entities.Message;

public class Ticket {
    int id;
    int category;
    int priority;
    int status;
    String title;
    String channelId;
    String infoEmbedMessageId;
    String trackerEmbedId;
    long lastUpdated;
    String assigneeId;
    Message closeConfirmMessage;
}
