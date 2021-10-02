package listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.config.YmlConfig;
import model.story.*;
import model.story.Character;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DetectiveListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(DetectiveListener.class);

    private static final String ADMIN_CONSOLE = "admin-console";
    private static final String RULES = "rules";
    private static final String BIOS = "bios";
    private static final String INVENTORY = "inventory";
    private static final String NOTES = "notes";
    private static final String LOOKUP = "lookup";
    private static final String CHAT_LOGS = "chat-logs";
    private static final String LOCATIONS = "locations";

    private final YmlConfig config;

    public DetectiveListener(YmlConfig config) {
        this.config = config;
    }

    @Override
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        Guild guild = event.getGuild();

        guild.createTextChannel(ADMIN_CONSOLE).setTopic("Used for creating and restarting detective games.").addPermissionOverride(guild.getPublicRole(), Collections.emptySet(), EnumSet.of(Permission.VIEW_CHANNEL)).queue();

        guild.createTextChannel(RULES).addPermissionOverride(guild.getPublicRole(), Collections.emptySet(), EnumSet.of(Permission.MESSAGE_WRITE)).queue();


        guild.createTextChannel(BIOS).setTopic("Contains the bios for all relevant characters. If new characters are discovered, they will get added here.").addPermissionOverride(guild.getPublicRole(), Collections.emptySet(), EnumSet.of(Permission.MESSAGE_WRITE)).queue();
        guild.createTextChannel(INVENTORY).setTopic("Contains all items you have found so far. If new items are discovered, they will get added here.").addPermissionOverride(guild.getPublicRole(), Collections.emptySet(), EnumSet.of(Permission.MESSAGE_WRITE)).queue();
        guild.createTextChannel(NOTES).setTopic("Free space for all players to leave notes and discuss ideas.").addPermissionOverride(guild.getPublicRole(), Collections.emptySet(), EnumSet.of(Permission.USE_SLASH_COMMANDS)).queue();
        guild.createTextChannel(LOOKUP).setTopic("Used for searching keywords to discover new locations or characters. Type a search term and wait for a response.").addPermissionOverride(guild.getPublicRole(), Collections.emptySet(), EnumSet.of(Permission.USE_SLASH_COMMANDS)).queue();

        guild.createCategory(CHAT_LOGS).addPermissionOverride(guild.getPublicRole(), Collections.emptySet(), EnumSet.of(Permission.MESSAGE_WRITE)).queue();
        guild.createCategory(LOCATIONS).queue();
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        TextChannel textChannel = event.getMessage().getTextChannel();
        if (!event.getAuthor().isBot()) {
            if (textChannel.getName().equals(ADMIN_CONSOLE)) {
                String rawMessage = event.getMessage().getContentRaw();
                String[] messageTokens = inputToCommand(rawMessage).toArray(new String[0]);

                if (messageTokens.length > 0 && messageTokens[0].equals("list")) {
                    File casesDir = new File(config.getCasePath());
                    File[] caseFiles = casesDir.listFiles();
                    if (caseFiles != null) {
                        ObjectMapper mapper = new ObjectMapper();
                        StringBuilder messageBuilder = new StringBuilder();
                        messageBuilder.append("Here are the cases I have on file:");
                        for (int i = 0; i < caseFiles.length; ++i) {
                            File caseFile = caseFiles[i];
                            try {
                                Case detectiveCase = mapper.readValue(caseFile, Case.class);
                                messageBuilder.append(String.format("\n[%d] `%s` by %s", i, detectiveCase.getName(), detectiveCase.getAuthor()));
                            } catch (Exception e) {
                                logger.error("Could not load case: " + caseFile.getName() + ": " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        textChannel.sendMessage(messageBuilder.toString()).queue();
                    } else {
                        textChannel.sendMessage("Could not find the cases folder. Make sure you specified the path correctly!").queue();
                    }
                } else if (messageTokens.length > 1 && messageTokens[0].equals("load")) {
                    try {
                        int caseNumber = Integer.parseInt(messageTokens[1]);
                        File casesDir = new File(config.getCasePath());
                        File[] caseFiles = casesDir.listFiles();
                        if (caseFiles != null) {
                            if (caseNumber >= 0 && caseNumber < caseFiles.length) {
                                ObjectMapper mapper = new ObjectMapper();
                                Case detectiveCase = mapper.readValue(caseFiles[caseNumber], Case.class);
                                textChannel.sendMessage("Ok, clearing the current case...").queue();
                                clearCurrentCase(event.getGuild());
                                textChannel.sendMessage("Done! Opening Case #" + caseNumber + "...").queue();
                                openCase(event.getGuild(), detectiveCase);
                                textChannel.sendMessage("Done! Check the rules to see how to play.").queue();
                            } else {
                                textChannel.sendMessage("That case number is too big or too small").queue();
                            }
                        } else {
                            textChannel.sendMessage("Could not find the cases folder. Make sure you specified the path correctly!").queue();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        textChannel.sendMessage("Couldn't open case: " + messageTokens[1] + ". make sure you send the index of the case you want to open.").queue();
                    }
                }
            }
            else if (textChannel.getName().equals(LOOKUP)) {

            }
        }
    }

    private void openCase(Guild guild, Case selectedCase) {
        guild.getTextChannelsByName(RULES, true).get(0).sendMessageEmbeds(selectedCase.getEmbed()).queue();

        for (Character character : selectedCase.getCharacters()) {
            if (character.isStarter()) {
                guild.getTextChannelsByName(BIOS, true).get(0).sendMessageEmbeds(character.getEmbed()).queue();
            }
        }

        for (InventoryItem inventoryItem : selectedCase.getInventory()) {
            guild.getTextChannelsByName(INVENTORY, true).get(0).sendMessageEmbeds(inventoryItem.getEmbed()).queue();
        }

        Category chatLogs = guild.getCategoriesByName(CHAT_LOGS, true).get(0);
        for (ChatLog chatLog : selectedCase.getChatLogs()) {
            chatLogs.createTextChannel(chatLog.getName()).setTopic(chatLog.getDescription()).addPermissionOverride(guild.getPublicRole(), Collections.emptySet(), selectedCase.getCharacters().stream().filter(Character::isStarter).map(Character::getName).collect(Collectors.toSet()).containsAll(chatLog.getParticipants()) ? EnumSet.of(Permission.MESSAGE_WRITE) : EnumSet.of(Permission.VIEW_CHANNEL)).queue((textChannel) -> {
                for (ChatMessage chatMessage : chatLog.getMessages()) {
                    textChannel.sendMessageEmbeds(chatMessage.getEmbed()).queue();
                }
            });
        }

        Category locations = guild.getCategoriesByName(LOCATIONS, true).get(0);
        for (Location location : selectedCase.getLocations()) {
            locations.createTextChannel(location.getName()).setTopic(location.getDescription()).addPermissionOverride(guild.getPublicRole(), Collections.emptySet(), location.isStarter() ? Collections.emptySet() : EnumSet.of(Permission.VIEW_CHANNEL)).queue();
        }
    }

    private void clearCurrentCase(Guild guild) {
        // clear rules, bios, inventory, notes, and lookup
        clearTextChannel(guild.getTextChannelsByName(RULES, true).get(0));
        clearTextChannel(guild.getTextChannelsByName(BIOS, true).get(0));
        clearTextChannel(guild.getTextChannelsByName(INVENTORY, true).get(0));
        clearTextChannel(guild.getTextChannelsByName(NOTES, true).get(0));
        clearTextChannel(guild.getTextChannelsByName(LOOKUP, true).get(0));

        // delete chat logs and locations
        Category chatLogs = guild.getCategoriesByName(CHAT_LOGS, true).get(0);
        List<GuildChannel> channels = chatLogs.getChannels();
        for (GuildChannel channel : channels) {
            channel.delete().complete();
        }
        Category locations = guild.getCategoriesByName(LOCATIONS, true).get(0);
        channels = locations.getChannels();
        for (GuildChannel channel : channels) {
            channel.delete().complete();
        }
    }

    private void clearTextChannel(TextChannel channel) {
        List<Message> messages;
        do {
            messages = channel.getHistory().retrievePast(50).complete();
            if (messages.size() > 1) {
                channel.deleteMessages(messages).complete();
            }
            else if (messages.size() == 1) {
                channel.deleteMessageById(messages.get(0).getIdLong()).complete();
            }
        } while (!messages.isEmpty());
    }

    private List<String> inputToCommand(String message) {
        List<String> command = new ArrayList<>();
        Matcher m = Pattern.compile("([^`]\\S*|`.+?`)\\s*").matcher(message);
        while (m.find()) {
            command.add(m.group(1).replace("`", ""));
        }
        return command;
    }
}
