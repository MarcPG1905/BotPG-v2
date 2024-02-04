package com.marcpg.botpg2;

import com.marcpg.botpg2.commands.*;
import com.marcpg.botpg2.economy.MessageEvent;
import com.marcpg.botpg2.economy.SlashStats;
import com.marcpg.botpg2.moderation.SlashClear;
import com.marcpg.botpg2.moderation.SlashModUtil;
import com.marcpg.botpg2.moderation.SlashWarn;
import com.marcpg.botpg2.tickets.TicketCreation;
import com.marcpg.color.Ansi;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.io.IOException;
import java.sql.SQLException;

public class BotPGv2 extends ListenerAdapter {
    public static JDA JDA;

    public static void main(String[] args) throws InterruptedException {
        Config.load();

        JDA = JDABuilder.createDefault(Config.TOKEN, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(new TicketCreation(), new SlashTicketMsg(), new SlashModUtil(), new SlashStats(), new PrivateCommands(), new SlashSource(), new SlashSocials(), new SlashClear(), new SlashAvatar(), new MessageEvent(), new BotPGv2())
                .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS)
                .build()
                .awaitReady();
        System.out.println(Ansi.formattedString("Created the JDA instance!", Ansi.GREEN));

        try {
            UserStuff.load();
            SlashWarn.load();
            System.out.println(Ansi.formattedString("Loaded user data and warnings!", Ansi.GREEN));
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
