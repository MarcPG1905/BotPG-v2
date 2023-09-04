package org.pegos.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

public class SlashAvatar extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("avatar")) return;

		User target = Objects.requireNonNull(event.getOption("user")).getAsUser();

		EmbedBuilder embed = new EmbedBuilder()
			.setColor(Color.decode("#FF0000"))
			.setTitle(target.getEffectiveName() + "'s Avatar")
			.setImage(target.getAvatarUrl());

		event.replyEmbeds(embed.build()).queue();
    }
}
