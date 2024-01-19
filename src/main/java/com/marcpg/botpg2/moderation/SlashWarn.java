package com.marcpg.botpg2.moderation;

import com.marcpg.color.Ansi;
import com.marcpg.data.time.Time;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SlashWarn extends ListenerAdapter {
    public static final List<Warn> WARNS = new ArrayList<>();

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
            WARNS.add(new Warn(user.getId(), event.getUser().getId(), reason, level, end));

            int totalWarns = WARNS.stream()
                    .filter(warn -> warn.userId.equals(user.getId()))
                    .mapToInt(Warn::level)
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

    public static void updateWarns(User user) {
        WARNS.removeIf(warn -> warn.userId.equals(user.getId()) && new Date().after(warn.ends));
    }

    public static void load() throws IOException {
        Files.readAllLines(new File("warns").toPath()).forEach(string -> WARNS.add(Warn.parse(string)));
    }

    public static void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(new File("userdata").toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Warn warn : WARNS) {
                writer.write(warn.format());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println(Ansi.formattedString("Couldn't write all warns to the `warns` file!", Ansi.RED));
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
