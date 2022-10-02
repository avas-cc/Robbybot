package cc.avas.robbybot.handlers;

import cc.avas.robbybot.tickets.TicketHandler;
import cc.avas.robbybot.utils.EmbedUtil;
import cc.avas.robbybot.utils.Logger;
import cc.avas.robbybot.utils.data.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class CommandHandler {
    static InteractionHandler i = new InteractionHandler();
    public static void Handle(SlashCommandInteraction event) throws IOException, ExecutionException, InterruptedException {
        switch (event.getName()) {
            //Admin commands
            case "ticket" -> {
                if (!i.CheckPermission(event, 2)) return;
                switch (event.getSubcommandName()) {
                    case "start" -> TicketHandler.Start(event);
//                    case "title" -> TicketHandler.SetTitle(event);
//                    case "claim" -> TicketHandler.Claim(event);
//                    case "assign" -> TicketHandler.Assign(event);
//                    case "add" -> TicketHandler.AddUser(event);
//                    case "remove" -> TicketHandler.RemoveUser(event);
                }
            }
            case "config" -> {
                if (!i.CheckPermission(event, 2)) return;
                switch (event.getSubcommandName()) {
                    case "guild" -> HandleGuild(event);
                    case "mod-roles" -> HandleModRoles(event);
                    case "mod-log" -> HandleModLog(event);
                    case "data" -> HandleDataChannels(event);
                    case "poll" -> HandlePollConfig(event);
                    case "debug" -> HandleDebug(event);
                }
            }
        }
    }

    // Config commands
    static void HandleGuild (SlashCommandInteraction event) {
        String guild = event.getOption("set").getAsString();
        if (guild.equals("public")) Data.SetGuildPublic(event.getGuild());
        else if (guild.equals("private")) Data.SetGuildPrivate(event.getGuild());
        else {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Admin")
                    .setDescription("Invalid syntax! Please use \"public\" or \"private\".");
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
            return;
        }
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] Admin")
                .setDescription("Guilds updated.");
        new EmbedUtil().ReplyEmbed(event, eb, true, false);

    }

    static void HandleModRoles (SlashCommandInteraction event) {
        Role addRole = null;
        Role removeRole = null;
        boolean list = false;
        try { addRole = event.getOption("add").getAsRole(); } catch (Exception ignored) {}
        try { removeRole = event.getOption("remove").getAsRole(); } catch (Exception ignored) {}
        try { list = event.getOption("list").getAsBoolean(); } catch (Exception ignored) {}

        if (addRole != null) {
            ArrayList<Role> modRoles = Data.GetModRoles(event.getJDA());
            if (modRoles.contains(addRole)) {
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("[RB] Admin")
                        .setDescription("Role **@" + addRole.getName() + "** is already added to the moderator role list");
                new EmbedUtil().SendEmbed(event.getTextChannel(), eb, true);
            } else {
                modRoles.add(addRole);
                Data.SetModRole(modRoles);
            }
        }

        if (removeRole != null) {
            ArrayList<Role> modRoles = Data.GetModRoles(event.getJDA());
            if (modRoles.contains(removeRole)) {
                modRoles.remove(removeRole);
                Data.SetModRole(modRoles);
            } else {
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("[RB] Admin")
                        .setDescription("Role **@" + removeRole.getName() + "** is not added to the moderator role list");
                new EmbedUtil().SendEmbed(event.getTextChannel(), eb, true);
            }
        }

        if (list) {
            String blob = "";
            for (Role role : Data.GetModRoles(event.getJDA())) blob += role.getName() + "\n";
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Admin")
                    .setDescription("**Moderator roles:**\n" + blob);
            new EmbedUtil().ReplyEmbed(event, eb, true, false);
        } else {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Admin")
                    .setDescription("Updated moderator roles.");
            new EmbedUtil().ReplyEmbed(event, eb, true, false);
        }
    }

    static void HandleModLog (SlashCommandInteraction event) {
        TextChannel channel = event.getOption("channel").getAsTextChannel();
        Data.SetModLogChannel(channel);
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] Admin")
                .setDescription("Set mod log to **" + channel.getName() + "**");
        new EmbedUtil().ReplyEmbed(event, eb, true, false);
    }

    static void HandleDataChannels (SlashCommandInteraction event) {
        TextChannel pub = null;
        TextChannel priv = null;

        try { pub = event.getOption("public").getAsTextChannel(); } catch (Exception ignored) {}
        try { priv = event.getOption("private").getAsTextChannel(); } catch (Exception ignored) {}

        if (pub != null) Data.SetDataPublicChannel(pub);
        if (priv != null) Data.SetDataPrivateChannel(priv);

        if (pub != null || priv != null) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Admin")
                    .setDescription("Data channels updated.");
            new EmbedUtil().ReplyEmbed(event, eb, true, false);
        } else {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Admin")
                    .setDescription("Failed to update data channels!");
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
        }
    }

    static void HandleDebug (SlashCommandInteraction event) {
        Data.SetDebug(!Data.GetDebug());
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] Admin")
                .setDescription("Set config.debug to `" + Data.GetDebug() + "`");
        new EmbedUtil().ReplyEmbed(event, eb, true, false);
    }

    static void HandlePollConfig (SlashCommandInteraction event) {
        Role role = null;
        TextChannel channel = null;

        try { role = event.getOption("role").getAsRole(); } catch (Exception ignored) {}
        try { channel = event.getOption("channel").getAsTextChannel(); } catch (Exception ignored) {}

        if (role != null) Data.SetPollRole(role);
        if (channel != null) Data.SetPollChannel(channel);

        if (role != null || channel != null) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Admin")
                    .setDescription("Poll config updated.");
            new EmbedUtil().ReplyEmbed(event, eb, true, false);
        } else {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Admin")
                    .setDescription("Failed to update poll config!");
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
        }
    }
}
