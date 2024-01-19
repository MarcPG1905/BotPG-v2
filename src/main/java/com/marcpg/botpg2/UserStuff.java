package com.marcpg.botpg2;

import com.marcpg.color.Ansi;
import com.marcpg.data.time.Time;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class UserStuff {
    public static final HashMap<Long, UserData> USERDATA = new HashMap<>();
    public static final Random RANDOM = new Random();

    public static class UserData {
        private final User user;
        private int messagesSent;
        private int level;
        private int levelXP;
        private int totalXP;
        private Message lastMessage;
        private final Time voiceChatTime;

        public int messagesSent() {
            return messagesSent;
        }

        public int level() {
            return level;
        }

        public int levelXP() {
            return levelXP;
        }

        public long totalXP() {
            return totalXP;
        }

        public UserData(User user, int messagesSent, int level, int levelXP, int totalXP, Message lastMessage, Time voiceChatTime) {
            this.user = user;
            this.messagesSent = messagesSent;
            this.level = level;
            this.levelXP = levelXP;
            this.totalXP = totalXP;
            this.lastMessage = lastMessage;
            this.voiceChatTime = voiceChatTime;
        }

        public void sentMessage(@NotNull Message message) {
            messagesSent++;

            if (!message.isFromGuild() || user.isBot() || user.isSystem() || message.isWebhookMessage()) return;

            if (message.getTimeCreated().toEpochSecond() - (lastMessage != null ? lastMessage.getTimeCreated().toEpochSecond() : message.getTimeCreated().toEpochSecond() - 20) >= 10) {
                String content = message.getContentDisplay();
                int messageLength = content.length();

                lastMessage = message;
                if (content.length() <= 1) return;
                int xp = RANDOM.nextInt(messageLength, messageLength * 2);

                List<Message> messages = message.getChannel().getHistory().retrievePast(15).complete();
                ArrayList<String> messageContents = new ArrayList<>();
                for (Message msg : messages) {
                    if (message.getAuthor() == user) {
                        String msgContent = msg.getContentDisplay();
                        if (messageContents.contains(msgContent)) xp /= 5;
                        messageContents.add(content);
                    }
                }

                if (xp > 20) xp = 20;
                addXP(xp, message);
            }
        }

        public void addXP(int amount, Message message) {
            totalXP += amount;
            levelXP += amount;

            if (levelXP >= xpCalc(level)) {
                levelXP = levelXP - xpCalc(level);
                level++;

                if (level % 5 == 0) {
                    EmbedBuilder builder = new EmbedBuilder()
                            .setAuthor(user.getEffectiveName(), null, user.getEffectiveAvatarUrl())
                            .setColor(Color.GREEN)
                            .setTitle("Great job, " + user.getAsMention() + "!")
                            .setDescription("You just reached level " + level + "!");
                    message.replyEmbeds(builder.build()).queue();
                }
            }
        }
    }

    public static final double l = 19000, k = 0.019, c = 46.5;

    public static int xpCalc(int level) {
        return (int) (l / (c * Math.pow(Math.E, (-k * level)) + 1));
    }

    public static void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(new File("userdata").toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (UserData d : USERDATA.values()) {
                List<String> list = List.of(
                        d.user.getId(),
                        String.valueOf(d.messagesSent),
                        String.valueOf(d.level),
                        String.valueOf(d.levelXP),
                        String.valueOf(d.totalXP),
                        String.valueOf(d.voiceChatTime.get())
                );
                writer.write(String.join(", ", list));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println(Ansi.formattedString("Couldn't write all user data to the `userdata` file!", Ansi.RED));
        }
    }

    public static void load() throws IOException {
        USERDATA.clear();
        for (String line : Files.readAllLines(new File("userdata").toPath())) {
            String[] values = line.split(", ");
            USERDATA.put(Long.parseLong(values[0]), new UserData(
                    BotPGv2.JDA.retrieveUserById(values[0]).complete(),
                    Integer.parseInt(values[1]),
                    Integer.parseInt(values[2]),
                    Integer.parseInt(values[3]),
                    Integer.parseInt(values[4]),
                    null, new Time(Long.parseLong(values[5]))));
        }
    }
}
