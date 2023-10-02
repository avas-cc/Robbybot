package cc.avas.robbybot.events.mapart;

import cc.avas.robbybot.events.EventManager;
import cc.avas.robbybot.events.EventManager.Event;
import cc.avas.robbybot.events.VoteManager;
import cc.avas.robbybot.events.VoteManager.Vote;
import cc.avas.robbybot.utils.EmbedUtil;
import cc.avas.robbybot.utils.data.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.w3c.dom.Text;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class MACManager {

    static List<Entry> entries = new ArrayList<>();

    static class Entry {
        long discordID;
        long messageID;
        String title;
        String imageURL;
        int votes;

        public Entry(long discordID, long messageID, String title, String imageURL) {
            this.discordID = discordID;
            this.messageID = messageID;
            this.title = title;
            this.imageURL = imageURL;
            this.votes = 0;
        }

        public long getDiscordID() { return discordID; }

        public long getMessageID() { return messageID; }

        public String getTitle() { return title; }

        public String getImageURL() { return imageURL; }

        public int getVotes() { return votes; }

        public void addVote() { votes++; }

        public void removeVote() { votes--; }
    }

    public static void startEvent(SlashCommandInteraction event, int duration) {
        // Get event info
        Modal modal = Modal.create("events-1-start-" + duration, "Mapart Contest Creation")
                .addActionRow(TextInput.create("theme", "Theme", TextInputStyle.SHORT).setRequired(true).build())
                .addActionRow(TextInput.create("rules", "Rules", TextInputStyle.PARAGRAPH).setRequired(true).build())
                .build();

        event.replyModal(modal).queue();
    }

    public static void startEvent2(ModalInteractionEvent event) {
        JDA jda = event.getJDA();

        int eventID = Integer.parseInt(event.getModalId().split("-")[1]);
        int duration = Integer.parseInt(event.getModalId().split("-")[3]);

        Event newEvent = new Event(eventID, duration);
        EventManager.runningEvent = newEvent;

        // Private submissions channel
        Guild guild = Data.getGuild(jda);
        TextChannel submissionChannel = Data.getSubmissionsChannel(jda);
        submissionChannel.upsertPermissionOverride(guild.getPublicRole())
            .setDenied(Permission.VIEW_CHANNEL)
                .queue();
        for(Role role : Data.getModRoles(jda)) {
            submissionChannel.upsertPermissionOverride(role)
                .setAllowed(Permission.VIEW_CHANNEL)
                    .queue();
        }

        TextChannel eventsChannel = Data.getEventChannel(jda);

        // Post instructions embed to events channel
        String theme = event.getInteraction().getValue("theme").getAsString();
        String rules = event.getInteraction().getValue("rules").getAsString();
        long end = newEvent.getEnd();

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] Mapart Contest")
                .setDescription("Starting event! This might take a second..");
        new EmbedUtil().ReplyEmbed(event, eb, true, false);

        buildEventInfoEmbed(eventsChannel, end, theme, rules);
    }

    static void clearEventsChannel (TextChannel eventsChannel) throws RuntimeException {
        CountDownLatch latch = new CountDownLatch(1);
        while (true) {
            try {
                if (eventsChannel.getHistoryFromBeginning(7).submit().get().size() == 0) break;
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

            eventsChannel.getHistory().retrievePast(1).queue(messages -> {
                int messageCount = messages.size();
                CountDownLatch deleteLatch = new CountDownLatch(messageCount);

                for (Message message : messages) {
                    message.delete().queue(success -> deleteLatch.countDown(), throwable -> deleteLatch.countDown());
                }

                try {
                    deleteLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            });
        }

        latch.countDown();
        try { latch.await(); }
        catch (InterruptedException e) { e.printStackTrace(); }
    }

    static void buildEventInfoEmbed(TextChannel eventsChannel, long end, String theme, String rules) {
        clearEventsChannel(eventsChannel);

        Event event = EventManager.runningEvent;

        long now = System.currentTimeMillis();
        String dur;
        if (event.getStart() == event.getEnd()) dur = "Indefinite";
        else {
            long delta = (end - now);

            if (delta <= 0) {
                startVoting(eventsChannel.getJDA());
                return;
            }

            dur = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toHours(delta), TimeUnit.MILLISECONDS.toMinutes(delta) % 60);
        }

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] Mapart Contest Information")
                .setDescription(String.format("**Theme:** %s\n**Time Remaining:** %s\n\n**Submission Rules:**\n%s\n\n**Voting Rules:**\n- 1 vote per user (can be changed at any time)\n- Must have joined the Discord before event announcement\nVote in #submissions when voting is opened!\n\n**How to submit:**\nUse **/submit**, include a screenshot and a title!", theme, dur, rules));

        MessageAction action = eventsChannel.sendMessageEmbeds(eb.build());
        action.submit().whenComplete((v, error) -> {
            if (error != null) error.printStackTrace();
        });

        if (!dur.equals("Indefinite")) {
            FutureTask<Void> timeRemainingTask = new FutureTask<>(() -> eventTimeRemainingTimer(eventsChannel, end, theme, rules), null);
            new Thread(timeRemainingTask).start();
        }
    }

    static Timer infoEmbedUpdateTimer = new Timer();
    static void eventTimeRemainingTimer(TextChannel eventsChannel, long end, String theme, String rules) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                buildEventInfoEmbed(eventsChannel, end, theme, rules);
            }
        };
        infoEmbedUpdateTimer.schedule(task, 60000L);
    }

    public static void addEntry (SlashCommandInteraction event) {
        if (EventManager.runningEvent.getVoting()) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Mapart Contest")
                    .setDescription("Voting has already started, so you can't submit a new piece now! ");
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
            return;
        }

        long discordID = event.getUser().getIdLong();
        Entry entry = getEntryByUser(discordID);
        if (entry != null) {
            EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] Mapart Contest")
                .setDescription(String.format("You've already submitted a mapart! (%s)", entry.getTitle()));
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
            return;
        }

        String title = event.getOption("title").getAsString();
        String imageURL = event.getOption("attachment").getAsAttachment().getUrl();

        TextChannel submissionsChannel = Data.getSubmissionsChannel(event.getJDA());

        EmbedBuilder eb = new EmbedBuilder()
            .setTitle(title)
            .setImage(imageURL);
        MessageAction action = submissionsChannel.sendMessageEmbeds(eb.build());
        try {
            Message message = action.submit().get();
            entries.add(new Entry(discordID, message.getIdLong(), title, imageURL));

            Button voteButton = Button.secondary("events-1-vote-" + message.getId(), String.format("Vote for %s", title));
            submissionsChannel.sendMessage(new MessageBuilder().setContent(" ").setActionRows(ActionRow.of(voteButton)).build()).queue();
            EmbedBuilder _eb = new EmbedBuilder()
                .setTitle("[RB] Mapart Contest")
                .setDescription(String.format("Successfully submitted %s to the mapart contest!", title));
            new EmbedUtil().ReplyEmbed(event, _eb, true, false);
        } catch (InterruptedException | ExecutionException e) { throw new RuntimeException(e); }
    }

    public static void removeEntry (MessageReactionAddEvent event) {
        long messageID = event.getMessageIdLong();

        Entry entry = getEntryByMessage(messageID);
        entries.remove(entry);

        Data.getSubmissionsChannel(event.getJDA()).deleteMessageById(messageID).queue();
        VoteManager.removeVotes(messageID);
    }

    public static void vote (ButtonInteractionEvent event) {
        // if too new fuckem
        long joinTime = event.getMember().getTimeJoined().toInstant().toEpochMilli();
        if (joinTime > EventManager.runningEvent.getStart()) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Mapart Contest")
                    .setDescription("You can only vote if you joined after the event was announced!");
            new EmbedUtil().ReplyEmbed(event, eb, true, true);
            return;
        }

        long discordID = event.getUser().getIdLong();
        long messageID = Long.parseLong(event.getButton().getId().split("-")[3]);
        Entry entry = getEntryByMessage(messageID);

        //if vote already, change vote
        Vote check = VoteManager.getVote(discordID);
        if (check != null) {
            if (check.getMessageID() == messageID) {
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("[RB] Mapart Contest")
                        .setDescription("You already voted for this entry!");
                new EmbedUtil().ReplyEmbed(event, eb, true, false);
                return;
            }

            Vote voteObj = VoteManager.getVote(discordID);

            long oldMessageID = voteObj.getMessageID();
            Entry oldEntry = getEntryByMessage(oldMessageID);
            oldEntry.removeVote();

            voteObj.setMessageID(messageID);

            entry.addVote();
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Mapart Contest")
                    .setDescription(String.format("Changed vote from **%s** to **%s**!", oldEntry.getTitle(), entry.getTitle()));
            new EmbedUtil().ReplyEmbed(event, eb, true, false);
            return;
        }

        //else confirm first vote
        VoteManager.addVote(event.getUser().getIdLong(), messageID);
        entry.addVote();
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] Mapart Contest")
                .setDescription(String.format("Voted for **%s**!", entry.getTitle()));
        new EmbedUtil().ReplyEmbed(event, eb, true, false);
    }

    public static void startVoting(JDA jda) {
        EventManager.runningEvent.setVoting(true);
        TextChannel eventsChannel = Data.getEventChannel(jda);

        clearEventsChannel(eventsChannel);

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] Mapart Contest Information")
                .setDescription("**Voting has started!** Cast your vote in #submissions!\n\n**Voting Rules**:\n- 1 vote per user (can be changed at any time)\n- Must have joined before the event was announced\n\nThe voting period is 24 hours, after which the winners will be announced.\n\n**NOTE:** If you can't see the submissions, try restarting Discord sorry this platforms so shit");

        MessageAction action = eventsChannel.sendMessageEmbeds(eb.build());
        action.submit().whenComplete((v, error) -> {
            if (error != null) error.printStackTrace();
        });

        TextChannel submissionsChannel = Data.getSubmissionsChannel(jda);
        Guild guild = Data.getGuild(jda);
        submissionsChannel.upsertPermissionOverride(guild.getPublicRole())
                .setAllowed(Permission.VIEW_CHANNEL)
                .setAllowed(Permission.MESSAGE_HISTORY)
                .queue();

        FutureTask<Void> task = new FutureTask<>(() -> votePeriodTimer(jda), null);
        new Thread(task).start();
    }

    static Timer votePeriodTimer = new Timer();
    static void votePeriodTimer(JDA jda) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    stopEvent(jda);
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        votePeriodTimer.schedule(task, 86400000L); //86,400,000 = 1d
    }

    public static void stopEvent(JDA jda) throws ExecutionException, InterruptedException {
        EventManager.runningEvent = null;

        // get winners
        entries.sort((o1, o2) -> Integer.compare(o2.getVotes(), o1.getVotes()));
        List<Entry> winners = new ArrayList<>(entries.subList(0, Math.min(3, entries.size())));

        // dump entries into #maparts
        TextChannel mapartChannel = Data.getMapartChannel(jda);
        entries.removeAll(winners);
        for (Entry entry : entries) {
            mapartChannel.sendMessage(entry.getTitle() + " by " + jda.getUserById(entry.getDiscordID()).getAsMention()).queue();
            mapartChannel.sendMessage(entry.getImageURL());
        }

        List<String> imageURLS = new ArrayList<>();
        for (Entry entry : winners) {
            String title = entry.getTitle();
            String imageURL = entry.getImageURL();
            long discordID = entry.getDiscordID();

            mapartChannel.sendMessage(title + " by " + jda.getUserById(discordID).getAsMention()).queue();
            MessageAction action = mapartChannel.sendMessage(imageURL);

            String messageID = action.submit().get().getId();
            imageURLS.add(String.format("https://discord.com/channels/%s/%s/%s", mapartChannel.getGuild().getId(), mapartChannel.getId(), messageID));
        }

        TextChannel leaderboardsChannel = Data.getLeaderboardsChannel(jda);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yy");
        StringBuilder blob = new StringBuilder();
        int i = 1;
        for (Entry winner : winners) {
            blob.append(String.format("%d. %s %s - %s votes\n", i, jda.getUserById(winner.getDiscordID()).getAsMention(), imageURLS.get(i-1), winner.getVotes()));
            i++;
        }

        leaderboardsChannel.sendMessage("**Mapart Contest Winners** - "  + LocalDate.now().format(formatter)).queue();
        leaderboardsChannel.sendMessage(blob.toString()).queue();

        // announce winners in events channel
        TextChannel eventsChannel = Data.getEventChannel(jda);

        clearEventsChannel(eventsChannel);
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] Mapart Contest Winners")
                .setDescription("Congratulations to the following players!");

        MessageAction action = eventsChannel.sendMessageEmbeds(eb.build());
        action.submit().whenComplete((v, error) -> {
            if (error != null) error.printStackTrace();
        });

        i = 1;
        for (Entry winner : winners) {
            eventsChannel.sendMessage(String.format("%d. **%s** by ", i, winner.getTitle()) + jda.getUserById(winner.getDiscordID()).getAsMention() + " - " + winner.getVotes() + " votes").queue();
            eventsChannel.sendMessage(winner.getImageURL()).queue();
            i++;
        }
    }

    static Entry getEntryByMessage(long messageID) {
        for (Entry entry : entries) {
            if (entry.getMessageID() == messageID) return entry;
        }
        return null;
    }

    static Entry getEntryByUser(long discordID) {
        for (Entry entry : entries) {
            if (entry.getDiscordID() == discordID) return entry;
        }
        return null;
    }
}
