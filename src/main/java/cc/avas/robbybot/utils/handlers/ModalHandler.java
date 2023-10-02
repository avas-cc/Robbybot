package cc.avas.robbybot.utils.handlers;

import cc.avas.robbybot.events.EventManager;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public class ModalHandler {
    public static void handle(ModalInteractionEvent event) {
        String[] modalData = event.getModalId().split("-");
        switch (modalData[0]) {
            case "events" -> EventManager.handleModalInteraction(event);
        }
    }
}
