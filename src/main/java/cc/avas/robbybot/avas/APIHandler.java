package cc.avas.robbybot.avas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class APIHandler {
    // AVAS API
    public String[] GetPublicPlayerStats (String user) {
        try {
            URL url = new URL("https://api.avas.cc/player?username=" + user);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String content = reader.readLine();
            String[] stats = content.substring(1, content.length()-1).split(",");
            return stats;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
