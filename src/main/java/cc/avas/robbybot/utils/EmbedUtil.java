package cc.avas.robbybot.utils;

import cc.avas.robbybot.utils.data.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class EmbedUtil {
    final Color[] color = {Color.CYAN, Color.MAGENTA};

    public void ReplyEmbed (SlashCommandInteraction event, EmbedBuilder eb, boolean eph, boolean error) {
        if(error) eb.setColor(Color.RED);
        else eb.setColor(color[ThreadLocalRandom.current().nextInt(0, color.length)]);

        if(eph) event.replyEmbeds(eb.build()).setEphemeral(true).queue();
        else event.replyEmbeds(eb.build()).queue();
    }

    public void ReplyEmbed (ButtonInteraction event, EmbedBuilder eb, boolean eph, boolean error) {
        if(error) eb.setColor(Color.RED);
        else eb.setColor(color[ThreadLocalRandom.current().nextInt(0, color.length)]);

        if(eph) event.replyEmbeds(eb.build()).setEphemeral(true).queue();
        else event.replyEmbeds(eb.build()).queue();
    }

    public void ReplyEmbed (ModalInteractionEvent event, EmbedBuilder eb, boolean eph, boolean error) {
        if(error) eb.setColor(Color.RED);
        else eb.setColor(color[ThreadLocalRandom.current().nextInt(0, color.length)]);

        if(eph) event.replyEmbeds(eb.build()).setEphemeral(true).queue();
        else event.replyEmbeds(eb.build()).queue();
    }

    public void SendEmbed (TextChannel channel, EmbedBuilder eb, boolean error) {
        if(error) eb.setColor(Color.RED);
        else eb.setColor(color[ThreadLocalRandom.current().nextInt(0, color.length)]);

        channel.sendMessageEmbeds(eb.build()).queue();
    }

    public void ModLogEmbed (JDA jda, EmbedBuilder eb, boolean good) {
        if(!good) eb.setColor(Color.RED);
        else eb.setColor(Color.GREEN);
        try { Data.GetModLogChannel(jda).sendMessageEmbeds(eb.build()).queue(); } catch (Exception ignored) {}
    }
}