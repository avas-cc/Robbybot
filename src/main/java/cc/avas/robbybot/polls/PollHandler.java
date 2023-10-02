package cc.avas.robbybot.polls;

import cc.avas.robbybot.utils.EmbedUtil;
import cc.avas.robbybot.utils.data.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.util.concurrent.ExecutionException;

public class PollHandler {
    static Emoji[] reaction = {
            Emoji.fromUnicode("U+1F1E6"),
            Emoji.fromUnicode("U+1F1E7"),
            Emoji.fromUnicode("U+1F1E8"),
            Emoji.fromUnicode("U+1F1E9"),
            Emoji.fromUnicode("U+1F1EA"),
            Emoji.fromUnicode("U+1F1EB"),
            Emoji.fromUnicode("U+1F1EC"),
            Emoji.fromUnicode("U+1F1ED"),
            Emoji.fromUnicode("U+1F1EE"),
            Emoji.fromUnicode("U+1F1EF")
    };


    public static void Handle(SlashCommandInteraction event) throws ExecutionException, InterruptedException {
        // Check for role and channel
        TextChannel pollChannel = null;
        try { pollChannel = Data.getPollChannel(event.getJDA()); } catch (Exception ignored) {}
        Role pollRole = null;
        try { pollRole = Data.getPollRole(event.getJDA()); } catch (Exception ignored) {}

        EmbedUtil embedUtil = new EmbedUtil();
        if(pollChannel == null) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Polls")
                    .setDescription("Poll channel not set! Use `/config poll` to set it.");
            embedUtil.ReplyEmbed(event, eb, true, true);
            return;
        }
        if(pollRole == null) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Polls")
                    .setDescription("Poll role not set! Use `/config poll` to set it.");
            embedUtil.ReplyEmbed(event, eb, true, true);
            return;
        }

        // Check for "Yes/No" poll
        String question = event.getOption("question").getAsString();
        String[] responses = {"Yes", "No"};
        if(event.getOptions().size() == 1) {
            Message pollEmbed = pollChannel.sendMessageEmbeds(new EmbedBuilder().setTitle(question).build()).submit().get();
            pollEmbed.addReaction(Emoji.fromUnicode("U+2705")).queue();
            pollEmbed.addReaction(Emoji.fromUnicode("U+274E")).queue();
            Message ping = pollChannel.sendMessage(pollRole.getAsMention()).submit().get();
            ping.delete().queue();
            embedUtil.ReplyEmbed(event, new EmbedBuilder().setTitle("[RB] Polls").setDescription("Poll created!"), true, false);
            return;
        }

        // Multiple choice poll
        // Get responses
        responses = event.getOption("responses").getAsString().split(";");
        if(responses.length > 10) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Polls")
                    .setDescription("Maximum 10 responses allowed!");
            embedUtil.ReplyEmbed(event, eb, true, true);
            return;
        }

        // Format response key
        String blob = "";
        for(int i = 0; i < responses.length; i++) blob += reaction[i].getFormatted() + " - " + responses[i] + "\n";

        // Send the embed + reactions, and then ghost ping the poll role
        Message pollEmbed = pollChannel.sendMessageEmbeds(new EmbedBuilder().setTitle(question).setDescription(blob).build()).submit().get();
        for(int i = 0; i < responses.length; i++) pollEmbed.addReaction(reaction[i]).queue();
        Message ping = pollChannel.sendMessage(pollRole.getAsMention()).submit().get();
        ping.delete().queue();

        // Respond to event
        embedUtil.ReplyEmbed(event, new EmbedBuilder().setTitle("[RB] Polls").setDescription("Poll created!"), true, false);
    }
}
