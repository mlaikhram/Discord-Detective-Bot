package model.story;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class Character implements DiscordEmbeddable {

    private String name;
    private String avatarUrl;
    private String color;
    private FreeformEmbedBody bio;
    private boolean starter;

    @Override
    public MessageEmbed getEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor(name, null, avatarUrl);
        embedBuilder.setColor(Color.decode(color));
        return bio.applyToEmbedBuilder(embedBuilder).build();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public FreeformEmbedBody getBio() {
        return bio;
    }

    public void setBio(FreeformEmbedBody bio) {
        this.bio = bio;
    }

    public boolean isStarter() {
        return starter;
    }

    public void setStarter(boolean starter) {
        this.starter = starter;
    }
}
