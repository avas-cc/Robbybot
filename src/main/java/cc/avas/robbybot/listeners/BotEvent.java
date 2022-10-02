package cc.avas.robbybot.listeners;

import cc.avas.robbybot.utils.Data;
import cc.avas.robbybot.utils.Logger;
import cc.avas.robbybot.utils.SQL;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class BotEvent extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Logger.Load();
        Logger.log("[+] Logger loaded.", 1);

        new Data().Load();
        new SQL().Load();
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {

    }
}
