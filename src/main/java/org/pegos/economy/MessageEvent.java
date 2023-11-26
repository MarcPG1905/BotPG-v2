package org.pegos.economy;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.pegos.UserStuff;

import java.util.Random;

public class MessageEvent extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        System.out.println(event.getAuthor().getEffectiveName() + " send a message!");

        User user = event.getAuthor();
        Member member = event.getMember();

        if (UserStuff.USERDATA.get(user.getIdLong()) == null) {
            int xp = new Random().nextInt(10, 20);
            UserStuff.USERDATA.put(user.getIdLong(), new UserStuff.UserData(user, 1, 0, xp, xp, event.getMessage(), (member != null && member.isBoosting()), false, UserStuff.UserFlag.NONE));
        }

        UserStuff.UserData data = UserStuff.USERDATA.get(user.getIdLong());

        if (data.flag().rank >= UserStuff.UserFlag.LIMITED_ACCESS.rank) {
            event.getMessage().delete().queue();
            if (user.hasPrivateChannel()) {
                user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage("You have limited access due to breaking rules right now!")).queue();
            }
            return;
        }

        data.sentMessage(event.getMessage());
    }
}
