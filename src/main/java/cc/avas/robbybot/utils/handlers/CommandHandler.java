package cc.avas.robbybot.utils.handlers;

import cc.avas.robbybot.general.RemindmeHandler;
import cc.avas.robbybot.moderation.MuteHandler;
import cc.avas.robbybot.polls.PollHandler;
import cc.avas.robbybot.tickets.TicketHandler;
import cc.avas.robbybot.utils.EmbedUtil;
import cc.avas.robbybot.utils.Logger;
import cc.avas.robbybot.utils.data.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CommandHandler {
    static InteractionHandler i = new InteractionHandler();
    public static void RegisterCommands(JDA jda) {
        List<CommandData> commandData = new ArrayList<>();
        // Admin commands
        // config
        commandData.add(Commands.slash("config", "Configure channels, roles and stuff for the bot")
                .addSubcommands(new SubcommandData("guild", "Set public and private guilds")
                        .addOptions(new OptionData(OptionType.STRING, "set", "Set public/private guild", true)
                                .addChoice("public", "public")
                                .addChoice("private", "private")))
                .addSubcommands(new SubcommandData("data", "Set public and private data channels")
                        .addOption(OptionType.CHANNEL, "public", "Set public data channel")
                        .addOption(OptionType.CHANNEL, "private", "Set private data channel"))
                .addSubcommands(new SubcommandData("mod-roles", "Configure mod stuff")
                        .addOption(OptionType.ROLE, "add", "Add role to the mod role list")
                        .addOption(OptionType.ROLE, "remove", "Remove role from the mod role list")
                        .addOption(OptionType.BOOLEAN, "list", "List moderator roles"))
                .addSubcommands(new SubcommandData("mod-log", "Set moderation logging channel")
                        .addOption(OptionType.CHANNEL,"channel", "Channel to set as moderation log", true))
                .addSubcommands(new SubcommandData("poll", "Configure poll role and channel")
                        .addOption(OptionType.ROLE, "role", "Role to ping when poles are made")
                        .addOption(OptionType.CHANNEL, "channel", "Channel to make new polls in"))
                .addSubcommands(new SubcommandData("debug", "Toggle debugging mode")));

        // ticket
        commandData.add(Commands.slash("ticket", "Start ticket service in current channel")
                .addSubcommands(new SubcommandData("start", "Start ticket service in current channel"))
                .addSubcommands(new SubcommandData("title", "Brief description of the ticket")
                        .addOption(OptionType.STRING, "title", "Brief description of the ticket", true))
                .addSubcommands(new SubcommandData("assign", "Assign ticket to a user")
                        .addOption(OptionType.USER, "user", "User to assign to the ticket", true))
                .addSubcommands(new SubcommandData("claim", "Assign ticket to yourself"))
                .addSubcommands(new SubcommandData("add", "Add user to the ticket")
                        .addOption(OptionType.USER, "user", "Add user to the ticket", true))
                .addSubcommands(new SubcommandData("remove", "Remove user from the ticket")
                        .addOption(OptionType.USER, "user", "Remove user from the ticket", true)));

        // Mod commands
        // mute
        commandData.add(Commands.slash("mute", "Mute a user")
                .addOption(OptionType.USER, "user", "User to mute", true)
                .addOption(OptionType.STRING, "reason", "Reason for the mute", true)
                .addOption(OptionType.INTEGER, "duration", "Duration of mute")
                .addOptions(new OptionData(OptionType.STRING, "unit", "Unit of time for duration of mute")
                        .addChoice("s", "s")
                        .addChoice("m", "m")
                        .addChoice("h", "h")
                        .addChoice("d", "d")));

        // unmute
        commandData.add(Commands.slash("unmute", "Unmute a user")
                .addOption(OptionType.USER, "user", "User to unmute", true));

        // poll
        commandData.add(Commands.slash("poll", "Create a poll")
                .addOption(OptionType.STRING, "question", "Poll question", true)
                .addOption(OptionType.STRING, "responses", "Separate with `;`. Omit for YES/NO. "));

        // General
        // remindme
        commandData.add(Commands.slash("remindme", "Set a reminder")
                .addOption(OptionType.STRING, "content", "What do you need reminding?", true)
                .addOption(OptionType.INTEGER, "duration", "of time", true)
                .addOptions(new OptionData(OptionType.STRING, "unit", "of time", true)
                        .addChoice("s", "s")
                        .addChoice("m", "m")
                        .addChoice("h", "h")
                        .addChoice("d", "d")));


        jda.updateCommands().addCommands(commandData).queue();
        System.out.println("[Robbybot] [+] Registered " + commandData.toArray().length + " commands to " + jda.getGuilds());
    }

    public static void Handle(SlashCommandInteraction event) throws IOException, ExecutionException, InterruptedException {
        switch (event.getName()) {
            // Admin commands
            case "ticket" -> {
                if (!i.CheckPermission(event, 2)) return;
                switch (event.getSubcommandName()) {
                    case "start" -> TicketHandler.CreateTicketPanel(event);
                    case "title" -> TicketHandler.UpdateTitle(event);
                    case "claim" -> TicketHandler.Claim(event);
                    case "assign" -> TicketHandler.UpdateAssignee(event);
                    case "add" -> TicketHandler.AddUser(event);
                    case "remove" -> TicketHandler.RemoveUser(event);
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

            // Mod commands
            case "mute" -> {
                if (!i.CheckPermission(event, 1)) return;
                MuteHandler.Mute(event);
            }
            case "unmute" -> {
                if (!i.CheckPermission(event, 1)) return;
                MuteHandler.Unmute(event);
            }
            case "poll" -> {
                if(!i.CheckPermission(event, 1)) return;
                PollHandler.Handle(event);
            }

            // General
            case "remindme" -> RemindmeHandler.RemindMe(event);
        }
    }

    // Config commands
    static void HandleGuild (SlashCommandInteraction event) {
        String guild = event.getOption("set").getAsString();
        if (guild.equals("public")) {
            Data.SetGuildPublic(event.getGuild());
            Logger.log("[+] Public guild updated to [" + event.getGuild().getName() + "].", 1);
        }
        else if (guild.equals("private")) {
            Data.SetGuildPrivate(event.getGuild());
            Logger.log("[+] Private guild updated to [" + event.getGuild().getName() + "].", 1);
        }
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
                Logger.log("[+] Role [" + addRole.getName() + "] added to mod roles list.", 1);
            }
        }

        if (removeRole != null) {
            ArrayList<Role> modRoles = Data.GetModRoles(event.getJDA());
            if (modRoles.contains(removeRole)) {
                modRoles.remove(removeRole);
                Data.SetModRole(modRoles);
                Logger.log("[+] Role [" + removeRole.getName() + "] removed from mod roles list.", 1);
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
        Logger.log("[+] Channel [" + channel.getName() + "] set as mod log.", 1);
    }

    static void HandleDataChannels (SlashCommandInteraction event) {
        TextChannel pub = null;
        TextChannel priv = null;

        try { pub = event.getOption("public").getAsTextChannel(); } catch (Exception ignored) {}
        try { priv = event.getOption("private").getAsTextChannel(); } catch (Exception ignored) {}

        if (pub != null) {
            Data.SetDataPublicChannel(pub);
            Logger.log("[+] Channel [" + pub.getName() + "] set as public data channel.", 1);
        }
        if (priv != null) {
            Data.SetDataPrivateChannel(priv);
            Logger.log("[+] Channel [" + priv.getName() + "] set as private data channel.", 1);
        }

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

    static void HandlePollConfig (SlashCommandInteraction event) {
        Role role = null;
        TextChannel channel = null;

        try { role = event.getOption("role").getAsRole(); } catch (Exception ignored) {}
        try { channel = event.getOption("channel").getAsTextChannel(); } catch (Exception ignored) {}

        if (role != null) {
            Data.SetPollRole(role);
            Logger.log("[+] Role [" + role.getName() + "] set as poll role.", 1);
        }
        if (channel != null) {
            Data.SetPollChannel(channel);
            Logger.log("[+] Channel [" + channel.getName() + "] set as poll channel.", 1);
        }

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

    static void HandleDebug (SlashCommandInteraction event) {
        Data.SetDebug(!Data.GetDebug());
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] Admin")
                .setDescription("Set config.debug to `" + Data.GetDebug() + "`");
        new EmbedUtil().ReplyEmbed(event, eb, true, false);
        Logger.log("[+] DEBUG set to [" + Data.GetDebug() + "].", 1);
    }
}
