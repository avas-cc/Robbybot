package cc.avas.robbybot.tickets;

import cc.avas.robbybot.utils.EmbedUtil;
import cc.avas.robbybot.utils.Logger;
import cc.avas.robbybot.utils.data.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.awt.*;
import java.time.Instant;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TicketBuilder {
    final static String[] prio = {"QOL", "Low", "Medium", "High", "Immediate"};
    final static String[] stat = {"New", "Scheduled", "In Progress", "Needs Update", "Completed", "Closed"};
    final static String[] catEmoji = {"\uD83C\uDFAE", "\uD83D\uDCAC", "\uD83D\uDCDD", "\uD83D\uDEAB"};

    public static void SendModal (ButtonInteractionEvent event, String cat) {
        TextInput[] opt = {
                TextInput.create("title", "Title:", TextInputStyle.SHORT).setPlaceholder("Brief description of the issue").setRequiredRange(1, 50).build(),
                TextInput.create("text0", "Reported by:", TextInputStyle.SHORT).setPlaceholder("User(s) who reported the issue").setRequired(false).build(),
                TextInput.create("text1", "Affected component/command", TextInputStyle.SHORT).setRequired(false).build(),
                TextInput.create("text2", "Existing since:", TextInputStyle.SHORT).setRequired(false).build(),
                TextInput.create("desc", "Description:", TextInputStyle.PARAGRAPH).setRequired(false).setMaxLength(500).build()
        };

        Modal modal = Modal.create("ticket-temp-" + cat, "Ticket Template")
                .addActionRows(ActionRow.of(opt[0]), ActionRow.of(opt[1]), ActionRow.of(opt[2]), ActionRow.of(opt[3]), ActionRow.of(opt[4]))
                .build();

        event.replyModal(modal).queue();
    }

    public static void GetModalData (ModalInteractionEvent event) {
        String title = "None";
        String blob = "";
        int cat = Integer.parseInt(event.getModalId().split("-")[2]);

        for(ModalMapping value : event.getValues()) {
            switch (value.getId()) {
                case "title" -> title = value.getAsString();
                case "text0" -> {
                    if(value.getAsString().equals("")) break;
                    blob += "\n**Reported by:**\n" + value.getAsString() + "\n";
                }
                case "text1" -> {
                    if(value.getAsString().equals("")) break;
                    blob += "\n**Affected component/command:**\n" + value.getAsString() + "\n";
                }
                case "text2" -> {
                    if(value.getAsString().equals("")) break;
                    blob += "\n**Existing since:**\n" + value.getAsString() + "\n";
                }
                case "desc" -> {
                    if(value.getAsString().equals("")) break;
                    blob += "\n**Description:**\n" + value.getAsString();
                }
            }
        }

        CreateTicket(event.getJDA(), event.getUser(), event.getGuild(), title, cat, blob);
        new EmbedUtil().ReplyEmbed(event, new EmbedBuilder().setDescription("Ticket created"), true, false);
    }

    public static void CreateTicket (JDA jda, User user, Guild guild, String title, int category, String infoBlob) {
        Guild publicGuild = Data.GetGuildPublic(jda);

        // Set basic ticket data
        Ticket t = new Ticket();
        t.id = Data.GetTicketCount() + 1;
        t.category = category;
        t.priority = 2;
        t.status = 0;
        t.title = title;
        t.lastUpdated = Instant.now().getEpochSecond();
        t.assigneeId = user.getId();

        Logger.log("[t][" + t.id + "] New ticket ID " + t.id + " created by " + user, 3);

        // Create ticket channel/objects
        List<Role> modRole = Data.GetModRoles(jda);
        ChannelAction<TextChannel> newChannel = publicGuild.createTextChannel(catEmoji[t.category] + "-ticket-" + t.id, guild.getCategoriesByName("Tickets", true).get(0))
                .addPermissionOverride(publicGuild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL));
        for(Role role : modRole) newChannel.addRolePermissionOverride(role.getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL), null);
        CompletableFuture<?> future = CompletableFuture.allOf(newChannel.submit());
        while (!future.isDone()) continue;

        Logger.log("[t][" + t.id + "] Ticket channel created: " + t.title, 3);

        //Create/send status buttons
        Button[] statusButton = new Button[5];
        for(int i = 1; i < 5; i++) statusButton[i-1] = Button.secondary("ticket-stat-" + i + "-" + t.id, stat[i]);
        statusButton[4] = Button.danger("ticket-stat-5-" + t.id, stat[5]);

        //Create/send priority buttons
        Button[] prioButton = new Button[5];
        for(int i = 0; i < 5; i++) prioButton[i] = Button.secondary("ticket-prio-" + i + "-" + t.id, prio[i]);

        TextChannel ticketChannel = null;
        for (TextChannel _channel : publicGuild.getTextChannels()) {
            if (_channel.getName().contains("ticket-" + t.id)) {
                ticketChannel = _channel;
                t.channelId = _channel.getId();
                break;
            }
        }

        ticketChannel.sendMessage(new MessageBuilder().setContent(" ").setActionRows(ActionRow.of(prioButton)).build()).queue();
        ticketChannel.sendMessage(new MessageBuilder().setContent(" ").setActionRows(ActionRow.of(statusButton)).build()).queue();

        //Ticket object data
        EmbedBuilder infoEB = new EmbedBuilder()
                .setTitle(catEmoji[t.category] + " " + t.id + ": " + t.title)
                .addField("   Priority   ", prio[t.priority], true)
                .addField("   Status   ", stat[t.status], true)
                .addField(" Assignee ", jda.getUserById(t.assigneeId).getName(), false)
                .setColor(Color.CYAN);

        String _title = t.title;
        if(t.category == 3) _title = "Private";
        //Tracker embed
        EmbedBuilder trackerEB = new EmbedBuilder()
                .setTitle(catEmoji[t.category] + " " + t.id + ": " + _title)
                .addField("   Priority ", prio[t.priority], true)
                .addField(" Status ", stat[t.status], true)
                .addField(" Assignee ", jda.getUserById(t.assigneeId).getName(), true)
                .addField(" Updated ", new Date(t.lastUpdated*1000).toString(), true)
                .setColor(Color.CYAN);
        MessageAction action0 = ticketChannel.sendMessageEmbeds(infoEB.build());
        MessageAction action1 = publicGuild.getTextChannelsByName("ticket-tracker", true).get(0).sendMessageEmbeds(trackerEB.build());
        try {
            t.infoEmbedMessageId = action0.submit().get().getId();
            t.trackerEmbedId = action1.submit().get().getId();
        } catch (InterruptedException | ExecutionException e) { throw new RuntimeException(e); }

        if(infoBlob.length() > 0) new EmbedUtil().SendEmbed(ticketChannel, new EmbedBuilder().setDescription(infoBlob), false);

        // Last stuff
        new TicketHandler().AddTicket(t);
        Data.SetTicketCount(Data.GetTicketCount() + 1);
    }
}
