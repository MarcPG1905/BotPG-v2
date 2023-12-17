package com.marcpg.botpg2.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class SlashSocials  extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("socials")) return;

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.decode("#4EA7F5"))
                .setTitle("PegOS Social-Media Accounts")
                .setDescription("Here are all currently existing social-media accounts.")
                .addField("TikTok", "[@pegos.org](https://tiktok.com/@pegos.org)", true)
                .addField("Instagram", "[@pegos.official](https://instagram.com/pegos.official)", true)
                .addField("Twitter/X", "[@PegOS_Official](https://x.com/PegOS_Official)", true)
                .addField("Website", "https://pegos.org/ (not existing yet)", true)
                .setFooter("More platforms and translated accounts will come soon!");

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }
}
