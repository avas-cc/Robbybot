package cc.avas.robbybot.utils.data;

import cc.avas.robbybot.utils.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Data {
    private static Config config = new Config();
    final static File configFile = new File("./data/config.json");

    public void load() {
        //Validate path
        new File("./data").mkdir();

        //Create config.json if needed
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                save();
                Logger.log("[+] config.json successfully created.", 1);
            }
            catch (IOException e) { Logger.log("[-] Failed to build config.json!\n" + e, 1); }
        }

        //Load config.json
        try {
            config = new ObjectMapper().readValue(configFile, Config.class);
            Logger.log("[+] Config loaded.", 1);
        }
        catch (IOException e) {Logger.log("[-] Failed to load config.json!\n" + e, 1); }
    }

    public static void save() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(configFile, config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Mod roles
    public static ArrayList<Role> getModRoles(JDA jda) {
        ArrayList<Role> roles = new ArrayList<>();
        String modRoles = config.getModRoles();
        if (modRoles.equals("")) return roles;

        for (String role : config.getModRoles().split(",")) {
            roles.add(jda.getRoleById(role));
        }
        return roles;
    }

    public static void setModRole(ArrayList<Role> roles) {
        String blob = "";

        if (roles.size() > 0) {
            for (Role role : roles) blob += role.getId() + ",";
            blob = blob.substring(0, blob.length() - 1);
        }
        config.setModRoles(blob);
        save();
    }

    // Mod log
    public static TextChannel getModLogChannel(JDA jda) {
        return jda.getTextChannelById(config.getModChannel());
    }

    public static void setModLogChannel(TextChannel channel) {
        config.setModChannel(channel.getId()); save();
    }

    // Guild
    public static Guild getGuild(JDA jda) { return jda.getGuildById(config.getGuildID()); }

    public static void setGuildID(Guild guild) {
        config.setGuildID(guild.getId()); save();
    }

    // Poll
    public static Role getPollRole(JDA jda) {
        return jda.getRoleById(config.getPollRole());
    }

    public static void setPollRole(Role role) {
        config.setPollRole(role.getId()); save();
    }

    public static TextChannel getPollChannel(JDA jda) {
        return jda.getTextChannelById(config.getPollChannel());
    }

    public static void setPollChannel(TextChannel channel) {
        config.setPollChannel(channel.getId()); save();
    }

    // Events
    public static TextChannel getEventChannel (JDA jda) { return jda.getTextChannelById(config.getEventsChannel()); }

    public static void setEventsChannel (TextChannel channel) {
        config.setEventsChannel(channel.getId()); save();
    }

    public static TextChannel getLeaderboardsChannel (JDA jda) { return jda.getTextChannelById(config.getLeaderboardsChannel()); }

    public static void setLeaderboardsChannel (TextChannel channel) {
        config.setLeaderboardsChannel(channel.getId()); save();
    }

    public static TextChannel getSubmissionsChannel (JDA jda) { return jda.getTextChannelById(config.getSubmissionsChannel()); }

    public static void setSubmissionsChannel (TextChannel channel) {
        config.setSubmissionsChannel(channel.getId()); save();
    }

    public static TextChannel getMapartChannel (JDA jda) { return jda.getTextChannelById(config.getMapartChannel()); }

    public static void setMapartChannel (TextChannel channel) {
        config.setMapartChannel(channel.getId()); save();
    }

    // Debug
    public static boolean getDebug() {
        return config.isDebug();
    }

    public static void setDebug(boolean set) {
        config.setDebug(set); save();
    }
}