package com.marcpg.botpg2;

import com.marcpg.data.database.sql.AutoCatchingSQLConnection;
import com.marcpg.data.database.sql.SQLConnection;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

public class UserStuff {
    public static final HashMap<Long, Message> LAST_MESSAGES = new HashMap<>();

    private static final double l = 22000, k = 0.03, c = 21;
    private static final Random RANDOM = new Random();

    public static AutoCatchingSQLConnection<Long> DATABASE;
    public static AutoCatchingSQLConnection<UUID> WARN_DATABASE;
    /*
    ========================================= USERDATA =========================================
    |  Column | uuid        | messages_sent | level | level_xp | total_xp | voice_chat_seconds |
    +---------+-------------+---------------+-------+----------+----------+--------------------+
    |    Type | UUID        | INT           | INT   | INT      | INT      | BIGINT             |
    | Default | PRIMARY KEY | 0             | 0     | 0        | 0        | 0                  |
    |   Index | 0           | 1             | 2     | 3        | 4        | 5                  |
    ============================================================================================

    ====================================== WARNS ======================================
    |  Column | uuid        | warned   | warner   | reason   | level    | ending_time |
    +---------+-------------+----------+----------+----------+----------+-------------+
    |    Type | UUID        | UUID     | UUID     | TEXT     | INT      | TIMESTAMP   |
    | Default | PRIMARY KEY | NOT NULL | NOT NULL | NOT NULL | NOT NULL | NOT NULL    |
    |   Index | 0           | 1        | 2        | 3        | 4        | 5           |
    ===================================================================================
     */

    public static void load() throws SQLException, ClassNotFoundException {
        DATABASE = new AutoCatchingSQLConnection<>(SQLConnection.DatabaseType.POSTGRESQL, Config.PSQL_URL, Config.PSQL_USER, Config.PSQL_PASSWD, "userdata", "user_id", e -> System.out.println("Couldn't interact with userdata database : " + e.getMessage()));
        WARN_DATABASE = new AutoCatchingSQLConnection<>(SQLConnection.DatabaseType.POSTGRESQL, Config.PSQL_URL, Config.PSQL_USER, Config.PSQL_PASSWD, "warns", "uuid", e -> System.out.println("Couldn't interact with warn database : " + e.getMessage()));
    }

    public static int xpCalc(int level) {
        return (int) (l / (c * Math.pow(Math.E, (-k * level)) + 1));
    }

    public static void sentMessage(@NotNull Message message) {
        System.out.println(message.getAuthor().getName() + " sent a message in \"" + message.getGuild().getName() + "\" -> #" + message.getChannel().getName());

        User user = message.getAuthor();

        DATABASE.set(user.getIdLong(), "messages_sent", (Integer) DATABASE.get(user.getIdLong(), "messages_sent") + 1);

        if (!message.isFromGuild() || user.isBot() || user.isSystem() || message.isWebhookMessage()) return;

        if (message.getTimeCreated().toEpochSecond() - LAST_MESSAGES.get(user.getIdLong()).getTimeCreated().toEpochSecond() >= 10) {
            String content = message.getContentDisplay();
            int messageLength = content.length();

            LAST_MESSAGES.put(user.getIdLong(), message);
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
            addXP(Math.min(xp, 20), message);
        }
    }

    private static void addXP(int amount, @NotNull Message message) {
        long userId = message.getAuthor().getIdLong();

        DATABASE.set(userId, "total_xp", (Integer) DATABASE.get(userId, "total_xp") + amount);
        DATABASE.set(userId, "level_xp", (Integer) DATABASE.get(userId, "level_xp") + amount);

        int requiredXp = xpCalc((Integer) DATABASE.get(userId, "level"));

        if ((Integer) DATABASE.get(userId, "level_xp") >= requiredXp) {
            DATABASE.set(userId, "level_xp", (Integer) DATABASE.get(userId, "level_xp") - requiredXp);
            DATABASE.set(userId, "level", (Integer) DATABASE.get(userId, "level") + 1);

            if ((Integer) DATABASE.get(userId, "level") % 5 == 0) {
                EmbedBuilder builder = new EmbedBuilder()
                        .setAuthor(message.getAuthor().getEffectiveName(), null, message.getAuthor().getEffectiveAvatarUrl())
                        .setColor(Color.GREEN)
                        .setTitle("Great job, " + message.getAuthor().getAsMention() + "!")
                        .setDescription("You just reached level " + DATABASE.get(userId, "level") + "!");
                message.replyEmbeds(builder.build()).queue();
            }
        }
    }
}
