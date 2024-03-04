package com.marcpg.botpg2.economy;

import com.marcpg.botpg2.BotPGv2;
import com.marcpg.botpg2.UserStuff;
import com.marcpg.text.Formatter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SlashLeaderboard extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("leaderboard")) return;

        String stat = event.getSubcommandName();

        if (stat == null) {
            event.reply("Invalid Stat!").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder builder = new EmbedBuilder()
                .setColor(new Color(43, 45, 49))
                .setDescription("Here are the current top 12 users in the chat:");

        if (stat.equals("leveling")) {
            builder.setTitle("Leaderboard - Leveling");
            List<Map<String, Object>> allData = UserStuff.DATABASE.getAllRowMaps().parallelStream()
                    .sorted(Comparator.comparingInt((Map<String, Object> map) -> (Integer) map.get("total_xp")).reversed())
                    .limit(12)
                    .toList();
            for (int i = 0; i < allData.size(); i++) {
                Map<String, Object> userdata = allData.get(i);
                builder.addField(
                        (i + 1) + ". " + BotPGv2.JDA.retrieveUserById((Long) userdata.get("user_id")).complete().getEffectiveName(),
                        "Level: `" + userdata.get("level") + "`\nLevel XP: `" + userdata.get("level_xp") + "`",
                        true);
            }
        } else if (stat.equals("messages")) {
            builder.setTitle("Leaderboard - Messages Sent");
            List<Map<String, Object>> allData = UserStuff.DATABASE.getAllRowMaps().parallelStream()
                    .sorted(Comparator.comparingInt((Map<String, Object> map) -> (Integer) map.get("messages_sent")).reversed())
                    .limit(12)
                    .toList();
            for (int i = 0; i < allData.size(); i++) {
                Map<String, Object> userdata = allData.get(i);
                builder.addField(
                        (i + 1) + ". " + BotPGv2.JDA.retrieveUserById((Long) userdata.get("user_id")).complete().getEffectiveName(),
                        "Messages: `" + userdata.get("messages_sent") + "`",
                        true);
            }
        }

        event.replyEmbeds(builder.build()).queue();
    }
}
