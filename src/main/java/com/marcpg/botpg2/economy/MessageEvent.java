package com.marcpg.botpg2.economy;

import com.marcpg.botpg2.UserStuff;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class MessageEvent extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        System.out.println(event.getAuthor().getEffectiveName() + " send a message!");

        User user = event.getAuthor();

        if (!UserStuff.LAST_MESSAGES.containsKey(user.getIdLong()))
            UserStuff.LAST_MESSAGES.put(user.getIdLong(), event.getMessage());

        if (!UserStuff.DATABASE.contains(UserStuff.snowflakeToUuid(user.getIdLong()))) {
            int xp = new Random().nextInt(10, 20);
            UserStuff.DATABASE.add(UserStuff.snowflakeToUuid(user.getIdLong()), 1, 0, xp, xp, 0);
        }

        UserStuff.sentMessage(UserStuff.snowflakeToUuid(user.getIdLong()), event.getMessage());
    }
}
