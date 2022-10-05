package cc.avas.robbybot.utils.handlers;

import cc.avas.robbybot.tickets.TicketBuilder;
import cc.avas.robbybot.tickets.TicketHandler;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class ButtonHandler {
    public static void Handle (ButtonInteractionEvent event) {
        String[] id = event.getButton().getId().split("-");
        switch (id[0]) {
            case "ticket" -> {
                switch (id[1]) {
                    case "start" -> new TicketBuilder().SendModal(event, id[2]);
                    case "prio" -> TicketHandler.UpdatePriority(event, Integer.parseInt(id[3]), Integer.parseInt(id[2]));
                    case "stat" -> TicketHandler.UpdateStatus(event, Integer.parseInt(id[3]), Integer.parseInt(id[2]));
                    case "close" -> TicketHandler.CloseTicket(event, Integer.parseInt(id[3]), Integer.parseInt(id[2]));
                }
            }
        }
    }
}
