package com.marcpg.botpg2;

import com.marcpg.text.Formatter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Tickets extends ListenerAdapter {
    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("application-role")) {
            String role = event.getValues().getFirst();

            Modal.Builder modalBuilder = Modal.create("application-" + role, "Application for " + Formatter.toPascalCase(role.replace("-", " ")));

            modalBuilder.addActionRow(TextInput.create("name", "Name", TextInputStyle.SHORT)
                    .setPlaceholder("A name or nickname to call you.")
                    .setRequired(true)
                    .build());
            modalBuilder.addActionRow(TextInput.create("age", "Age", TextInputStyle.SHORT)
                    .setPlaceholder("Your age, if comfortable sharing.")
                    .setMaxLength(3)
                    .setRequired(false)
                    .build());
            modalBuilder.addActionRow(TextInput.create("timezone", "Time-Zone", TextInputStyle.SHORT)
                    .setPlaceholder("Your time-zone, if comfortable sharing.")
                    .setRequired(false)
                    .build());
            modalBuilder.addActionRow(TextInput.create("experience", "Past Experience?", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Your past experiences in this job.")
                    .setRequired(false)
                    .build());
            modalBuilder.addActionRow(TextInput.create("why", "Why you?", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Tell us, why we should accept you.")
                    .setRequired(true)
                    .build());

            event.replyModal(modalBuilder.build()).queue();
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String button = event.getComponentId();
        switch (button) {
            case "open-ticket" -> {
                Modal.Builder modalBuilder = Modal.create("ticket", "Create a Ticket");

                modalBuilder.addActionRow(TextInput.create("subject", "Subject", TextInputStyle.SHORT)
                        .setPlaceholder("Subject of the ticket")
                        .setRequired(true)
                        .build());
                modalBuilder.addActionRow(TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("Your concerns/questions go here")
                        .setRequired(true)
                        .build());
                event.replyModal(modalBuilder.build()).queue();
            }
            case "delete-ticket" -> {
                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle("Delete this Ticket?")
                        .setDescription("This cannot be undone and scripts are currently not saved yet!")
                        .setColor(Color.RED);
                event.replyEmbeds(builder.build())
                        .addActionRow(
                                Button.danger("confirm-delete-ticket", "Delete Ticket"),
                                Button.secondary("cancel-delete-ticket", "Cancel")
                        )
                        .setEphemeral(true)
                        .queue();
            }
            case "confirm-delete-ticket" -> {
                event.reply("Deleting this ticket now!").queue();
                Executors.newScheduledThreadPool(1).schedule(() -> event.getChannel().delete().queue(), 3, TimeUnit.SECONDS);
            }
            case "cancel-delete-ticket" -> event.getMessage().delete().queue();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild());

        if (event.getModalId().startsWith("application")) {
            String role = event.getModalId().replace("application-", "");
            ModalMapping age = event.getValue("age");
            ModalMapping timezone = event.getValue("timezone");

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle(event.getMember().getEffectiveName() + "'s " + Formatter.toPascalCase(role.replace("-", " ")) + " Application")
                    .setDescription(event.getMember().getEffectiveName() + " applied for " + Formatter.toPascalCase(role.replace("-", " ")) + " using a ticket!")
                    .setColor(Color.decode("#2B2D31"))
                    .addField("(Nick-)Name", event.getValue("name").getAsString(), true)
                    .addField("Age", age != null ? age.getAsString() : "/", true)
                    .addField("Time-Zone", timezone != null ? timezone.getAsString() : "/", true)
                    .addField("Experience", event.getValue("experience").getAsString(), false)
                    .addField("Why you?", event.getValue("why").getAsString(), false);

            TextChannel channel = createTicket(guild, role, event.getMember());
            channel.sendMessageEmbeds(embedBuilder.build())
                    .setActionRow(Button.danger("delete-ticket", "Delete Application"))
                    .queue();
            event.reply("Your application was created at " + channel.getAsMention() + "!").setEphemeral(true).queue();
        } else if (event.getModalId().equals("ticket")) {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Ticket: " + event.getValue("subject").getAsString())
                    .setDescription(event.getMember().getEffectiveName() + " created a new ticket!")
                    .setColor(Color.decode("#2B2D31"))
                    .addField("Description", event.getValue("description").getAsString(), false);

            TextChannel channel = createTicket(guild, "ticket", event.getMember());
            channel.sendMessageEmbeds(embedBuilder.build())
                    .setActionRow(Button.danger("delete-ticket", "Delete Ticket"))
                    .queue();
            event.reply("Your ticket was created at " + channel.getAsMention() + "!").setEphemeral(true).queue();
        }
    }

    private TextChannel createTicket(@NotNull Guild guild, String name, @NotNull Member member) {
        return guild.createTextChannel(name + "-" + member.getEffectiveName(), guild.getCategoryById(guild.getId().equals(Config.PEGOS_ID) ? Config.PEGOS_TICKET_CATEGORY : Config.HECTUS_TICKET_CATEGORY))
                .syncPermissionOverrides()
                .addMemberPermissionOverride(member.getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL), List.of())
                .complete();
    }
}
