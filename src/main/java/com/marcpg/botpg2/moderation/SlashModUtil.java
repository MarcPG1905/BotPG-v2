package com.marcpg.botpg2.moderation;

import com.marcpg.botpg2.Config;
import com.marcpg.data.time.Time;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SlashModUtil extends ListenerAdapter {
    public static final List<String> COMMANDS = List.of("kick", "ban", "pardon", "timeout", "un-timeout");

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!COMMANDS.contains(event.getName())) return;
        if (!event.isFromGuild()) {
            event.reply("This command can only be used on servers!").setEphemeral(true).queue();
            return;
        }

        Guild guild = Objects.requireNonNull(event.getGuild());
        Member member = Objects.requireNonNull(event.getMember());
        String reason = Objects.requireNonNull(event.getOption("reason")).getAsString();

        final TextChannel modChannel = Objects.requireNonNull(guild.getTextChannelById(guild.getId().equals(Config.PEGOS_ID) ? Config.PEGOS_MODS_ONLY : Config.HECTUS_MODS_ONLY));

        if (event.getName().equals("pardon")) {
            String userString = Objects.requireNonNull(event.getOption("id")).getAsString();

            guild.retrieveBanList().stream()
                    .map(Guild.Ban::getUser)
                    .filter(user -> user.getName().equals(userString) || user.getId().equals(userString))
                    .findFirst()
                    .ifPresentOrElse(
                            mentionedUser -> guild.unban(mentionedUser).queue(
                                    success -> {
                                        modChannel.sendMessageEmbeds(new EmbedBuilder()
                                                .setTitle(member.getEffectiveName() + " unbanned " + mentionedUser.getEffectiveName())
                                                .setDescription(mentionedUser.getEffectiveName() + " got unbanned by " + member.getEffectiveName() + ".")
                                                .setColor(Color.YELLOW)
                                                .addField("Reason", reason, false)
                                                .build()
                                        ).queue();

                                        if (mentionedUser.hasPrivateChannel()) {
                                            mentionedUser.openPrivateChannel().queue(pm -> pm.sendMessageEmbeds(new EmbedBuilder()
                                                    .setTitle("Unbanned from " + Objects.requireNonNull(guild).getName())
                                                    .setDescription("You just got unbanned from the " + guild.getName() + " Discord server by " + member.getEffectiveName() + "!")
                                                    .setColor(Color.GREEN)
                                                    .addField("Reason", reason, false)
                                                    .build()
                                            ).queue());
                                        }
                                        event.reply("Successfully unbanned the user " + mentionedUser.getEffectiveName() + "!").setEphemeral(true).queue();
                                    },
                                    failure -> event.reply("The user " + mentionedUser.getEffectiveName() + " couldn't be unbanned or wasn't banned in the first place!").setEphemeral(true).queue()
                            ),
                            () -> event.reply("The user could not be found!").setEphemeral(true).queue()
                    );
            return;
        }

        Member mentionedMember = Objects.requireNonNull(Objects.requireNonNull(event.getOption("member")).getAsMember());

        if (mentionedMember == member) {
            event.reply("You can't " + event.getName() + " yourself!").setEphemeral(true).queue();
            return;
        }

        if (mentionedMember.getRoles().getFirst().getPosition() >= member.getRoles().getFirst().getPosition()) {
            event.reply("You can't " + event.getName() + " members with higher or equal permissions!").setEphemeral(true).queue();
            return;
        }

        switch (event.getName()) {
            case "kick" -> {
                modChannel.sendMessageEmbeds(new EmbedBuilder()
                        .setTitle(member.getEffectiveName() + " kicked " + mentionedMember.getEffectiveName())
                        .setDescription(mentionedMember.getEffectiveName() + " was just kicked by " + member.getEffectiveName() + ".")
                        .setColor(Color.ORANGE)
                        .addField("Reason", reason, false)
                        .build()
                ).queue();

                if (mentionedMember.getUser().hasPrivateChannel()) {
                    mentionedMember.getUser().openPrivateChannel().queue(pm -> pm.sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("Kicked from " + Objects.requireNonNull(guild).getName())
                            .setDescription("You just got kicked from the Discord server by " + member.getEffectiveName() + "!")
                            .setColor(Color.ORANGE)
                            .addField("Reason", reason, false)
                            .build()
                    ).queue());
                }

                mentionedMember.kick().queue();
                event.reply("Successfully kicked " + mentionedMember.getEffectiveName() + "!").setEphemeral(true).queue();
            }
            case "ban" -> {
                OptionMapping deletionTime = event.getOption("deletion-time");

                modChannel.sendMessageEmbeds(new EmbedBuilder()
                        .setTitle(member.getEffectiveName() + " banned " + mentionedMember.getEffectiveName())
                        .setDescription(mentionedMember.getEffectiveName() + " was just banned by " + member.getEffectiveName() + ".")
                        .setColor(Color.RED)
                        .addField("Reason", reason, false)
                        .build()
                ).queue();

                if (mentionedMember.getUser().hasPrivateChannel()) {
                    mentionedMember.getUser().openPrivateChannel().queue(pm -> pm.sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("Banned from " + Objects.requireNonNull(guild).getName())
                            .setDescription("You just got banned from the " + guild.getName() + " Discord server by " + member.getEffectiveName() + "!")
                            .setColor(Color.RED)
                            .addField("Reason", reason, false)
                            .build()
                    ).queue());
                }

                mentionedMember.ban(deletionTime == null ? 0 : (int) Time.parse(deletionTime.getAsString()).get(), TimeUnit.SECONDS).reason(reason).queue();

                event.reply("Successfully banned " + mentionedMember.getEffectiveName() + "!").setEphemeral(true).queue();
            }
            case "timeout" -> {
                String time = Objects.requireNonNull(event.getOption("time")).getAsString();

                modChannel.sendMessageEmbeds(new EmbedBuilder()
                        .setTitle(member.getEffectiveName() + " timed out " + mentionedMember.getEffectiveName())
                        .setDescription(mentionedMember.getEffectiveName() + " was just timed out by " + member.getEffectiveName() + ".")
                        .setColor(Color.ORANGE)
                        .addField("Reason", reason, false)
                        .build()
                ).queue();

                if (mentionedMember.getUser().hasPrivateChannel()) {
                    mentionedMember.getUser().openPrivateChannel().queue(pm -> pm.sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("Timed out on " + Objects.requireNonNull(guild).getName())
                            .setDescription("You just got timed out on the " + guild.getName() + " Discord server by " + member.getEffectiveName() + "!")
                            .setColor(Color.ORANGE)
                            .addField("Reason", reason, false)
                            .build()
                    ).queue());
                }

                mentionedMember.timeoutFor((int) Time.parse(time).get(), TimeUnit.SECONDS).queue();
                event.reply("Successfully timeouted " + mentionedMember.getEffectiveName() + " for " + Time.parse(time).getPreciselyFormatted() + "!").setEphemeral(true).queue();
            }
            case "un-timeout" -> mentionedMember.removeTimeout().queue(
                    success -> {
                        modChannel.sendMessageEmbeds(new EmbedBuilder()
                                .setTitle(member.getEffectiveName() + " un-timeouted " + mentionedMember.getEffectiveName())
                                .setDescription(mentionedMember.getEffectiveName() + " got his timeout removed by " + member.getEffectiveName() + ".")
                                .setColor(Color.YELLOW)
                                .addField("Reason", reason, false)
                                .build()
                        ).queue();

                        if (mentionedMember.getUser().hasPrivateChannel()) {
                            mentionedMember.getUser().openPrivateChannel().queue(pm -> pm.sendMessageEmbeds(new EmbedBuilder()
                                    .setTitle("Un-timeouted from " + Objects.requireNonNull(guild).getName())
                                    .setDescription("Your timeout just got removed from the " + guild.getName() + " Discord server by " + member.getEffectiveName() + "!")
                                    .setColor(Color.GREEN)
                                    .addField("Reason", reason, false)
                                    .build()
                            ).queue());
                        }
                        event.reply("Successfully removed " + mentionedMember.getEffectiveName() + "'s timeout!").setEphemeral(true).queue();
                    },
                    failure -> event.reply("The timeout of " + mentionedMember.getEffectiveName() + " could not be removed, or he wasn't timed out in the first place!").setEphemeral(true).queue()
            );
        }
    }
}
