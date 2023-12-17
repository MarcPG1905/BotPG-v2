package com.marcpg.botpg2.economy;

import com.marcpg.botpg2.UserStuff;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class FileManagementCommands extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("save")) {
            try {
                UserStuff.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (event.getName().equals("load")) {
            try {
                UserStuff.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
