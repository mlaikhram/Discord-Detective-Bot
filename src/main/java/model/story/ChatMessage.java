package model.story;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.temporal.TemporalAccessor;

public class ChatMessage implements DiscordEmbeddable {

    private Character author;
    private String text;
    private TemporalAccessor timestamp;
    private String imageUrl;

    private static final String THUMBNAIL_URL = "";

    @Override
    public MessageEmbed getEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor(author.getName(), null, author.getAvatarUrl());
        embedBuilder.setColor(author.getColor());
        embedBuilder.setDescription(text);
        embedBuilder.setImage(imageUrl);
        embedBuilder.setTimestamp(timestamp);
        embedBuilder.setThumbnail(THUMBNAIL_URL);

        return embedBuilder.build();
    }

    public Character getAuthor() {
        return author;
    }

    public void setAuthor(Character author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public TemporalAccessor getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(TemporalAccessor timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
