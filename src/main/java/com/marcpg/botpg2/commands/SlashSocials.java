package com.marcpg.botpg2.commands;

import com.marcpg.botpg2.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

public class SlashSocials  extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("socials")) return;
        if (!event.isFromGuild()) {
            event.reply("This command can only be used on servers!").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();
        if (Objects.requireNonNull(event.getGuild()).getId().equals(Config.PEGOS_ID)) {
            embed.setTitle("PegOS Social-Media Accounts")
                    .setColor(Color.decode("#4EA7F5"))
                    .setDescription("Here are all currently existing social-media accounts.")
                    .addField("TikTok", "[@pegos.org](https://tiktok.com/@pegos.org)", true)
                    .addField("Instagram", "[@pegos.official](https://instagram.com/pegos.official)", true)
                    .addField("Twitter/X", "[@PegOS_Official](https://x.com/PegOS_Official)", true)
                    .addField("Website", "https://pegos.org/", true)
                    .setFooter("More platforms will come soon!");
        } else if (Objects.requireNonNull(event.getGuild()).getId().equals(Config.HECTUS_ID)) {
            embed.setTitle("Hectus Social-Media Accounts")
                    .setColor(Color.decode("#F547BE"))
                    .setDescription("Here are all currently existing social-media accounts.")
                    .addField("TikTok", "[@hectus.net](https://tiktok.com/@hectus.net)", true)
                    .addField("Instagram", "[@hectusnetwork](https://www.instagram.com/hectusnetwork/)", true)
                    .addField("Twitter/X", "[@HectusNet](https://x.com/HectusNet)", true)
                    .addField("Website", "https://hectus.net/", true)
                    .setFooter("More platforms and translated accounts will come soon!");
        } else if (Objects.requireNonNull(event.getGuild()).getId().equals(Config.SPELLBOUND_ID)) {
            embed.setTitle("SpellBoundMC Social-Media Accounts")
                    .setColor(Color.decode("#DB9C27"))
                    .setDescription("Here are all currently existing social-media accounts.")
                    .addField("TikTok", "[@spellboundmc](https://tiktok.com/@spellboundmc)", true)
                    .addField("Twitter/X", "[@_SpellBound___](https://twitter.com/_SpellBound___)", true)
                    .addField("Website", "https://spellboundmc.com/", true)
                    .setFooter("More platforms will come soon!");
        }

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }
}
