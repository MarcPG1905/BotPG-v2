package com.marcpg.botpg2.economy;

import com.marcpg.botpg2.BotPGv2;
import com.marcpg.botpg2.Config;
import com.marcpg.botpg2.UserStuff;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PrivateCommands extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!List.of("save-data", "load-data", "stop-botpg").contains(event.getName())) return;

        if (!event.getUser().getId().equals(Config.MARCPG)) {
            event.reply("Only MarcPG can use this command!").setEphemeral(true).queue();
            return;
        }

        if (event.getName().equals("save-data")) {
            UserStuff.save();
            event.reply("Saved all user data to the 'userdata' file successfully!").setEphemeral(true).queue();
        } else if (event.getName().equals("load-data")) {
            UserStuff.load();
            event.reply("Loaded all user data from the 'userdata' file successfully!").setEphemeral(true).queue();
        } else if (event.getName().equals("stop-botpg")) {
            event.reply("Shutting down and saving user data!").setEphemeral(true).queue();
            UserStuff.save();
            BotPGv2.JDA.shutdown();
        }
    }
}
