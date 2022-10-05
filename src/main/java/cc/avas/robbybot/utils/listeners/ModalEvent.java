package cc.avas.robbybot.utils.listeners;

import cc.avas.robbybot.utils.handlers.ModalHandler;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ModalEvent extends ListenerAdapter {
    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        ModalHandler.Handle(event);
    }
}