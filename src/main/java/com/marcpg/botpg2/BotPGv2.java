package com.marcpg.botpg2;

import com.marcpg.botpg2.commands.SlashAvatar;
import com.marcpg.botpg2.commands.SlashSocials;
import com.marcpg.botpg2.commands.SlashSource;
import com.marcpg.botpg2.economy.MessageEvent;
import com.marcpg.botpg2.economy.PrivateCommands;
import com.marcpg.botpg2.economy.SlashStats;
import com.marcpg.botpg2.moderation.SlashClear;
import com.marcpg.botpg2.moderation.SlashModUtil;
import com.marcpg.color.Ansi;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class BotPGv2 extends ListenerAdapter {
    public static JDA JDA;

    public static void main(String[] args) throws InterruptedException {
        Config.load();

        JDA = JDABuilder.createDefault(Config.TOKEN, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(new SlashModUtil(), new SlashStats(), new PrivateCommands(), new SlashSource(), new SlashSocials(), new SlashClear(), new SlashAvatar(), new MessageEvent(), new BotPGv2())
                .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS)
                .build()
                .awaitReady();
        System.out.println(Ansi.formattedString("Created the JDA instance!", Ansi.GREEN));

        Thread.sleep(100);
        UserStuff.load();
    }
}
