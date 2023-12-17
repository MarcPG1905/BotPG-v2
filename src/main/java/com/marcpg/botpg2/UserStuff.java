package com.marcpg.botpg2;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import me.marcpg1905.data.time.Time;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class UserStuff {
    public static final HashMap<Long, UserData> USERDATA = new HashMap<>();
    public static final Random RANDOM = new Random();

    public enum UserFlag {
        MINIMAL_ACCESS(4),
        LIMITED_ACCESS(3),
        WARNED(2),
        SUSPICIOUS(1),
        NONE(0);

        public final int rank;

        UserFlag(int rank) {
            this.rank = rank;
        }
    }

    public static class UserData {
        private final User user;
        private int messagesSent;
        private int level;
        private int levelXP;
        private int totalXP;
        private Message lastMessage;
        private boolean booster;
        private boolean donator;
        private UserFlag flag;
        private Time voiceChatTime;
        private int invitations;

        public int messagesSent() {
            return messagesSent;
        }
        public void messagesSent(int messagesSent) {
            this.messagesSent = messagesSent;
        }

        public int level() {
            return level;
        }
        public void level(int level) {
            this.level = level;
        }

        public int levelXP() {
            return levelXP;
        }
        public void levelXP(int levelXP) {
            this.levelXP = levelXP;
        }

        public long totalXP() {
            return totalXP;
        }
        public void totalXP(int totalXP) {
            this.totalXP = totalXP;
        }

        public Message lastMessage() {
            return lastMessage;
        }
        public void lastMessage(Message lastMessage) {
            this.lastMessage = lastMessage;
        }

        public boolean booster() {
            return booster;
        }
        public void booster(boolean booster) {
            this.booster = booster;
        }

        public boolean donator() {
            return donator;
        }
        public void donator(boolean donator) {
            this.donator = donator;
        }

        public UserFlag flag() {
            return flag;
        }
        public void flag(UserFlag flag) {
            this.flag = flag;
        }

        public Time voiceChatTime() {
            return voiceChatTime;
        }
        public void addVoiceChatTime(Time voiceChatTime) {
            this.voiceChatTime.increment(voiceChatTime.getAs(Time.Unit.SECONDS));
        }

        public int invitations() {
            return invitations;
        }
        public void invitations(int invitations) {
            this.invitations = invitations;
        }

        public UserData(User user, int messagesSent, int level, int levelXP, int totalXP, Message lastMessage, boolean booster, boolean donator, UserFlag flag) {
            this.user = user;
            this.messagesSent = messagesSent;
            this.level = level;
            this.levelXP = levelXP;
            this.totalXP = totalXP;
            this.lastMessage = lastMessage;
            this.booster = booster;
            this.donator = donator;
            this.flag = flag;
        }

        public void sentMessage(@NotNull Message message) {
            messagesSent++;

            if (!message.isFromGuild() || user.isBot() || user.isSystem() || message.isWebhookMessage()) return;
            if (flag.rank >= UserFlag.WARNED.rank) return;

            if (message.getTimeCreated().toEpochSecond() - (lastMessage != null ? lastMessage.getTimeCreated().toEpochSecond() : message.getTimeCreated().toEpochSecond() - 20) >= 10) {
                String content = message.getContentDisplay();
                int messageLength = content.length();

                lastMessage = message;
                if (content.length() <= 1) return;
                int xp = RANDOM.nextInt(messageLength, messageLength * 2);

                List<Message> messages = message.getChannel().getHistory().retrievePast(15).complete();
                ArrayList<String> messageContents = new ArrayList<>();
                for (Message msg : messages) {
                    if (message.getAuthor() == user) {
                        String msgContent = msg.getContentDisplay();
                        if (messageContents.contains(msgContent)) xp /= 5;
                        messageContents.add(content);
                    }
                }

                if (xp > 20) xp = 20;
                addXP(xp, message);
            }
        }

        public void addXP(int amount, Message message) {
            totalXP += amount;
            levelXP += amount;

            if (levelXP >= xpCalc(level)) {
                levelXP = levelXP - xpCalc(level);
                level++;

                if (level % 5 == 0) {
                    EmbedBuilder builder = new EmbedBuilder()
                            .setAuthor(user.getEffectiveName(), null, user.getEffectiveAvatarUrl())
                            .setColor(Color.GREEN)
                            .setTitle("Great job, " + user.getAsMention() + "!")
                            .setDescription("You just reached level " + level + "!");
                    message.replyEmbeds(builder.build()).queue();
                }
            }
        }

        public static class UserDataSerializer extends JsonSerializer<UserData> {
            @Override
            public void serialize(@NotNull UserData userData, @NotNull JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeStartObject();

                jsonGenerator.writeNumberField("userId", userData.user.getIdLong());
                jsonGenerator.writeNumberField("messagesSent", userData.messagesSent);
                jsonGenerator.writeNumberField("level", userData.level);
                jsonGenerator.writeNumberField("levelXP", userData.levelXP);
                jsonGenerator.writeNumberField("totalXP", userData.totalXP);
                jsonGenerator.writeBooleanField("booster", userData.booster);
                jsonGenerator.writeBooleanField("donator", userData.donator);
                jsonGenerator.writeStringField("flag", userData.flag.name());

                jsonGenerator.writeEndObject();
            }
        }

        public static class UserDataDeserializer extends JsonDeserializer<UserData> {
            @Override
            public UserData deserialize(@NotNull JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
                JsonNode node = jsonParser.getCodec().readTree(jsonParser);

                User user = BotPGv2.jda.getUserById(node.get("userId").asLong());
                int messagesSent = node.get("messagesSent").asInt();
                int level = node.get("level").asInt();
                int levelXP = node.get("levelXP").asInt();
                int totalXP = node.get("totalXP").asInt();
                boolean booster = node.get("booster").asBoolean();
                boolean donator = node.get("donator").asBoolean();
                UserFlag flag = UserFlag.valueOf(node.get("flag").asText());

                return new UserData(user, messagesSent, level, levelXP, totalXP, null, booster, donator, flag);
            }
        }
    }

    public static final double l = 19000, k = 0.019, c = 46.5;

    public static int xpCalc(int level) {
        return (int) (l / (c * Math.pow(Math.E, (-k * level)) + 1));
    }

    public static void save() throws IOException {
        File dataFile = new File("data.json");
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(UserData.class, new UserData.UserDataSerializer());
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        mapper.writeValue(dataFile, USERDATA);
    }

    public static void load() throws IOException {
        File dataFile = new File("data.json");
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(UserData.class, new UserData.UserDataDeserializer());
        mapper.registerModule(module);

        USERDATA.clear();
        TypeReference<HashMap<Long, UserData>> typeReference = new TypeReference<>() {};
        USERDATA.putAll(mapper.readValue(dataFile, typeReference));
    }
}
