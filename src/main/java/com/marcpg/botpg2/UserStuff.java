package com.marcpg.botpg2;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.hectus.sql.AutoCatchingPostgreConnection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

public class UserStuff {
    public static final HashMap<Long, Message> LAST_MESSAGES = new HashMap<>();
    public static final Random RANDOM = new Random();
    public static AutoCatchingPostgreConnection DATABASE;

    /*
    |  Column | user_id     | messages_sent | level   | level_xp | total_xp | voice_chat_seconds |
    +---------+-------------+---------------+---------+----------+----------+--------------------+
    |    Type | BIGINT      | INTEGER       | INTEGER | INTEGER  | INTEGER  | BIGINT             |
    | Default | PRIMARY KEY | 0             | 0       | 0        | 0        | 0                  |
     */

    public static final double l = 19000, k = 0.019, c = 46.5;

    public static void load() throws SQLException {
        DATABASE = new AutoCatchingPostgreConnection(Config.PSQL_URL, Config.PSQL_USER, Config.PSQL_PASSWD, "discord_userdata", Throwable::printStackTrace);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull UUID snowflakeToUuid(long snowflake) {
        return new UUID(snowflake, 0);
    }

    public static int xpCalc(int level) {
        return (int) (l / (c * Math.pow(Math.E, (-k * level)) + 1));
    }

    public static void sentMessage(UUID uuid, @NotNull Message message) {
        User user = message.getAuthor();

        DATABASE.set(uuid, "messages_sent", (Long) DATABASE.get(uuid, "messages_sent") + 1);

        if (!message.isFromGuild() || user.isBot() || user.isSystem() || message.isWebhookMessage()) return;

        if (message.getTimeCreated().toEpochSecond() - LAST_MESSAGES.get(user.getIdLong()).getTimeCreated().toEpochSecond() >= 10) {
            String content = message.getContentDisplay();
            int messageLength = content.length();

            LAST_MESSAGES.put(user.getIdLong(), message);
            if (content.length() <= 1) return;
            int xp = RANDOM.nextInt(messageLength / 2, (int) (messageLength * 1.5));

            List<Message> messages = message.getChannel().getHistory().retrievePast(15).complete();
            ArrayList<String> messageContents = new ArrayList<>();
            for (Message msg : messages) {
                if (message.getAuthor() == user) {
                    String msgContent = msg.getContentDisplay();
                    if (messageContents.contains(msgContent)) xp /= 5;
                    messageContents.add(content);
                }
            }

            if (xp > 15) xp = 15;
            addXP(xp, uuid, message);
        }
    }

    public static void addXP(int amount, UUID uuid, Message message) {
        DATABASE.set(uuid, "total_xp", (Integer) DATABASE.get(uuid, "total_xp") + amount);
        DATABASE.set(uuid, "level_xp", (Integer) DATABASE.get(uuid, "level_xp") + amount);

        int requiredXp = xpCalc((Integer) DATABASE.get(uuid, "level"));

        if ((Integer) DATABASE.get(uuid, "level_xp") >= requiredXp) {
            DATABASE.set(uuid, "level_xp", (Integer) DATABASE.get(uuid, "level_xp") - requiredXp);
            DATABASE.set(uuid, "level", (Integer) DATABASE.get(uuid, "level") + 1);

            if ((Integer) DATABASE.get(uuid, "level") % 5 == 0) {
                EmbedBuilder builder = new EmbedBuilder()
                        .setAuthor(message.getAuthor().getEffectiveName(), null, message.getAuthor().getEffectiveAvatarUrl())
                        .setColor(Color.GREEN)
                        .setTitle("Great job, " + message.getAuthor().getAsMention() + "!")
                        .setDescription("You just reached level " + DATABASE.get(uuid, "level") + "!");
                message.replyEmbeds(builder.build()).queue();
            }
        }
    }
}
