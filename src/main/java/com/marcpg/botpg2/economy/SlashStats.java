package com.marcpg.botpg2.economy;

import com.marcpg.botpg2.BotPGv2;
import com.marcpg.botpg2.UserStuff;
import com.marcpg.botpg2.moderation.Warning;
import com.marcpg.data.time.Time;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class SlashStats extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().contains("stats")) return;
        if (!event.isFromGuild()) {
            event.reply("This command can only be used in servers!").setEphemeral(true).queue();
            return;
        }

        OptionMapping option = event.getOption("user");
        Member member = Objects.requireNonNull(option != null ? option.getAsMember() : event.getMember());

        if (event.getName().equals("stats")) {
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
                    .addField("Boosting", member.isBoosting() ? "Yes" : "No", true)
                    .addField("Warns", Warning.getWarns(member.getIdLong()).stream().mapToInt(row -> (Integer) row[4]) + "/8", true);
            event.replyEmbeds(builder.build()).queue();
        } else {
            EmbedBuilder builder = new EmbedBuilder()
                    .setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl())
                    .setColor(Color.YELLOW)
                    .setTitle(member.getEffectiveName() + "'s Warnings");

            Warning.getWarns(member.getIdLong()).forEach(warn -> {
                User byUser = BotPGv2.JDA.getUserById(((UUID) warn[2]).getMostSignificantBits());
                String by = (byUser == null ? "Unknown" : byUser.getAsMention());
                long expires = ((Timestamp) warn[5]).toInstant().getEpochSecond();
                builder.addField("<t:" + (expires - Time.Unit.MONTHS.sec) + ":R>:", "**Level:** " + warn[4] + "\n**Reason: **" + warn[3] + "\n**By:** " + by + "\n**Expires:** <t:" + expires + ":R>\n**Warn-UUID**: `" + warn[0].toString() + "`\n ", false);
                builder.addBlankField(false);
            });

            event.replyEmbeds(builder.build()).queue();
        }
    }
}
