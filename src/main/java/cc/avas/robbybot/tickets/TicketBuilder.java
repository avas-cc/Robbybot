package cc.avas.robbybot.tickets;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.io.IOException;

public class TicketBuilder {
    final String[] cat = {"In Game", "Discord", "Other", "Private"};
    final String[] prio = {"QOL", "Low", "Medium", "High", "Immediate"};
    final String[] stat = {"New", "Scheduled", "In Progress", "Needs Update", "Completed", "Closed"};
    final int[] rem = {14, 7, 5, 3, 1};
    final String[] catEmoji = {"\uD83C\uDFAE", "\uD83D\uDCAC", "\uD83D\uDCDD", "\uD83D\uDEAB"};

    public static void SendModal (ButtonInteractionEvent event) throws IOException {
        int cat = Integer.parseInt(event.getButton().getId().split("_")[2]);

        TextInput[] opt = {
                TextInput.create("title", "Title:", TextInputStyle.SHORT).setPlaceholder("Brief description of the issue").setRequiredRange(1, 50).build(),
                TextInput.create("text0", "Reported by:", TextInputStyle.SHORT).setPlaceholder("User(s) who reported the issue").setRequired(false).build(),
                TextInput.create("text1", "Affected component/command", TextInputStyle.SHORT).setRequired(false).build(),
                TextInput.create("text2", "Existing since:", TextInputStyle.SHORT).setRequired(false).build(),
                TextInput.create("desc", "Description:", TextInputStyle.PARAGRAPH).setRequired(false).setMaxLength(500).build()
        };

        Modal modal = Modal.create("ticket-temp-" + cat, "Ticket Template")
                .addActionRows(ActionRow.of(opt[0]), ActionRow.of(opt[1]), ActionRow.of(opt[2]), ActionRow.of(opt[3]), ActionRow.of(opt[4]))
                .build();

        event.replyModal(modal).queue();
    }
}
