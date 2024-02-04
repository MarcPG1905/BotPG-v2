package com.marcpg.botpg2.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class SlashTicketMsg extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("send-application-message")) {
            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle("Staff Application")
                    .setColor(Color.decode("#2B2D31"))
                    .setDescription("You need to answer some quick questions before a ticket will be created.\nClick below to apply to the team.")
                    .setFooter("You might need to scroll down to see all roles.");

            StringSelectMenu menu = StringSelectMenu.create("application-role")
                    .addOptions(
                            SelectOption.of("Moderator", "moderator")
                                    .withDescription("Keep the server safe and family-friendly.")
                                    .withEmoji(Emoji.fromFormatted("<:ModIcon:1192934642500763718>")),
                            SelectOption.of("Developer", "developer")
                                    .withDescription("Code and maintain the server's gamemodes.")
                                    .withEmoji(Emoji.fromFormatted("<:DevIcon:1192934626394636428>")),
                            SelectOption.of("Web Developer", "web-developer")
                                    .withDescription("Code and maintain our website.")
                                    .withEmoji(Emoji.fromFormatted("<:WebDevIcon:1192934660267856045>")),
                            SelectOption.of("Voice Actor", "voice-actor")
                                    .withDescription("Record your voice for use in our video-content.")
                                    .withEmoji(Emoji.fromFormatted("<:VoiceActorIcon:1192934658183266364>")),
                            SelectOption.of("Event Manager", "event-manager")
                                    .withDescription("Create and manage server events.")
                                    .withEmoji(Emoji.fromFormatted("<:EventMangerIcon:1196499777685303306>")),
                            SelectOption.of("Content Creator", "content-creator")
                                    .withDescription("Help recording, setting up or editing our content.")
                                    .withEmoji(Emoji.fromFormatted("<:ContentCreatorIcon:1192934620858155150>")),
                            SelectOption.of("Builder", "builder")
                                    .withDescription("Help at building the server's maps and structures.")
                                    .withEmoji(Emoji.fromFormatted("<:BuilderIcon:1192934618589040800>")),
                            SelectOption.of("Translator", "translator")
                                    .withDescription("Translate the server to make it accessible for everyone.")
                                    .withEmoji(Emoji.fromFormatted("<:TranslatorIcon:1192934655696064643>"))
                    )
                    .build();

            event.getChannel().sendMessageEmbeds(builder.build()).addActionRow(menu).queue();
            event.reply("Done!").setEphemeral(true).queue();
        } else if (event.getName().equals("send-ticket-message")) {
            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle("Open a Ticket")
                    .setColor(Color.decode("#2B2D31"))
                    .setDescription("You need to answer some quick questions before a ticket will be created.\nClick below to create a ticket.");

            event.getChannel().sendMessageEmbeds(builder.build()).addActionRow(Button.success("open-ticket", "Open Ticket")).queue();
            event.reply("Done!").setEphemeral(true).queue();
        }
    }
}
