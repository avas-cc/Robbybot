package cc.avas.robbybot.moderation;

import cc.avas.robbybot.utils.EmbedUtil;
import cc.avas.robbybot.utils.Logger;
import cc.avas.robbybot.utils.data.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.time.Instant;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MuteHandler {
    public static void Mute (SlashCommandInteraction event) {
        // Get data
        Role mutedRole = event.getGuild().getRolesByName("Muted", true).get(0);

        Guild guild = Data.getGuild(event.getJDA());
        Member member = event.getOption("user").getAsMember();
        User user = event.getOption("user").getAsUser();
        User modUser = event.getUser();
        String reason = event.getOption("reason").getAsString();

        // Check if the user is already muted
        if(member.getRoles().contains(mutedRole)) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Moderation")
                    .setDescription(member.getAsMention() + " is already muted");
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
            return;
        }

        int duration = 0;
        String unit = null;
        try { duration = event.getOption("duration").getAsInt(); } catch (Exception ignored) {}
        try { unit = event.getOption("unit").getAsString(); } catch (Exception ignored) {}

        // Check for perm mute
        if (duration == 0) {
            //Perm mute them
            guild.addRoleToMember(user, mutedRole).queue();
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Moderation")
                    .setDescription(user.getAsMention() + " has been permanently muted");
            new EmbedUtil().ReplyEmbed(event, eb, false, false);

            EmbedBuilder modLogEB = new EmbedBuilder()
                    .setTitle("[RB] Mute")
                    .setDescription(user.getAsMention() + " has been muted by " + modUser.getAsMention())
                    .addField("Duration", "Permanent", true)
                    .addField("Reason", reason, true);
            new EmbedUtil().ModLogEmbed(event.getJDA(), modLogEB, false);

            Logger.log("[+] User [" + user.getName() + "] has been permenantly muted by [" + modUser.getName() +"]", 2);
            return;
        }

        // Validate duration
        if((duration == 0 && unit != null) || (duration > 0 && unit == null)) {
            EmbedUtil embedUtil = new EmbedUtil();
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Moderation")
                    .setDescription("Bad duration. Please ensure both fields are correctly filled out.");
            embedUtil.ReplyEmbed(event, eb, true, true);
            return;
        }

        // Get duration
        int dur = duration;
        String unitLong = "seconds";
        switch (unit) {
            case "m" -> {
                dur = duration * 60;
                unitLong = "minutes";
            }
            case "h" -> {
                dur = duration * 3600;
                unitLong = "hours";
            }
            case "d" -> {
                dur = duration * 86400;
                unitLong = "days";
            }
        }

        // Mute them
        guild.addRoleToMember(user, mutedRole).queue();

        // Wait the duration and unmute them
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    guild.removeRoleFromMember(user, mutedRole).queue();
                    new MuteSQL().RemoveMute(user);
                    EmbedBuilder modLogEB = new EmbedBuilder()
                            .setTitle("[RB] Unmute")
                            .setDescription(user.getAsMention() + " has been unmuted");
                    new EmbedUtil().ModLogEmbed(event.getJDA(), modLogEB, true);
                    Logger.log("[+] User " + user.getName() + " has been unmuted", 2);
                } catch (Exception e) { throw new RuntimeException(e); }
            }
        };
        timer.schedule(task, dur * 1000L);

        // Add to db
        new MuteSQL().AddMute(user, reason, Instant.now().getEpochSecond(), dur);

        // Respond
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] Moderation")
                .setDescription(user.getAsMention() + " muted for " + duration + " " + unitLong);
        new EmbedUtil().ReplyEmbed(event, eb, false, false);

        // Log in modlog
        EmbedBuilder modLogEB = new EmbedBuilder()
                .setTitle("[RB] Mute")
                .setDescription(user.getAsMention() + " has been muted by " + modUser.getAsMention())
                .addField("Duration", duration + " " + unitLong, true)
                .addField("Reason", reason, true);
        new EmbedUtil().ModLogEmbed(event.getJDA(), modLogEB, false);

        // Log
        Logger.log("[+] User [" + user.getName() + "] has been muted by [" + modUser.getName() + "] for [" + duration + " " + unitLong +"]", 2);
    }

    public static void Unmute (SlashCommandInteraction event) {
        // Get data
        Guild guild = event.getGuild();
        Member mutedMember = event.getOption("user").getAsMember();
        User mutedUser = mutedMember.getUser();
        Role mutedRole = guild.getRolesByName("Muted", true).get(0);

        // Check if user is muted
        if(!mutedMember.getRoles().contains(mutedRole)) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Moderation")
                    .setDescription(mutedUser.getAsMention() + " is not muted");
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
            return;
        }

        // Remove muted role from user
        guild.removeRoleFromMember(mutedUser, mutedRole).queue();

        // SQL
        new MuteSQL().RemoveMute(mutedUser);
        //Respond
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] Moderation")
                .setDescription(mutedUser.getAsMention() + " has been unmuted");
        new EmbedUtil().ReplyEmbed(event, eb, false, false);

        // Log in modlog
        EmbedBuilder modLogEB = new EmbedBuilder()
                .setTitle("[RB] Unmute")
                .setDescription(mutedUser.getAsMention() + " has been unmuted");
        new EmbedUtil().ModLogEmbed(event.getJDA(), modLogEB, true);
    }

    public static void Load (JDA jda) {
        Guild guild = Data.getGuild(jda);
        Role mutedRole = guild.getRolesByName("Muted", true).get(0);

        List<String> mutes = new MuteSQL().GetMutes();
        for (String mute : mutes) {
            if (mute.equals("")) continue;
            String userId = mute.split(";")[0];
            User user = jda.getUserById(userId);
            long start = Integer.parseInt(mute.split(";")[2]);
            long duration = Integer.parseInt(mute.split(";")[3]);
            long now = Instant.now().getEpochSecond();

            // If their mute already expired
            if (now > (start + duration)) {
                // Remove muted role from user
                guild.removeRoleFromMember(user, mutedRole).queue();

                // SQL
                new MuteSQL().RemoveMute(user);

                // Log in modlog
                EmbedBuilder modLogEB = new EmbedBuilder()
                        .setTitle("[RB] Unmute")
                        .setDescription(user.getAsMention() + " has been unmuted");
                new EmbedUtil().ModLogEmbed(jda, modLogEB, true);
            }
            // Restart timer
            else {
                long newDuration = (start + duration) - now;
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            guild.removeRoleFromMember(user, mutedRole).queue();
                            new MuteSQL().RemoveMute(user);
                            EmbedBuilder modLogEB = new EmbedBuilder()
                                    .setTitle("[RB] Unmute")
                                    .setDescription(user.getAsMention() + " has been unmuted");
                            new EmbedUtil().ModLogEmbed(jda, modLogEB, true);
                            Logger.log("[+] User " + user.getName() + " has been unmuted", 2);
                        } catch (Exception e) { throw new RuntimeException(e); }
                    }
                };
                timer.schedule(task, newDuration * 1000L);
            }
        }
        Logger.log("[+] Loaded " + mutes.size() + " muted players.", 1);
    }
}
