package org.pegos;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.pegos.commands.SlashSocials;
import org.pegos.commands.SlashSource;
import org.pegos.moderation_commands.SlashClear;

import java.util.Objects;

public class BotPG {
    public static Guild pegosGuild;

    public static void main(String[] args) throws InterruptedException {
        JDA jda = JDABuilder.createDefault("MTE0NzQ4MzY2MTQ5OTg5NTg3OQ.GNb9Cf.PAeMhhmci30ZlQgdaieVBG3Ij9_evxZ56rQ1W0")
                .addEventListeners(new SlashSource(), new SlashSocials(), new SlashClear())
                .build();

        jda.awaitReady();

        pegosGuild = Objects.requireNonNull(jda.getGuildById(1140673234262564975L));

        pegosGuild.updateCommands().addCommands(
                Commands.slash("source", "Gives you links to the PegOS source-code."),
                Commands.slash("socials", "Gives you links to all of our social-media accounts."),
                Commands.slash("clear", "Clears the chat.")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
                        .addOption(OptionType.INTEGER, "count", "How many messages to clear")
                        .addOption(OptionType.CHANNEL, "channel", "What channel to clear.", false)
        ).queue();
    }
}
