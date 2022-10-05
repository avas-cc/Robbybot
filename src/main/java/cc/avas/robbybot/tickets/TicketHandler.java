package cc.avas.robbybot.tickets;

import cc.avas.robbybot.utils.EmbedUtil;
import cc.avas.robbybot.utils.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.awt.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TicketHandler {
    static TicketSQL sql = new TicketSQL();
    private static List<Ticket> ticketList = new ArrayList<>();

    public static void LoadTickets () {
        ticketList = sql.GetTickets();
        Logger.log("[+] Loaded " + ticketList.size() + " tickets.", 3);
    }

    public void AddTicket (Ticket t) {
        ticketList.add(t);
        sql.AddTicket(t);
    }

    public static void UpdateTitle (SlashCommandInteraction event) {
        String title = event.getOption("title").getAsString();
        Ticket t = getTicketById(Integer.parseInt(event.getChannel().getName().split("-")[2]));
        t.title = title;
        sql.UpdateTitle(t, title);
        UpdateTicketEmbeds(event.getJDA(), t);

        Logger.log("[t][" + t.id + "] " + event.getUser().getName() + " changed title to: " + title, 3);
        new EmbedUtil().ReplyEmbed(event, new EmbedBuilder().setFooter("Successfully updated title to:\n" + t.title), true, false);
    }

    public static void UpdatePriority (ButtonInteractionEvent event, int id, int p) {
        String[] prio = {"QOL", "Low", "Medium", "High", "Immediate"};
        Ticket t = getTicketById(id);
        t.priority = p;
        sql.UpdatePriority(t, p);
        UpdateTicketEmbeds(event.getJDA(), t);

        Logger.log("[t][" + t.id + "] " + event.getUser().getName() + " changed priority to: " + prio[t.priority], 3);
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[" + t.id + "] Changed priority to: *" + prio[t.priority] + "*")
                .setDescription("By **" + event.getUser().getName() + "** at **" + new Timestamp(System.currentTimeMillis()).toString().substring(11, 19) + "**");
        new EmbedUtil().SendEmbed(event.getTextChannel(), eb, false);
    }

    public static void UpdateStatus(ButtonInteractionEvent event, int id, int s) {
        String[] stat = {"New", "Scheduled", "In Progress", "Needs Update", "Completed", "Closed"};
        Ticket t = getTicketById(id);

        //Prompt for close confirmation
        if(s == 5) {
            EmbedBuilder closeEB = new EmbedBuilder()
                    .setTitle("[WARN] Are you sure you want to close this ticket?")
                    .setDescription("Clicking yes will delete this channel and the ticket entry from the tracker. You cannot undo this action.");
            new EmbedUtil().SendEmbed(event.getTextChannel(), closeEB, true);

            Button button0 = Button.danger("ticket-close-1-" + t.id, "YES");
            Button button1 = Button.secondary("ticket-close-0-" + t.id, "NO");
            Message message = new MessageBuilder().setContent(" ")
                    .setActionRows(ActionRow.of(button0, button1))
                    .build();

            MessageAction action = event.getChannel().sendMessage(message);
            try {
                t.closeConfirmMessage = action.submit().get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        t.status = s;
        sql.UpdateStatus(t, s);
        UpdateTicketEmbeds(event.getJDA(), t);

        Logger.log("[t][" + t.id + "] " + event.getUser().getName() + " changed status to: " + stat[t.status], 3);
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[" + t.id + "] Changed status to: *" + stat[t.status] + "*")
                .setDescription("By **" + event.getUser().getName() + "** at **" + new Timestamp(System.currentTimeMillis()).toString().substring(11, 19) + "**");
        new EmbedUtil().SendEmbed(event.getTextChannel(), eb, false);
    }

    public static void UpdateAssignee (SlashCommandInteraction event) {
        User assignee = event.getOption("user").getAsUser();
        Member member = event.getOption("user").getAsMember();
        String assigneeId = assignee.getId();
        Ticket t = getTicketById(Integer.parseInt(event.getChannel().getName().split("-")[2]));

        t.assigneeId = assigneeId;
        sql.UpdateAssignee(t, assigneeId);
        UpdateTicketEmbeds(event.getJDA(), t);

        event.getTextChannel().upsertPermissionOverride(member).setAllowed(Permission.VIEW_CHANNEL).queue();

        Logger.log("[t][" + t.id + "] " + event.getUser().getName() + " changed assignee to: " + assignee.getName(), 3);
        EmbedUtil embedUtil = new EmbedUtil();
        embedUtil.ReplyEmbed(event, new EmbedBuilder().setFooter("Successfully assigned ticket to:\n" + assignee.getName()), true, false);
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[" + t.id + "] Assigned ticket to: *" + assignee.getName() + "*")
                .setDescription("By **" + event.getUser().getName() + "** at **" + new Timestamp(System.currentTimeMillis()).toString().substring(11, 19) + "**");
        embedUtil.SendEmbed(event.getTextChannel(), eb, false);
    }

    public static void Claim (SlashCommandInteraction event) {
        User assignee = event.getUser();
        String assigneeId = assignee.getId();
        Ticket t = getTicketById(Integer.parseInt(event.getChannel().getName().split("-")[2]));
        t.assigneeId = event.getUser().getId();
        sql.UpdateAssignee(t, assigneeId);
        UpdateTicketEmbeds(event.getJDA(), t);

        Logger.log("[t][" + t.id + "] " + event.getUser().getName() + " changed assignee to: " + assignee.getName(), 3);
        EmbedUtil embedUtil = new EmbedUtil();
        embedUtil.ReplyEmbed(event, new EmbedBuilder().setFooter("Successfully assigned ticket to:\n" + assignee.getName()), true, false);
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[" + t.id + "] Assigned ticket to: *" + assignee.getName() + "*")
                .setDescription("By **" + event.getUser().getName() + "** at **" + new Timestamp(System.currentTimeMillis()).toString().substring(11, 19) + "**");
        embedUtil.SendEmbed(event.getTextChannel(), eb, false);
    }

    public static void CloseTicket (ButtonInteractionEvent event, int id, int flag) {
        Ticket t = getTicketById(id);
        // No
        if (flag == 0) {
            t.closeConfirmMessage.delete().queue();
            t.status = 2;
            UpdateTicketEmbeds(event.getJDA(), t);
            return;
        }

        // Yes
        event.getJDA().getTextChannelsByName("ticket-tracker", true).get(0).deleteMessageById(t.trackerEmbedId).queue();
        event.getChannel().delete().queue();
        sql.RemoveTicket(t);
        ticketList.remove(t);
        Logger.log("[t][" + t.id + "] " + event.getUser().getName() + " closed the ticket", 3);
    }

    public static Ticket getTicketById (int id) {
        for (Ticket t : ticketList) {
            if (t.id == id) return t;
        }
        return null;
    }

    static void UpdateTicketEmbeds (JDA jda, Ticket t) {
        String[] prio = {"QOL", "Low", "Medium", "High", "Immediate"};
        String[] stat = {"New", "Scheduled", "In Progress", "Needs Update", "Completed", "Closed"};
        String[] catEmoji = {"\uD83C\uDFAE", "\uD83D\uDCAC", "\uD83D\uDCDD", "\uD83D\uDEAB"};
        String title = t.title;

        // Internal info embed
        EmbedBuilder infoEB = new EmbedBuilder()
                .setTitle(catEmoji[t.category] + " " + t.id + ": " + title)
                .addField("   Priority ", prio[t.priority], true)
                .addField(" Status ", stat[t.status], true)
                .addField(" Assignee ", jda.getUserById(t.assigneeId).getName(), false)
                .addField(" Updated ", new Date(t.lastUpdated*1000).toString(), true)
                .setColor(Color.CYAN);

        // Tracker embed
        if(t.category == 3) title = "Private";
        EmbedBuilder trackerEB = new EmbedBuilder()
                .setTitle(catEmoji[t.category] + " " + t.id + ": " + title)
                .addField("   Priority ", prio[t.priority], true)
                .addField(" Status ", stat[t.status], true)
                .addField(" Assignee ", jda.getUserById(t.assigneeId).getName(), true)
                .addField(" Updated ", new Date(t.lastUpdated*1000).toString(), true)
                .setColor(Color.CYAN);

        TextChannel ticketChannel = jda.getTextChannelById(t.channelId);
        TextChannel trackerChannel = jda.getTextChannelsByName("ticket-tracker", true).get(0);
        ticketChannel.editMessageEmbedsById(t.infoEmbedMessageId, infoEB.build()).queue();
        trackerChannel.editMessageEmbedsById(t.trackerEmbedId, trackerEB.build()).queue();
    }

    public static void AddUser (SlashCommandInteraction event) {
        Ticket t = getTicketById(Integer.parseInt(event.getChannel().getName().split("-")[2]));
        Member member = event.getOption("user").getAsMember();

        event.getTextChannel().upsertPermissionOverride(member).setAllowed(Permission.VIEW_CHANNEL).queue();

        Logger.log("[t][" + t.id + "] " + event.getUser().getName() + " added " + member.getUser().getName() + " to the ticket", 3);
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[" + t.id + "] Added user: *" + member.getUser().getName() + "*")
                .setDescription("By **" + event.getUser().getName() + "** at **" + new Timestamp(System.currentTimeMillis()).toString().substring(11, 19) + "**");
        new EmbedUtil().SendEmbed(event.getTextChannel(), eb, false);
        new EmbedUtil().ReplyEmbed(event, new EmbedBuilder().setFooter("Successfully added user to ticket"), true, false);
    }

    public static void RemoveUser (SlashCommandInteraction event) {
        Ticket t = getTicketById(Integer.parseInt(event.getChannel().getName().split("-")[2]));
        Member member = event.getOption("user").getAsMember();

        event.getTextChannel().upsertPermissionOverride(member).setDenied(Permission.VIEW_CHANNEL).queue();

        Logger.log("[t][" + t.id + "] " + event.getUser().getName() + " removed " + member.getUser().getName() + " from the ticket", 3);
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[" + t.id + "] Removed user: *" + member.getUser().getName() + "*")
                .setDescription("By **" + event.getUser().getName() + "** at **" + new Timestamp(System.currentTimeMillis()).toString().substring(11, 19) + "**");
        new EmbedUtil().SendEmbed(event.getTextChannel(), eb, false);
        new EmbedUtil().ReplyEmbed(event, new EmbedBuilder().setFooter("Successfully removed user to ticket"), true, false);
    }

    public static void CreateTicketPanel (SlashCommandInteraction event) {
        //Ticket selection
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] Tickets")
                .setDescription("Select a ticket category to create a new ticket. You will be given a template to copy below.\n\nUse `/ticket title` to add a brief description of the ticket.\nUse `/ticket add/remove` to add or remove users to being able to see the ticket.");
        new EmbedUtil().ReplyEmbed(event, eb, false, false);
        Button button0 = Button.primary("ticket-start-0", "In-game \uD83C\uDFAE");
        Button button1 = Button.primary("ticket-start-1", "Discord \uD83D\uDCAC");
        Button button2 = Button.primary("ticket-start-2", "Other \uD83D\uDCDD");
        Button button3 = Button.danger("ticket-start-3", "Private \uD83D\uDEAB");
        Message message = new MessageBuilder()
                .setContent(" ")
                .setActionRows(ActionRow.of(button0, button1, button2, button3))
                .build();
        event.getChannel().sendMessage(message).queue();
        Logger.log("[+] Ticket service started in " + "[" + event.getChannel().getName() + "]", 3);

        //Tracker
        TextChannel trackerChannel = event.getGuild().getTextChannelsByName("ticket-tracker", true).get(0);
        EmbedBuilder trackerEB = new EmbedBuilder()
                .setTitle("[RB] AVAS Ticket Tracker")
                .setDescription("This channel updates in real-time, so as soon as any changes to the progress or status of the ticket are made it will reflect those changes here.")
                .addField("Categories:", "\uD83C\uDFAE - AVAS\n\uD83D\uDCAC - Discord\n\uD83D\uDCDD - Other\n\uD83D\uDEAB - Private", true)
                .addField("Priorities:", "Low\nMedium\nHigh\nImmediate", true)
                .addField("Timeframes:*", "7 days\n5 days\n3 days\n1 day", true)
                .setFooter("*Priority status is subject to change as tickets progress. These are estimated timeframes - there is no guarantee the ticket will be resolved within these estimates.");
        new EmbedUtil().SendEmbed(trackerChannel, trackerEB, false);
        Logger.log("[+] Ticket tracker started " + "[" + trackerChannel.getName() + "]", 3);
    }
}
