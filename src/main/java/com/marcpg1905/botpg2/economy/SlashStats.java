package com.marcpg1905.botpg2.economy;

import com.marcpg1905.botpg2.UserStuff;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class SlashStats extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("stats")) return;

        User user = event.getUser();
        UserStuff.UserData data = UserStuff.USERDATA.get(user.getIdLong());
        if (data == null) {
            event.reply("You need to send at least one message to use this command!").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor(user.getEffectiveName(), null, user.getEffectiveAvatarUrl())
                .setColor(Color.YELLOW)
                .setTitle(user.getEffectiveName() + "'s Stats")
                .addField("Level", String.valueOf(data.level()), true)
                .addField("Level XP", String.valueOf(data.levelXP()), true)
                .addField("Total XP", String.valueOf(data.totalXP()), true)
                .addField("Messages", String.valueOf(data.messagesSent()), true)
                .addField("Boosting", String.valueOf(data.booster()), true)
                .addField("Donator", String.valueOf(data.donator()), true)
                .addField("Flag", String.valueOf(data.flag()).toLowerCase(), true)
                .addField("VC Time", String.valueOf(data.voiceChatTime()).toLowerCase(), true)
                .addField("Invited Members", String.valueOf(data.invitations()).toLowerCase(), true);
        event.replyEmbeds(builder.build()).queue();
    }
}
