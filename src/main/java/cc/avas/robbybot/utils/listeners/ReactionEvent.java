package cc.avas.robbybot.utils.listeners;

import cc.avas.robbybot.events.EventManager;
import cc.avas.robbybot.utils.data.Data;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ReactionEvent extends ListenerAdapter {
    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getTextChannel() == Data.getSubmissionsChannel(event.getJDA())) EventManager.handleReactionInteraction(event);
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {

    }
}
