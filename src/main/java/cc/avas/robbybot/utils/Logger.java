package cc.avas.robbybot.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    private static File sysLog, botLog, modLog, ticketLog;

    public static void Load () {
        if (!new File("./logs").exists()) new File("./logs").mkdir();
        if (!new File("./logs/sys").exists()) new File("./logs/sys").mkdir();
        if (!new File("./logs/bot").exists()) new File("./logs/bot").mkdir();
        if (!new File("./logs/mod").exists()) new File("./logs/mod").mkdir();
        if (!new File("./logs/tickets").exists()) new File("./logs/tickets").mkdir();

        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        sysLog = new File("./logs/sys/" + today + ".log");
        botLog = new File("./logs/bot/" + today + ".log");
        modLog = new File("./logs/mod/" + today + ".log");
        ticketLog = new File("./logs/tickets/" + today + ".log");

        try {
            if (!sysLog.exists()) sysLog.createNewFile();
            if (!botLog.exists()) botLog.createNewFile();
            if (!modLog.exists()) modLog.createNewFile();
            if (!ticketLog.exists()) ticketLog.createNewFile();
        } catch (Exception e) {System.out.println("[X] Failed to create log files!");}

        log("[+] Logger paths updated.", 1);
    }

    public static void log (String content, int sub) {
        Date today = new Date();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(today);
        String time = new SimpleDateFormat("HH:mm:ss").format(today);

        if (!sysLog.getName().equals(todayDate + ".log")) Load();

        String line = "[" + time + "] " + content;

        // Always print to systems logs
        System.out.println(line);
        try {
            //BufferedWriter writer = new BufferedWriter(new FileWriter(sysLog, true));
            FileWriter writer = new FileWriter(sysLog, true);
            writer.write(line + "\n");
            writer.close();
        } catch (Exception e) { System.out.println("[X] Unable to write to sys log!\n" + e); }

        // Print to another requested log
        switch (sub) {
            case 1 -> BotOut(line);
            case 2 -> ModerationOut(line);
            case 3 -> TicketOut(line);
        }
    }

    public static void BotOut (String content) {
        try {
            FileWriter writer = new FileWriter(botLog, true);
            writer.write(content + "\n");
            writer.close();
        }
        catch (Exception e) { System.out.println("[X] Unable to write to bot log!\n" + e); }
    }

    public static void ModerationOut (String content) {
        try {
            FileWriter writer = new FileWriter(modLog, true);
            writer.write(content + "\n");
            writer.close();
        }
        catch (Exception e) { System.out.println("[X] Unable to write to mod log!\n" + e); }
    }

    public static void TicketOut (String content) {
        try {
            FileWriter writer = new FileWriter(ticketLog, true);
            writer.write(content + "\n");
            writer.close();
        }
        catch (Exception e) { System.out.println("[X] Unable to write to ticket log!\n" + e); }
    }
}
