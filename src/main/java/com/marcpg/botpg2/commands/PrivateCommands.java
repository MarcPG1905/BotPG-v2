package com.marcpg.botpg2.commands;

import com.marcpg.botpg2.UserStuff;
import com.marcpg.color.Ansi;
import com.marcpg.storing.Pair;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class PrivateCommands extends ListenerAdapter {
    public static final List<Message> LOADED_MESSAGES = new ArrayList<>();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("load-messages")) {
            event.deferReply(true).queue();
            try {
                List<GuildMessageChannel> channels = new ArrayList<>();
                channels.addAll(Objects.requireNonNull(event.getGuild()).getTextChannels());
                channels.addAll(event.getGuild().getNewsChannels());
                channels.addAll(event.getGuild().getThreadChannels());
                channels.addAll(event.getGuild().getVoiceChannels());
                channels.addAll(event.getGuild().getStageChannels());

                for (GuildMessageChannel channel : channels) {
                    long start = System.currentTimeMillis();
                    LOADED_MESSAGES.addAll(channel.getIterableHistory()
                            .takeAsync(9999)
                            .get());
                    long end = System.currentTimeMillis();
                    System.out.println(Ansi.formattedString("Retrieved " + channel.getGuild().getName() + " / #" + channel.getName() + " - Took " + (end - start) + "ms!", Ansi.DARK_GRAY));
                }

                LOADED_MESSAGES.sort(Comparator.comparing(Message::getTimeCreated));
                System.out.println(Ansi.formattedString("Retrieved all channel history!", Ansi.GREEN));
                event.getHook().editOriginal("Retrieved all channel history!").queue();
            } catch (InterruptedException e) {
                System.out.println(Ansi.formattedString("Channel history retrieving got interrupted!", Ansi.RED));
            } catch (ExecutionException e) {
                System.out.println(Ansi.formattedString("Couldn't retrieve the iterable channel history!", Ansi.RED));
            }
        } else if (event.getName().equals("simulate-xp")) {
            Map<Long, Pair<AtomicInteger, AtomicInteger>> data = new HashMap<>();

            for (Message message : LOADED_MESSAGES) {
                long user = message.getAuthor().getIdLong();
                if (!data.containsKey(user)) {
                    data.put(user, new Pair<>(new AtomicInteger(0), new AtomicInteger(0)));
                }
                int index = LOADED_MESSAGES.indexOf(message);
                data.get(user).left().incrementAndGet();
                data.get(user).right().addAndGet(getXp(message, LOADED_MESSAGES.subList(Math.max(index - 15, 0), index)));
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("simulation_result"))) {
                writer.write(String.format("%-22s%-7s%-10s%-10s%-10s", "User ID", "Level", "Level XP", "Total XP", "Messages"));
                writer.newLine();
                UserStuff.DATABASE.connection().prepareStatement("DELETE FROM " + UserStuff.DATABASE.table()).executeUpdate();
                for (Map.Entry<Long, Pair<AtomicInteger, AtomicInteger>> entry : data.entrySet()) {
                    Pair<Integer, Integer> result = calculate(entry.getValue().right().get());
                    writer.write(String.format("%-22s%-7s%-10s%-10s%-10s", entry.getKey(), result.left(), result.right(), entry.getValue().right().get(), entry.getValue().left().get()));
                    writer.newLine();
                    UserStuff.DATABASE.add(Map.of(
                            "user_id", entry.getKey(),
                            "level", result.left(),
                            "level_xp", result.right(),
                            "total_xp", entry.getValue().right().get(),
                            "messages_sent", entry.getValue().left().get()));
                }
            } catch (IOException e) {
                System.out.println(Ansi.formattedString("Couldn't save the processed result of the XP simulation to the file!", Ansi.RED));
            } catch (SQLException e) {
                System.out.println(Ansi.formattedString("Couldn't save the processed result into the database!", Ansi.RED));
            }
        }
    }

    public static final Random RANDOM = new Random();
    public static final Map<Long, OffsetDateTime> LAST_MESSAGES = new HashMap<>();

    public static int getXp(@NotNull Message message, List<Message> last15Messages) {
        User user = message.getAuthor();
        long userId = user.getIdLong();

        if (user.isBot() || user.isSystem() || message.isWebhookMessage()) return 0;

        if (!LAST_MESSAGES.containsKey(userId))
            LAST_MESSAGES.put(userId, message.getTimeCreated());

        if (message.getTimeCreated().toEpochSecond() - LAST_MESSAGES.get(userId).toEpochSecond() >= 10) {
            String content = message.getContentDisplay();
            int messageLength = content.length();

            LAST_MESSAGES.put(userId, message.getTimeCreated());

            if (messageLength <= 1) return 0;
            int xp = RANDOM.nextInt(messageLength, messageLength * 2);

            ArrayList<String> messageContents = new ArrayList<>();
            for (Message msg : last15Messages) {
                if (message.getAuthor() == user) {
                    String msgContent = msg.getContentDisplay();
                    if (messageContents.contains(msgContent)) xp /= 5;
                    messageContents.add(content);
                }
            }
            return Math.min(xp, 20);
        } else
            return 0;
    }

    /** Pair(Level, Level XP) */
    @Contract("_ -> new")
    public static @NotNull Pair<Integer, Integer> calculate(int xp) {
        int level = 0;
        while (true) {
            int required = UserStuff.xpCalc(level);
            if (xp >= required) {
                level++;
                xp -= required;
            } else {
                return new Pair<>(level, xp);
            }
        }
    }
}
