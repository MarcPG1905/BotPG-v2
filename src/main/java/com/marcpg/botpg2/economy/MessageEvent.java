package com.marcpg.botpg2.economy;

import com.marcpg.botpg2.UserStuff;
import com.marcpg.data.time.Time;
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

        if (!UserStuff.USERDATA.containsKey(user.getIdLong())) {
            int xp = new Random().nextInt(10, 20);
            UserStuff.USERDATA.put(user.getIdLong(), new UserStuff.UserData(user, 1, 0, xp, xp, event.getMessage(), new Time(0)));
        }

        UserStuff.USERDATA.get(user.getIdLong()).sentMessage(event.getMessage());
    }
}
