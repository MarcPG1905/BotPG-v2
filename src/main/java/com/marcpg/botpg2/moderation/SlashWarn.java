package com.marcpg.botpg2.moderation;

import com.marcpg.botpg2.UserStuff;
import com.marcpg.data.time.Time;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.postgresql.util.PGTimestamp;

import java.awt.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SlashWarn extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("warn")) {
            if (!event.isFromGuild()) return;
            Guild guild = Objects.requireNonNull(event.getGuild());

            User user = Objects.requireNonNull(event.getOption("member")).getAsUser();
            String reason = Objects.requireNonNull(event.getOption("reason")).getAsString();
            int level = Objects.requireNonNull(event.getOption("level")).getAsInt();

            updateWarns(user);

            Date end = Date.from(Instant.ofEpochSecond(Instant.now().getEpochSecond() + Time.Unit.MONTHS.sec));
            UserStuff.WARN_DATABASE.add(UUID.randomUUID(), UserStuff.snowflakeToUuid(user.getIdLong()), UserStuff.snowflakeToUuid(event.getUser().getIdLong()), reason, level, PGTimestamp.from(end.toInstant()));

            int totalWarns = UserStuff.WARN_DATABASE.getRowArraysContaining(UserStuff.snowflakeToUuid(user.getIdLong()), "warned").stream()
                    .mapToInt(row -> (Integer) row[4])
                    .sum();

            if (user.hasPrivateChannel()) {
                user.openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("You got warned on " + guild.getName() + "!")
                            .setDescription("You just got warned by " + event.getUser().getEffectiveName() + ". Read more below.")
                            .setColor(Color.ORANGE)
                            .addField("Level", level + "/8", true)
                            .addField("Warned By", event.getUser().getEffectiveName(), true)
                            .addField("Reason", reason, false)
                            .build()
                    ).queue();

                    if (totalWarns >= 8) {
                        privateChannel.sendMessageEmbeds(new EmbedBuilder()
                                .setTitle("You reached the limit on " + guild.getName() + "!")
                                .setDescription("You just reached the limit of warns, which means you're gonna get banned.")
                                .setColor(Color.RED)
                                .build()
                        ).queue();
                    }
                });
            }

            event.reply(user.getAsMention()).setEmbeds(new EmbedBuilder()
                    .setTitle(user.getEffectiveName() + " got warned!")
                    .setDescription(user.getEffectiveName() + " just got warned by " + event.getUser().getEffectiveName() + ". Read more below.")
                    .setColor(Color.ORANGE)
                    .addField("Level", level + "/8", true)
                    .addField("Warned By", event.getUser().getEffectiveName(), true)
                    .addField("Reason", reason, false)
                    .build()
            ).queue();

            if (totalWarns >= 8) {
                event.getChannel().sendMessageEmbeds(new EmbedBuilder()
                        .setTitle(user.getEffectiveName() + " reached the limit!")
                        .setDescription(user.getEffectiveName() + " just reached the limit of warns, which means he's gonna get banned.")
                        .setColor(Color.RED)
                        .build()
                ).queue();

                Executors.newScheduledThreadPool(1).schedule(() -> Objects.requireNonNull(guild.getMember(user)).ban(0, TimeUnit.SECONDS).reason("Reaching the Warn-Limit").queue(), 10, TimeUnit.SECONDS);
            }
        }
    }

    public static void updateWarns(@NotNull User user) {
        Timestamp currentTime = Timestamp.from(Instant.now());
        for (Object[] row : UserStuff.WARN_DATABASE.getRowArraysContaining(UserStuff.snowflakeToUuid(user.getIdLong()))) {
            if (currentTime.after((PGTimestamp) row[5])) {
                UserStuff.WARN_DATABASE.remove((UUID) row[0]);
            }
        }
    }

    public record Warn(String userId, String warnerId, String reason, int level, Date ends) {
        @NotNull
        public String format() {
            return userId + " |+| " + warnerId + " |+| " + reason + " |+| " + level + " |+| " + ends.toInstant().getEpochSecond();
        }

        @NotNull
        @Contract("_ -> new")
        public static Warn parse(@NotNull String input) {
            String[] values = input.split(" \\|\\+\\| ");
            return new Warn(values[0], values[1], values[2], Integer.parseInt(values[3]), Date.from(Instant.ofEpochSecond(Long.parseLong(values[4]))));
        }
    }
}
