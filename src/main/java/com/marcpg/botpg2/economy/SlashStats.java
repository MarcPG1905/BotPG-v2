package com.marcpg.botpg2.economy;

import com.marcpg.botpg2.UserStuff;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Map;
import java.util.Objects;

public class SlashStats extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("stats")) return;
        if (!event.isFromGuild()) {
            event.reply("This command can only be used in servers!").setEphemeral(true).queue();
            return;
        }

        OptionMapping option = event.getOption("user");
        Member member = Objects.requireNonNull(option != null ? option.getAsMember() : event.getMember());

        Map<String, Object> data = UserStuff.DATABASE.getRowMap(UserStuff.snowflakeToUuid(member.getIdLong()));
        if (data.isEmpty()) {
            event.reply("You need to send at least one message to use this command!").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl())
                .setColor(Color.YELLOW)
                .setTitle(member.getEffectiveName() + "'s Stats")
                .addField("Level", String.valueOf(data.get("level")), true)
                .addField("Level XP", String.valueOf(data.get("level_xp")), true)
                .addField("Total XP", String.valueOf(data.get("total_xp")), true)
                .addField("Messages", String.valueOf(data.get("messages_sent")), true)
                .addField("Boosting", member.isBoosting() ? "Yes" : "No", true);
        event.replyEmbeds(builder.build()).queue();
    }
}
