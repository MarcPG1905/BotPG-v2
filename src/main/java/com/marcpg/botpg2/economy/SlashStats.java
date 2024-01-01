package com.marcpg.botpg2.economy;

import com.marcpg.botpg2.UserStuff;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

public class SlashStats extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("stats")) return;
        if (!event.isFromGuild()) {
            event.reply("This command can only be used in servers!").setEphemeral(true).queue();
            return;
        }

        Member member = Objects.requireNonNull(event.getMember());

        OptionMapping option = event.getOption("user");
        User user = option != null ? option.getAsUser() : event.getUser();
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
                .addField("Boosting", member.isBoosting() ? "Yes" : "No", true)
                .addField("Donator", data.donator() ? "Yes" : "No", true)
                .addField("Flag", String.valueOf(data.flag()).toLowerCase(), true);

        event.replyEmbeds(builder.build()).queue();
    }
}
