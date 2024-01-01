package com.marcpg.botpg2.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

public class SlashAvatar extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("avatar")) return;
		if (!event.isFromGuild()) {
			event.reply("This command can only be used on servers!").setEphemeral(true).queue();
			return;
		}

		Member target = event.getOption("user") == null ? event.getMember() : Objects.requireNonNull(event.getOption("user")).getAsMember();

        assert target != null;
        EmbedBuilder embed = new EmbedBuilder()
				.setColor(Color.decode("#FFFFFF"))
				.setTitle(target.getUser().getName() + "'s User Profile")
				.setImage(target.getAvatarUrl())
				.addField("User ID", "`" + target.getId() + "`", true)
				.addField("Account Creation", "<t:" + target.getTimeCreated().toEpochSecond() + ">", true)
				.setColor(target.getColor())
				.addField("Nickname", Objects.requireNonNullElse(target.getNickname(), "None"), true)
				.addField("Owner", target.isOwner() ? "Yes" : "No", true)
				.addField("Boosting", target.isBoosting() ? "Yes" : "No", true);

		event.replyEmbeds(embed.build()).queue();
    }
}
