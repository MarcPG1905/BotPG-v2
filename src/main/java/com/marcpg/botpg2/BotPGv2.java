package com.marcpg.botpg2;

import com.marcpg.botpg2.commands.SlashSocials;
import com.marcpg.botpg2.economy.FileManagementCommands;
import com.marcpg.botpg2.economy.MessageEvent;
import com.marcpg.botpg2.moderation.SlashClear;
import me.marcpg1905.color.Ansi;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import com.marcpg.botpg2.commands.SlashAvatar;
import com.marcpg.botpg2.commands.SlashSource;
import com.marcpg.botpg2.economy.SlashStats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class BotPGv2 extends ListenerAdapter {
    public static Guild pegosGuild;
    public static JDA jda;

    public static void main(String[] args) throws InterruptedException, IOException {
        String token = Files.readAllLines(Paths.get("token")).getFirst();
        System.out.println(Ansi.formattedString("Using token: " + token, Ansi.BRIGHT_BLUE));

        System.out.println(Ansi.formattedString("Allocated RAM: " + Runtime.getRuntime().maxMemory() + " bytes", Ansi.BRIGHT_BLUE));
        System.out.println(Ansi.formattedString("Available Processors: " + Runtime.getRuntime().availableProcessors() + " bytes", Ansi.BRIGHT_BLUE));
        System.out.println(Ansi.formattedString("Operating System: " + System.getProperty("os.name") + " bytes", Ansi.BRIGHT_BLUE));

        jda = JDABuilder.createDefault(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(
                        new SlashStats(),
                        new FileManagementCommands(),
                        new SlashSource(),
                        new SlashSocials(),
                        new SlashClear(),
                        new SlashAvatar(),
                        new MessageEvent(),
                        new BotPGv2()
                )
                .build()
                .awaitReady();
        System.out.println(Ansi.formattedString("Created the JDA instance!", Ansi.GREEN));

        pegosGuild = Objects.requireNonNull(jda.getGuildById(1140673234262564975L));
        System.out.println(Ansi.formattedString("Initialized the PegOS Guild!", Ansi.GREEN));

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
        System.out.println(Ansi.formattedString("Updated commands on the PegOS Guild!", Ansi.GREEN));
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
