package cc.avas.robbybot.utils.listeners;

import cc.avas.robbybot.moderation.MuteHandler;
import cc.avas.robbybot.utils.handlers.CommandHandler;
import cc.avas.robbybot.utils.data.Data;
import cc.avas.robbybot.utils.Logger;
import cc.avas.robbybot.utils.data.SQL;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class BotEvent extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Logger.Load();
        Logger.log("[+] Logger loaded.", 1);

        new Data().Load();
        new SQL().Load();
        MuteHandler.Load(event.getJDA());
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        if(event.getJDA().getGuilds().toArray().length > 1) {
            CommandHandler.RegisterCommands(event.getJDA());
        }
    }
}
