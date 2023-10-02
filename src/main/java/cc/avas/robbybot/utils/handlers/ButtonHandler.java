package cc.avas.robbybot.utils.handlers;

import cc.avas.robbybot.events.EventManager;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class ButtonHandler {
    public static void Handle (ButtonInteractionEvent event) {
        String[] id = event.getButton().getId().split("-");
        switch (id[0]) {
            case "events" -> EventManager.handleButtonInteraction(event);
        }
    }
}
