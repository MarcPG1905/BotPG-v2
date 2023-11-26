package org.pegos;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.pegos.commands.SlashAvatar;
import org.pegos.commands.SlashSocials;
import org.pegos.commands.SlashSource;
import org.pegos.economy.MessageEvent;
import org.pegos.economy.FileManagementCommands;
import org.pegos.economy.SlashStats;
import org.pegos.moderation.SlashClear;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class BotPG extends ListenerAdapter {
    public static Guild pegosGuild;
    public static JDA jda;

    public static void main(String[] args) throws InterruptedException, IOException {
        String token = Files.readAllLines(Paths.get("token")).get(0);
        System.out.println("Using token: " + token);

        jda = JDABuilder.createDefault(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(
                        new SlashStats(),
                        new FileManagementCommands(),
                        new SlashSource(),
                        new SlashSocials(),
                        new SlashClear(),
                        new SlashAvatar(),
                        new MessageEvent(),
                        new BotPG()
                )
                .build()
                .awaitReady();

        pegosGuild = Objects.requireNonNull(jda.getGuildById(1140673234262564975L));

        pegosGuild.updateCommands().addCommands(
                Commands.slash("stats", "Gives information about your stats, like your level and XP."),
                Commands.slash("load", "Loads xp from a file, temporary command"),
                Commands.slash("save", "Saves xp in a file, temporary command"),
                Commands.slash("source", "Gives you links to the PegOS source-code."),
                Commands.slash("socials", "Gives you links to all of our social-media accounts."),
                Commands.slash("avatar", "Gives you the profile picture and some other info about a user.")
                        .addOption(OptionType.USER, "user", "What user to get the info from.", true),
                Commands.slash("clear", "Clears the chat.")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
                        .addOption(OptionType.INTEGER, "count", "How many messages to clear")
                        .addOption(OptionType.CHANNEL, "channel", "What channel to clear.", false)
        ).queue();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        try {
            UserStuff.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        try {
            UserStuff.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
