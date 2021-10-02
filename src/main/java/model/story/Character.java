package model.story;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class Character implements DiscordEmbeddable {

    private String name;
    private String avatarUrl;
    private Color color;
    private FreeformEmbedBody bio;
    private boolean isStarter;

    @Override
    public MessageEmbed getEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor(name, null, avatarUrl);
        embedBuilder.setColor(color);
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

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public FreeformEmbedBody getBio() {
        return bio;
    }

    public void setBio(FreeformEmbedBody bio) {
        this.bio = bio;
    }

    public boolean isStarter() {
        return isStarter;
    }

    public void setStarter(boolean starter) {
        isStarter = starter;
    }
}
