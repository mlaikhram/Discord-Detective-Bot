package model.story;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;

public class Case implements DiscordEmbeddable {

    private String caseId;
    private String name;
    private String author;
    private String iconUrl;
    private FreeformEmbedBody rules;

    private ArrayList<Character> characters;
    private ArrayList<InventoryItem> inventory;
    private ArrayList<ChatLog> chatLogs;
    private ArrayList<Location> locations;


    @Override
    public MessageEmbed getEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(name);
        embedBuilder.setAuthor("Made by " + author, null, iconUrl);
        embedBuilder.setFooter(caseId + "");
        return rules.applyToEmbedBuilder(embedBuilder).build();
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public FreeformEmbedBody getRules() {
        return rules;
    }

    public void setRules(FreeformEmbedBody rules) {
        this.rules = rules;
    }

    public ArrayList<Character> getCharacters() {
        return characters;
    }

    public void setCharacters(ArrayList<Character> characters) {
        this.characters = characters;
    }

    public ArrayList<InventoryItem> getInventory() {
        return inventory;
    }

    public void setInventory(ArrayList<InventoryItem> inventory) {
        this.inventory = inventory;
    }

    public ArrayList<ChatLog> getChatLogs() {
        return chatLogs;
    }

    public void setChatLogs(ArrayList<ChatLog> chatLogs) {
        this.chatLogs = chatLogs;
    }

    public ArrayList<Location> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<Location> locations) {
        this.locations = locations;
    }
}
