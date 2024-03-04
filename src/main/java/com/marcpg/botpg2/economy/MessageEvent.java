package com.marcpg.botpg2.economy;

import com.marcpg.botpg2.UserStuff;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MessageEvent extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        User user = event.getAuthor();

        if (!UserStuff.LAST_MESSAGES.containsKey(user.getIdLong()))
            UserStuff.LAST_MESSAGES.put(user.getIdLong(), event.getMessage());

        if (!UserStuff.DATABASE.contains(user.getIdLong()))
            UserStuff.DATABASE.add(Map.of("user_id", user.getIdLong()));

        UserStuff.sentMessage(event.getMessage());
    }
}
