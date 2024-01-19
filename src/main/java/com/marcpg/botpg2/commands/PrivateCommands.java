package com.marcpg.botpg2.commands;

import com.marcpg.botpg2.BotPGv2;
import com.marcpg.botpg2.Config;
import com.marcpg.botpg2.UserStuff;
import com.marcpg.botpg2.moderation.SlashWarn;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class PrivateCommands extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("stop-botpg")) {
            if (!event.getUser().getId().equals(Config.MARCPG)) {
                event.reply("Only MarcPG can use this command!").setEphemeral(true).queue();
                return;
            }

            event.reply("Shutting down and saving user data!").setEphemeral(true).queue();
            UserStuff.save();
            SlashWarn.save();
            BotPGv2.JDA.shutdown();
        }
    }
}
