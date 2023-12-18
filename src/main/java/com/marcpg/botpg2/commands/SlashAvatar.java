package com.marcpg.botpg2.commands;

import com.marcpg.text.Formatter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class SlashAvatar extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("avatar")) return;

		User target = Objects.requireNonNull(event.getOption("user")).getAsUser();

		EmbedBuilder embed = new EmbedBuilder()
				.setColor(Color.decode("#FFFFFF"))
				.setTitle(target.getName() + "'s Avatar")
				.setImage(target.getAvatarUrl())
				.addField("User ID", target.getId(), true)
				.addField("Account Creation", target.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME), true);

		Member targetMember = Objects.requireNonNull(event.getOption("user")).getAsMember();
		if (targetMember != null) {
			embed
					.setColor(targetMember.getColor())
					.addField("Nickname", Objects.requireNonNullElse(targetMember.getNickname(), "None"), true)
					.addField("Currently Active Devices", lookGood(targetMember.getActiveClients()), true)
					.addField("Member Flags", lookGood(targetMember.getFlags()), true)
					.addField("Status", targetMember.getOnlineStatus().name(), true)
					.addField("Owner", targetMember.isOwner() ? "Yes" : "No", true)
					.addField("Boosting", targetMember.isBoosting() ? "Yes" : "No", true);
		}

		event.replyEmbeds(embed.build()).queue();
    }

	@NotNull
	public static String lookGood(@NotNull Object input) {
		String newString = input.toString().replace("[", "").replace("]", "");
        if (newString.isBlank() || newString.isEmpty()) {
			return "None";
		} else {
			return Formatter.toPascalCase(newString);
		}
	}
}
