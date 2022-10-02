package cc.avas.robbybot.tickets;

import cc.avas.robbybot.utils.EmbedUtil;
import cc.avas.robbybot.utils.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class TicketHandler {
    public static void Start (SlashCommandInteraction event) {
        //Ticket selection
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] Tickets")
                .setDescription("Select a ticket category to create a new ticket. You will be given a template to copy below.\n\nUse `/ticket title` to add a brief description of the ticket.\nUse `/ticket add/remove` to add or remove users to being able to see the ticket.");
        new EmbedUtil().ReplyEmbed(event, eb, false, false);
        Button button0 = Button.primary("ticket_start_0", "In-game \uD83C\uDFAE");
        Button button1 = Button.primary("ticket_start_1", "Discord \uD83D\uDCAC");
        Button button2 = Button.primary("ticket_start_2", "Other \uD83D\uDCDD");
        Button button3 = Button.danger("ticket_start_3", "Private \uD83D\uDEAB");
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
