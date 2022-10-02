package cc.avas.robbybot.utils.data;

import cc.avas.robbybot.utils.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Data {
    private static Config config = new Config();
    final static File configFile = new File(".\\data\\config.json");

    public void Load () {
        //Validate path
        new File(".\\data").mkdir();

        //Create config.json if needed
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                Save();
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

    public static void Save () {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(configFile, config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Public guild
    public static Guild GetGuildPublic (JDA jda) {
        return jda.getGuildById(config.getGuildPublic());
    }

    public static void SetGuildPublic (Guild guild) {
        config.setGuildPublic(guild.getId());
        Save();
    }

    // Private guild
    public static Guild GetGuildPrivate (JDA jda) {
        return jda.getGuildById(config.getGuildPrivate());
    }

    public static void SetGuildPrivate (Guild guild) {
        config.setGuildPrivate(guild.getId());
        Save();
    }

    // Mod roles
    public static ArrayList<Role> GetModRoles (JDA jda) {
        ArrayList<Role> roles = new ArrayList<>();
        String modRoles = config.getModRoles();
        if (modRoles.equals("")) return roles;

        for (String role : config.getModRoles().split(",")) {
            roles.add(jda.getRoleById(role));
        }
        return roles;
    }

    public static void SetModRole (ArrayList<Role> roles) {
        String blob = "";

        if (roles.size() > 0) {
            for (Role role : roles) blob += role.getId() + ",";
            blob = blob.substring(0, blob.length() - 1);
        }
        config.setModRoles(blob);
        Save();
    }
}
