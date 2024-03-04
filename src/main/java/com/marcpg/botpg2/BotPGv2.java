package com.marcpg.botpg2;

import com.marcpg.botpg2.commands.*;
import com.marcpg.botpg2.economy.MessageEvent;
import com.marcpg.botpg2.economy.SlashLeaderboard;
import com.marcpg.botpg2.economy.SlashStats;
import com.marcpg.botpg2.moderation.ChatClearing;
import com.marcpg.botpg2.moderation.ModUtility;
import com.marcpg.botpg2.moderation.Warning;
import com.marcpg.color.Ansi;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.sql.SQLException;

public class BotPGv2 {
    public static JDA JDA;

    public static void main(String[] args) throws InterruptedException {
        Config.load();

        JDA = JDABuilder.createDefault(Config.TOKEN, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(
                        new MessageEvent(), new PrivateCommands(), new SlashAvatar(), new ChatClearing(), new ModUtility(),
                        new SlashSocials(), new SlashSource(), new SlashStats(), new SlashTicketMsg(), new Warning(),
                        new Tickets(), new SlashLeaderboard()
                )
                .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS)
                .build()
                .awaitReady();
        System.out.println(Ansi.formattedString("Created the JDA instance!", Ansi.GREEN));

        try {
            UserStuff.load();
            System.out.println(Ansi.formattedString("Loaded user data and warnings!", Ansi.GREEN));
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
