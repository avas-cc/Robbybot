package cc.avas.robbybot.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Data {
    private JsonNode configData;

    public void Load () {
        //Validate path
        new File(".\\data").mkdir();

        //Load config.json
        if (!new File(".\\data\\config.json").exists()) {
            try {
                new File(".\\data\\config.json").createNewFile();
                BuildConfig();
                Logger.log("[+] config.json successfully created.", 1);
            }
            catch (IOException e) { Logger.log("[-] Failed to build config.json!\n" + e, 1); }
        }
        try {
            configData = new ObjectMapper().readTree(new File(".\\data\\config.json"));
            Logger.log("[+] Config loaded.", 1);
        }
        catch (IOException e) {Logger.log("[-] Failed to load config.json!\n" + e, 1); }
    }

    public void BuildConfig () {
        HashMap<String,Object> configMap = new HashMap<>();

        HashMap<String,String> innerMap = new HashMap<>();
        innerMap.put("public", "");
        innerMap.put("private", "");
        configMap.put("guild", innerMap);

        innerMap = new HashMap<>();
        innerMap.put("roles", "");
        innerMap.put("modLogChannel", "");
        configMap.put("mod", innerMap);

        innerMap = new HashMap<>();
        innerMap.put("role", "");
        innerMap.put("channel", "");
        configMap.put("poll", innerMap);

        innerMap = new HashMap<>();
        innerMap.put("publicChannel", "");
        innerMap.put("privateChannel", "");
        configMap.put("data", innerMap);

        innerMap = new HashMap<>();
        innerMap.put("debug", "true");
        configMap.put("general", innerMap);

        configMap.put("faqDays", 3);

        configMap.put("ticketCount", 0);

        ObjectMapper mapper = new ObjectMapper();

        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("./data/config.json"), configMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
