package cc.avas.robbybot.utils.listeners;

import cc.avas.robbybot.utils.handlers.ButtonHandler;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ButtonEvent extends ListenerAdapter {
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        ButtonHandler.Handle(event);
    }
}