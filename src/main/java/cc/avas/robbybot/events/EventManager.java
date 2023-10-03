package cc.avas.robbybot.events;

import cc.avas.robbybot.events.mapart.MACManager;
import cc.avas.robbybot.utils.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Map;

public class EventManager {
    public static Event runningEvent = null;
    static Map<String, Integer> eventIDs = Map.of(
    "mapart contest", 1
    );

    public static class Event {
        int event;
        long start;
        long end;
        boolean voting;

        public Event(int event, long endTimestamp) {
            this.event = event;
            this.end = endTimestamp;
            this.voting = false;
            this.start = Instant.now().atZone(ZoneId.of("America/Chicago")).toInstant().toEpochMilli();
        }

        public int getEvent() { return this.event; }

        public long getStart() { return this.start; }

        public long getEnd() { return this.end; }

        public boolean getVoting() { return this.voting; }
        public void setVoting(boolean status) { this.voting = status; }
    }

    public static void handle(SlashCommandInteraction event) {
        switch (event.getOption("action").getAsString()) {
            case "start" -> {
                if (event.getOption("event").getAsString().equals("voting")) {
                    if (runningEvent == null) {
                        EmbedBuilder eb = new EmbedBuilder()
                                .setTitle("[RB] Event Manager")
                                .setDescription("No event is already running!");
                        new EmbedUtil().ReplyEmbed(event, eb, true, true);
                        return;
                    }
                    startVoting(event);
                    EmbedBuilder eb = new EmbedBuilder()
                            .setTitle("[RB] Event Manager")
                            .setDescription("Started the voting period!");
                    new EmbedUtil().ReplyEmbed(event, eb, true, false);
                    return;
                }

                if (runningEvent != null) {
                    EmbedBuilder eb = new EmbedBuilder()
                            .setTitle("[RB] Event Manager")
                            .setDescription("An event is already running! Please stop it before starting a new one!");
                    new EmbedUtil().ReplyEmbed(event, eb, true, true);
                    return;
                }

                String eventName;

                try { eventName = event.getOption("event").getAsString(); }
                catch (Exception e) {
                    EmbedBuilder eb = new EmbedBuilder()
                            .setTitle("[RB] Event Manager")
                            .setDescription("Failed to set [event]!");
                    new EmbedUtil().ReplyEmbed(event, eb, true, true);
                    return;
                }

                long endTimestamp = 0L;
                try {
                    String end = event.getOption("end-time").getAsString();

                    // Define the DateTimeFormatter with the correct pattern and set the year to the current year
                    DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                            .appendPattern("MM-dd HH:mm")
                            .parseDefaulting(ChronoField.YEAR, Year.now().getValue())
                            .toFormatter();

                    // Parse the datetime string to LocalDateTime
                    LocalDateTime dateTime = LocalDateTime.parse(end, formatter);

                    // Specify the time zone as Central Standard Time (CST)
                    ZoneId zoneId = ZoneId.of("America/Chicago");

                    // Convert LocalDateTime to ZonedDateTime with the specified time zone
                    ZonedDateTime zonedDateTime = dateTime.atZone(zoneId);

                    endTimestamp = zonedDateTime.toInstant().toEpochMilli();
                }
                catch (Exception e) {
                    System.out.println(e);
                }

                startEvent(event, eventIDs.get(eventName), endTimestamp);
            }

            case "stop" -> stopEvent(event);
        }
    }

    public static void startEvent(SlashCommandInteraction event, int eventID, long endTimestamp) {
        switch (eventID) {
            case 1 -> MACManager.startEvent(event, endTimestamp);
        }
    }

    public static void startVoting(SlashCommandInteraction event) {
        switch (runningEvent.getEvent()) {
            case 1 -> MACManager.startVoting(event.getJDA());
        }
    }

    public static void stopEvent(SlashCommandInteraction event) {
        if (runningEvent == null) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Event Manager")
                    .setDescription("No event running!");
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
            return;
        }

        switch (runningEvent.getEvent()) {
            case 1 -> {
                try {
                    EmbedBuilder eb = new EmbedBuilder()
                            .setTitle("[RB] Event Manager")
                            .setDescription("Stopping event - might take a minute..");
                    new EmbedUtil().ReplyEmbed(event, eb, true, true);
                    MACManager.stopEvent(event.getJDA());
                }
                catch (Exception e) {
                    EmbedBuilder eb = new EmbedBuilder()
                            .setTitle("[RB] Event Manager")
                            .setDescription("Failed to stop event - check logs for error :(");
                    new EmbedUtil().ReplyEmbed(event, eb, true, true);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void submit(SlashCommandInteraction event) {
        switch (runningEvent.getEvent()) {
            case 1 -> MACManager.addEntry(event);
        }
    }

    public static void handleModalInteraction(ModalInteractionEvent event) {
        String[] modalData = event.getModalId().split("-");
        switch (modalData[1]) {
            case "1" -> { // Mapart contest
                switch (modalData[2]) {
                    case "start" -> MACManager.startEvent2(event);
                }
            }
        }
    }

    public static void handleReactionInteraction(MessageReactionAddEvent event) {
        switch (runningEvent.getEvent()) {
            case 1 -> MACManager.removeEntry(event);
        }
    }

    public static void handleButtonInteraction(ButtonInteractionEvent event) {
        switch (runningEvent.getEvent()) {
            case 1 -> MACManager.vote(event);
        }
    }
}
