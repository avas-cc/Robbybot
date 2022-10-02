package cc.avas.robbybot.handlers;

import cc.avas.robbybot.tickets.TicketHandler;
import cc.avas.robbybot.utils.EmbedUtil;
import cc.avas.robbybot.utils.Logger;
import cc.avas.robbybot.utils.data.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class CommandHandler {
    static InteractionHandler i = new InteractionHandler();
    public static void Handle(SlashCommandInteraction event) throws IOException, ExecutionException, InterruptedException {
        switch (event.getName()) {
            //Admin commands
            case "ticket" -> {
                if (!i.CheckPermission(event, 2)) return;
                switch (event.getSubcommandName()) {
                    case "start" -> TicketHandler.Start(event);
//                    case "title" -> TicketHandler.SetTitle(event);
//                    case "claim" -> TicketHandler.Claim(event);
//                    case "assign" -> TicketHandler.Assign(event);
//                    case "add" -> TicketHandler.AddUser(event);
//                    case "remove" -> TicketHandler.RemoveUser(event);
                }
            }
            case "config" -> {
                if (!i.CheckPermission(event, 2)) return;
                switch (event.getSubcommandName()) {
                    case "guild" -> {}
                    case "mod-roles" -> HandleMod(event);
                }
            }
        }
    }

    // Config commands
    static void HandleMod (SlashCommandInteraction event) {
        Channel eventChannel = event.getChannel();
        Role addRole = null;
        Role removeRole = null;
        boolean list = false;
        try { addRole = event.getOption("add").getAsRole(); } catch (Exception ignored) {}
        try { removeRole = event.getOption("remove").getAsRole(); } catch (Exception ignored) {}
        try { list = event.getOption("list").getAsBoolean(); } catch (Exception ignored) {}

        if (addRole != null) {
            ArrayList<Role> modRoles = Data.GetModRoles(event.getJDA());
            if (modRoles.contains(addRole)) {
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("[RB] Admin")
                        .setDescription("Role **@" + addRole.getName() + "** is already added to the moderator role list");
                new EmbedUtil().SendEmbed(event.getTextChannel(), eb, true);
            } else {
                modRoles.add(addRole);
                Data.SetModRole(modRoles);
            }
        }

        if (removeRole != null) {
            ArrayList<Role> modRoles = Data.GetModRoles(event.getJDA());
            if (modRoles.contains(removeRole)) {
                modRoles.remove(removeRole);
                Data.SetModRole(modRoles);
            } else {
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("[RB] Admin")
                        .setDescription("Role **@" + removeRole.getName() + "** is not added to the moderator role list");
                new EmbedUtil().SendEmbed(event.getTextChannel(), eb, true);
            }
        }

        if (list) {
            String blob = "";
            for (Role role : Data.GetModRoles(event.getJDA())) blob += role.getName() + "\n";
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Admin")
                    .setDescription("**Moderator roles:**\n" + blob);
            new EmbedUtil().ReplyEmbed(event, eb, true, false);
        } else {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("[RB] Admin")
                    .setDescription("Updated moderator roles.");
            new EmbedUtil().ReplyEmbed(event, eb, true, false);

        }
    }

}
