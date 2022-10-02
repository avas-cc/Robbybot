package cc.avas.robbybot.handlers;

import cc.avas.robbybot.utils.EmbedUtil;
import cc.avas.robbybot.utils.Logger;
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

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InteractionHandler {
    public static void RegisterCommands(JDA jda) {
        List<CommandData> commandData = new ArrayList<>();
        //Admin commands

        //config
        commandData.add(Commands.slash("config", "Configure channels, roles and stuff for the bot")
                .addSubcommands(new SubcommandData("guild", "Set public and private guilds")
                        .addOptions(new OptionData(OptionType.STRING, "set", "Set public/private guild")
                                .addChoice("public", "public")
                                .addChoice("private", "private")))
                .addSubcommands(new SubcommandData("mod-roles", "Configure mod stuff")
                        .addOption(OptionType.ROLE, "add", "Add role to the mod role list")
                        .addOption(OptionType.ROLE, "remove", "Remove role from the mod role list")
                        .addOption(OptionType.BOOLEAN, "list", "List moderator roles")));

        //ticket
        commandData.add(Commands.slash("ticket", "Start ticket service in current channel")
                .addSubcommands(new SubcommandData("start", "Start ticket service in current channel"))
                .addSubcommands(new SubcommandData("title", "Brief description of the ticket")
                        .addOption(OptionType.STRING, "title", "Brief description of the ticket", true))
                .addSubcommands(new SubcommandData("assign", "Assign ticket to a user")
                        .addOption(OptionType.USER, "user", "User to assign to the ticket", true))
                .addSubcommands(new SubcommandData("claim", "Assign ticket to yourself"))
                .addSubcommands(new SubcommandData("add", "Add user to the ticket")
                        .addOption(OptionType.USER, "user", "Add user to the ticket", true))
                .addSubcommands(new SubcommandData("remove", "Remove user from the ticket")
                        .addOption(OptionType.USER, "user", "Remove user from the ticket", true)));

        jda.updateCommands().addCommands(commandData).queue();
        System.out.println("[Robbybot] [+] Registered " + commandData.toArray().length + " commands to " + jda.getGuilds());
    }

    public boolean CheckPermission(SlashCommandInteraction event, int score) throws IOException {
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
