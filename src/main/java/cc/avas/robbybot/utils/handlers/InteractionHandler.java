package cc.avas.robbybot.utils.handlers;

import cc.avas.robbybot.utils.EmbedUtil;
import cc.avas.robbybot.utils.data.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.List;

public class InteractionHandler {
    public boolean CheckPermission(SlashCommandInteraction event, int score) {
        Member member = event.getMember();
        if(member == null) return false;

        switch(score) {
            case 1:
                if (member.hasPermission(Permission.ADMINISTRATOR)) return true;
                List<Role> modRoles = Data.GetModRoles(event.getJDA());
                for(Role role : member.getRoles()) {
                    if(modRoles.contains(role)) return true;
                }
            case 2: if (member.hasPermission(Permission.ADMINISTRATOR)) return true;
        }

        EmbedUtil embedUtil = new EmbedUtil();
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("[RB] You don't have permission lol")
                .setDescription(" lol");
        embedUtil.ReplyEmbed(event, eb, true, true);
        return false;
    }
}
