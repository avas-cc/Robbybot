package cc.avas.robbybot.utils.handlers;

import cc.avas.robbybot.events.EventManager;
import cc.avas.robbybot.general.RemindmeHandler;
import cc.avas.robbybot.moderation.MuteHandler;
import cc.avas.robbybot.polls.PollHandler;
import cc.avas.robbybot.utils.EmbedUtil;
import cc.avas.robbybot.utils.Logger;
import cc.avas.robbybot.utils.data.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
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
                .addSubcommands(new SubcommandData("guild", "Set this guild ID"))
                .addSubcommands(new SubcommandData("mod-roles", "Configure mod stuff")
                        .addOption(OptionType.ROLE, "add", "Add role to the mod role list")
                        .addOption(OptionType.ROLE, "remove", "Remove role from the mod role list")
                        .addOption(OptionType.BOOLEAN, "list", "List moderator roles"))
                .addSubcommands(new SubcommandData("mod-log", "Set moderation logging channel")
                        .addOption(OptionType.CHANNEL,"channel", "Channel to set as moderation log", true))
                .addSubcommands(new SubcommandData("poll", "Configure poll role and channel")
                        .addOption(OptionType.ROLE, "role", "Role to ping when poles are made")
                        .addOption(OptionType.CHANNEL, "channel", "Channel to make new polls in"))
                .addSubcommands(new SubcommandData("events", "Configure poll role and channel")
                        .addOption(OptionType.CHANNEL, "events-channel", "Channel to display event info in")
                        .addOption(OptionType.CHANNEL, "submissions-channel", "Channel to send submissions to")
                        .addOption(OptionType.CHANNEL, "leaderboards-channel", "Channel to hold event winners")
                        .addOption(OptionType.CHANNEL, "maparts-channel", "Channel to send maparts to from mapart contests"))
                .addSubcommands(new SubcommandData("debug", "Toggle debugging mode")));

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

        // events
        commandData.add(Commands.slash("event", "Manage event stuff")
                .addOptions(new OptionData(OptionType.STRING, "action", "Start/stop an event. Use /event start voting to stop submissions!", true)
                        .addChoice("start", "start")
                        .addChoice("stop", "stop"))
                .addOptions(new OptionData(OptionType.STRING, "event", "Which event to manage")
                        .addChoice("mapart-contest", "mapart contest")
                        .addChoice("voting", "voting"))
                .addOption(OptionType.INTEGER, "duration", "Duration of event in DAYS"));

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

        // submit
        commandData.add(Commands.slash("submit", "Submit your entry for an event!")
                .addOption(OptionType.ATTACHMENT, "attachment", "Attach an image of your entry!", true)
                .addOption(OptionType.STRING, "title", "Give your submission a title!", true));


        jda.updateCommands().addCommands(commandData).queue();
        System.out.println("[Robbybot] [+] Registered " + commandData.toArray().length + " commands to " + jda.getGuilds());
    }

    public static void Handle(SlashCommandInteraction event) throws IOException, ExecutionException, InterruptedException {
        switch (event.getName()) {
            // Admin commands
            case "config" -> {
                if (!i.CheckPermission(event, 2)) return;
                switch (event.getSubcommandName()) {
                    case "guild" -> HandleGuild(event);
                    case "mod-roles" -> HandleModRoles(event);
                    case "mod-log" -> HandleModLog(event);
                    case "poll" -> HandlePollConfig(event);
                    case "events" -> handleEventConfig(event);
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

            case "event" -> {
                if(!i.CheckPermission(event, 1)) return;
                EventManager.handle(event);
            }

            // General
            case "remindme" -> RemindmeHandler.RemindMe(event);
            case "submit" -> EventManager.submit(event);
        }
    }

    // Config commands
    static void HandleGuild (SlashCommandInteraction event) {
        Data.setGuildID(event.getGuild());
    }

    static void HandleModRoles (SlashCommandInteraction event) {
        Role addRole = null;
        Role removeRole = null;
        boolean list = false;
        try { addRole = event.getOption("add").getAsRole(); } catch (Exception ignored) {}
        try { removeRole = event.getOption("remove").getAsRole(); } catch (Exception ignored) {}
        try { list = event.getOption("list").getAsBoolean(); } catch (Exception ignored) {}

        if (addRole != null) {
            ArrayList<Role> modRoles = Data.getModRoles(event.getJDA());
            if (modRoles.contains(addRole)) {
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("[RB] Admin")
                        .setDescription("Role **@" + addRole.getName() + "** is already added to the moderator role list");
                new EmbedUtil().SendEmbed(event.getTextChannel(), eb, true);
            } else {
                modRoles.add(addRole);
                Data.setModRole(modRoles);
                Logger.log("[+] Role [" + addRole.getName() + "] added to mod roles list.", 1);
            }
        }

        if (removeRole != null) {
            ArrayList<Role> modRoles = Data.getModRoles(event.getJDA());
            if (modRoles.contains(removeRole)) {
                modRoles.remove(removeRole);
                Data.setModRole(modRoles);
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
            for (Role role : Data.getModRoles(event.getJDA())) blob += role.getName() + "\n";
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
        Data.setModLogChannel(channel);
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] Admin")
                .setDescription("Set mod log to **" + channel.getName() + "**");
        new EmbedUtil().ReplyEmbed(event, eb, true, false);
        Logger.log("[+] Channel [" + channel.getName() + "] set as mod log.", 1);
    }

    static void HandlePollConfig (SlashCommandInteraction event) {
        Role role = null;
        TextChannel channel = null;

        try { role = event.getOption("role").getAsRole(); } catch (Exception ignored) {}
        try { channel = event.getOption("channel").getAsTextChannel(); } catch (Exception ignored) {}

        if (role != null) {
            Data.setPollRole(role);
            Logger.log("[+] Role [" + role.getName() + "] set as poll role.", 1);
        }
        if (channel != null) {
            Data.setPollChannel(channel);
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

    static void handleEventConfig (SlashCommandInteraction event) {
        TextChannel eventChannel = null;
        TextChannel lbChannel = null;
        TextChannel subChannel = null;
        TextChannel mapartChannel = null;

        try { eventChannel = event.getOption("events-channel").getAsTextChannel(); } catch (Exception ignored) {}
        try { lbChannel = event.getOption("leaderboards-channel").getAsTextChannel(); } catch (Exception ignored) {}
        try { subChannel = event.getOption("submissions-channel").getAsTextChannel(); } catch (Exception ignored) {}
        try { mapartChannel = event.getOption("maparts-channel").getAsTextChannel(); } catch (Exception ignored) {}

        if (eventChannel != null) {
            Data.setEventsChannel(eventChannel);
            Logger.log("[+] Channel [" + eventChannel.getName() + "] set as events channel.", 1);
        }
        if (lbChannel != null) {
            Data.setLeaderboardsChannel(lbChannel);
            Logger.log("[+] Channel [" + lbChannel.getName() + "] set as leaderboards channel.", 1);
        }
        if (subChannel != null) {
            Data.setSubmissionsChannel(subChannel);
            Logger.log("[+] Channel [" + subChannel.getName() + "] set as submissions channel.", 1);
        }
        if (mapartChannel != null) {
            Data.setMapartChannel(mapartChannel);
            Logger.log("[+] Channel [" + mapartChannel.getName() + "] set as mapart channel.", 1);
        }

        if (eventChannel != null || lbChannel != null || subChannel != null || mapartChannel != null) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Admin")
                    .setDescription("Event config updated.");
            new EmbedUtil().ReplyEmbed(event, eb, true, false);
        } else {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Admin")
                    .setDescription("Failed to update event config!");
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
        }
    }

    static void HandleDebug (SlashCommandInteraction event) {
        Data.setDebug(!Data.getDebug());
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] Admin")
                .setDescription("Set config.debug to `" + Data.getDebug() + "`");
        new EmbedUtil().ReplyEmbed(event, eb, true, false);
        Logger.log("[+] DEBUG set to [" + Data.getDebug() + "].", 1);
    }
}
