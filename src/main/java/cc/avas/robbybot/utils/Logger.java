package cc.avas.robbybot.utils;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    private static File[] log = {null, null, null, null}; //sysLog, botLog, modLog, ticketLog;

    public static void Load () {
        //Validate paths
        String[] paths = {"/sys","/bot","/mod","/tickets"};
        if (!new File("./logs").exists()) new File("./logs").mkdir();
        for (String path : paths) if(!new File("./logs" + path).exists()) new File("./logs" + path).mkdir();

        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        //Create new log
        for (int i = 0; i < paths.length; i++) {
            log[i] = new File("./logs" + paths[i] + "/" + today + ".log");
            try { if (!log[i].exists()) log[i].createNewFile(); } catch (Exception e) {System.out.println("[X] Failed to create log files!");}
        }

        log("[+] Logger paths updated.", 1);
    }

    public static void log (String content, int sub) {
        Date date = new Date();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(date);
        String time = new SimpleDateFormat("HH:mm:ss").format(date);

        if (!log[0].getName().equals(today + ".log")) Load();

        String line = "[" + time + "] " + content;

        // Always print to systems logs
        System.out.println(line);
        try {
            FileWriter writer = new FileWriter(log[0], true);
            writer.write(line + "\n");
            writer.close();
        } catch (Exception e) { System.out.println("[X] Unable to write to sys log!\n" + e); }
        if (sub < 1) return;

        // Print to another requested log
        try {
            FileWriter writer = new FileWriter(log[sub], true);
            writer.write(line + "\n");
            writer.close();
        }
        catch (Exception e) {
            switch (sub) {
                case 1 -> System.out.println("[X] Unable to write to bot log!\n" + e);
                case 2 -> System.out.println("[X] Unable to write to mod log!\n" + e);
                case 3 -> System.out.println("[X] Unable to write to ticket log!\n" + e);
            }
        }
    }
}
