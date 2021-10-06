package listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.config.YmlConfig;
import model.story.*;
import model.story.Character;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;
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
    private long id;

    public DetectiveListener(YmlConfig config) {
        this.config = config;
    }

    public void setId(long id) {
        this.id = id;
        logger.info("id: " + this.id);
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

        createGuildCommands(guild);
    }

    private void createGuildCommands(Guild guild) {
        guild.updateCommands()
                .addCommands(new CommandData("inspect", "Try to learn about something in your current location")
                    .addOption(OptionType.STRING, "noun", "What to inspect", true))
                .addCommands(new CommandData("photograph", "Take a picture of something in your current location")
                    .addOption(OptionType.STRING, "noun", "What to take a picture of", true))
                .addCommands(new CommandData("use", "Use an item in your current location")
                    .addOption(OptionType.STRING, "item-id", "The id of the item to use", true)
                    .addOption(OptionType.STRING, "noun", "What you want to use the item on", false))
                .addCommands(new CommandData("unlock", "Unlock something in your current location")
                    .addOption(OptionType.STRING, "noun", "What you want to unlock", true)
                    .addOption(OptionType.STRING, "passcode", "What passcode to use", false)
                    .addOption(OptionType.STRING, "item-id", "The id of the item to use", false))
                .queue();
    }

    @Override
    public void onSlashCommand(@Nonnull SlashCommandEvent event) { // TODO: check if channel is a location
        event.deferReply().queue();
        String channelName = event.getTextChannel().getName();
        String command = event.getName();
        OptionMapping noun = event.getOption("noun");
        OptionMapping itemId = event.getOption("item-id");
        OptionMapping passcode = event.getOption("passcode");

        Message rulesMessage = event.getGuild().getTextChannelsByName(RULES, true).get(0).getHistory().retrievePast(1).complete().get(0);
        String caseId = rulesMessage.getEmbeds().get(0).getFooter().getText();
        Case currentCase = getCaseById(caseId);

        Location currentLocation = currentCase.getLocations().stream().filter(location -> channelStringEquals(location.getName(), channelName)).findFirst().orElse(null);

        if (currentLocation != null) {
            LocationCommand matchingCommand = currentLocation.getCommands().stream()
                    .filter(locationCommand -> locationCommand.getCommandType().toString().equalsIgnoreCase(command))
                    .filter(locationCommand -> (noun == null && locationCommand.getNoun() == null) || (noun != null && noun.getAsString().equalsIgnoreCase(locationCommand.getNoun())))
                    .filter(locationCommand -> (itemId == null && locationCommand.getItemId() == null) || (itemId != null && itemId.getAsString().equalsIgnoreCase(locationCommand.getItemId())))
                    .filter(locationCommand -> (passcode == null && locationCommand.getPasscode() == null) || (passcode != null && passcode.getAsString().equalsIgnoreCase(locationCommand.getPasscode())))
                    .findFirst().orElse(null);

            if (matchingCommand != null) {
                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append(matchingCommand.getSuccessMessage());
                if (!matchingCommand.getUnlockItems().isEmpty()) {

                    TextChannel inventoryChannel = event.getGuild().getTextChannelsByName(INVENTORY, true).get(0);
                    List<Message> messages = inventoryChannel.getHistory().retrievePast(100).complete();
                    Set<String> itemNames = new HashSet<>(messages.stream().map(message -> message.getEmbeds().get(0).getAuthor().getName().toLowerCase()).collect(Collectors.toSet()));
                    logger.info("got all found items: " + itemNames.size());
                    if (itemNames.contains(matchingCommand.getUnlockItems().get(0).getItemId().toLowerCase())) {
                        messageBuilder.append("\nI already gave you the items I found for this action.");
                    } else {
                        messageBuilder.append("\nI've added these items I found to your " + inventoryChannel.getAsMention() + ":");

                        for (InventoryItem inventoryItem : matchingCommand.getUnlockItems()) {
                            messageBuilder.append("\n" + inventoryItem.getTitle());
                            inventoryItem.setFounder(event.getMember());
                            inventoryChannel.sendMessageEmbeds(inventoryItem.getEmbed()).queue();
                        }
                    }
                }
                event.getHook().sendMessage(messageBuilder.toString()).queue();
            } else {
                event.getHook().sendMessage("That doesn't sound like it'll help here...").queue();
            }
        }
        else {
            event.getHook().sendMessage("I can't do that here! Ask me in a valid `Location`").queue();
        }
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        TextChannel textChannel = event.getMessage().getTextChannel();
        if (!event.getAuthor().isBot()) {
            String rawMessage = event.getMessage().getContentRaw();
            if (textChannel.getName().equals(ADMIN_CONSOLE)) {
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
                lookup(event, rawMessage);
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
            inventoryItem.setFounder(guild.getMemberById(id));
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

    private void lookup(GuildMessageReceivedEvent lookupEvent, String searchTerm) {
        // lookup for locations
        logger.info("searching for location...");
        Category locations = lookupEvent.getGuild().getCategoriesByName(LOCATIONS, true).get(0);
        for (TextChannel channel : locations.getTextChannels()) {
            if (channelStringEquals(channel.getName(), searchTerm)) {
                List<PermissionOverride> permissionOverrides = channel.getPermissionOverrides();
                for (PermissionOverride permissionOverride : permissionOverrides) {
                    if (permissionOverride.getDenied().contains(Permission.VIEW_CHANNEL)) {
                        permissionOverride.delete().queue();
                        lookupEvent.getMessage().reply("You discovered a new location: " + channel.getAsMention() + "!").queue();
                        return;
                    }
                }
                lookupEvent.getMessage().reply("We already knew about this location...").queue();
                return;
            }
        }

        // lookup for characters
        logger.info("searching for character...");
        TextChannel biosChannel = lookupEvent.getGuild().getTextChannelsByName(BIOS, true).get(0);
        List<Message> messages = biosChannel.getHistory().retrievePast(100).complete();
        Set<String> characterNames = new HashSet<>(messages.stream().map(message -> message.getEmbeds().get(0).getAuthor().getName().toLowerCase()).collect(Collectors.toSet()));
        logger.info("got all found characters: " + characterNames.size());
        if (characterNames.contains(searchTerm.toLowerCase())) {
            lookupEvent.getMessage().reply("We already knew about this person...").queue();
            return;
        }
        logger.info("search term not in existing characters...continuing search");
        Message rulesMessage = lookupEvent.getGuild().getTextChannelsByName(RULES, true).get(0).getHistory().retrievePast(1).complete().get(0);
        String caseId = rulesMessage.getEmbeds().get(0).getFooter().getText();
        Case currentCase = getCaseById(caseId);
        if (currentCase != null) {
            for (Character character : currentCase.getCharacters()) {
                if (character.getName().toLowerCase().equals(searchTerm.toLowerCase())) {
                    characterNames.add(character.getName().toLowerCase());
                    lookupEvent.getMessage().reply("You discovered a new person: `" + character.getName() + "`! I'll add them to the " + biosChannel.getAsMention()).queue();
                    biosChannel.sendMessageEmbeds(character.getEmbed()).queue();
                    List<TextChannel> chatLogChannels = lookupEvent.getGuild().getCategoriesByName(CHAT_LOGS, true).get(0).getTextChannels();
                    StringBuilder chatLogsMessageBuilder = new StringBuilder();
                    chatLogsMessageBuilder.append("I also found some chat logs related to this person:");

                    // search for any chat logs in case that contain this character
                    for (ChatLog chatLog : currentCase.getChatLogs()) {
                        if (chatLog.getParticipants().contains(character.getName())) {
                            if (characterNames.containsAll(chatLog.getParticipants().stream().map(String::toLowerCase).collect(Collectors.toSet()))) {

                                // search for appropriate text channel and update permissions
                                for (TextChannel chatLogChannel : chatLogChannels) {
                                    if (channelStringEquals(chatLogChannel.getName(), chatLog.getName())) {
                                        List<PermissionOverride> permissionOverrides = chatLogChannel.getPermissionOverrides();
                                        for (PermissionOverride permissionOverride : permissionOverrides) {
                                            if (permissionOverride.getDenied().contains(Permission.VIEW_CHANNEL)) {
                                                permissionOverride.delete().queue();
                                                chatLogsMessageBuilder.append("\n" + chatLogChannel.getAsMention());
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (chatLogsMessageBuilder.length() > "I also found some chat logs related to this person:".length()) {
                        lookupEvent.getMessage().reply(chatLogsMessageBuilder.toString()).queue();
                    }
                    return;
                }
            }
            lookupEvent.getMessage().reply("I couldn't find anything on that").queue();
        }
        else {
            lookupEvent.getMessage().reply("Something went wrong... I seem to have lost the case files").queue();
        }
    }

    private Case getCaseById(String id) {
        File casesDir = new File(config.getCasePath());
        File[] caseFiles = casesDir.listFiles();
        if (caseFiles != null) {
            ObjectMapper mapper = new ObjectMapper();
            for (int i = 0; i < caseFiles.length; ++i) {
                File caseFile = caseFiles[i];
                try {
                    Case detectiveCase = mapper.readValue(caseFile, Case.class);
                    if (detectiveCase.getCaseId().equals(id)) {
                        return detectiveCase;
                    }
                } catch (Exception e) {
                    logger.error("Could not load case: " + caseFile.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private boolean channelStringEquals(String s1, String s2) {
        return s1.toLowerCase().replace("-", " ").equals(s2.toLowerCase().replace("-", " "));
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
