package com.marcpg.botpg2.moderation;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ChatClearing extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("clear")) return;

        int count = Objects.requireNonNull(event.getOption("count")).getAsInt();
        if (count < 1) {
            event.reply("You can't delete " + count + " messages!").queue();
            return;
        }

        Channel channel = event.getChannel();

        if (event.getOption("channel") != null) {
            channel = Objects.requireNonNull(event.getOption("channel")).getAsChannel();
            if (!(channel instanceof TextChannel)) {
                event.reply("You can only select text channels for now!").setEphemeral(true).queue();
                return;
            }
        }

        List<Message> messages = ((MessageChannel) channel).getHistory().retrievePast(count).complete();
        for (Message m : messages) {
            m.delete().queue();
        }
        event.reply("Done!").queue();
    }
}
