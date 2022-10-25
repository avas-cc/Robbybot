package cc.avas.robbybot.avas;

import cc.avas.robbybot.utils.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.awt.*;

public class AVASCommandHandler {
    public static void Stats (SlashCommandInteraction event) {
        String player = event.getOption("player-name").getAsString();
        String[] stats = new APIHandler().GetPublicPlayerStats(player);

        if (stats.length < 5) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] " + player)
                    .setDescription("Player not found.")
                    .setColor(Color.red);
            event.replyEmbeds(eb.build()).setEphemeral(true).queue();
            return;
        }

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[AVAS] " + stats[5].split(":")[1] + ". " + stats[2].split(":")[1].replaceAll("\"", ""))
                .addField("Hours played:", String.valueOf((Integer.parseInt(stats[7].split(":")[1])/60)/60), true);
        if (stats[1].split(":")[1].equals("true")) {
            eb.addField("Donor:", "True", true);
            eb.setColor(Color.ORANGE);
        }
        else if (Integer.parseInt(stats[5].split(":")[1]) % 2 == 0) eb.setColor(Color.MAGENTA);
        else eb.setColor(Color.CYAN);

        event.replyEmbeds(eb.build()).queue();
    }
}
