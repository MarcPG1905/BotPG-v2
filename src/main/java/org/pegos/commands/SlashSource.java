package org.pegos.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class SlashSource extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("source")) return;

        String openSourceProjects = """
                - BotPG v2
                - PegOS Installer User Interface
                """;

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.DARK_GRAY)
                .setTitle("PegOS GitHub")
                .setUrl("https://github.com/Peg-OS")
                .setDescription("You can find all of the open-source code on [our GitHub](https://github.com/Peg-OS)")
                .addField("Open-Source Projects", openSourceProjects, false)
                .addField("Link", "https://github.com/Peg-OS", false);

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }
}
