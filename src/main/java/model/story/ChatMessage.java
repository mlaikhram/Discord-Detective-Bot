package model.story;

import com.fasterxml.jackson.annotation.JsonFormat;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.time.ZoneId;
import java.util.Date;

public class ChatMessage implements DiscordEmbeddable {

    private Character author;
    private String text;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ")
    private Date timestamp;
    private String imageUrl;

    private static final String THUMBNAIL_URL = "";

    @Override
    public MessageEmbed getEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor(author.getName(), null, author.getAvatarUrl());
        embedBuilder.setColor(Color.decode(author.getColor()));
        embedBuilder.setDescription(text);
        embedBuilder.setImage(imageUrl);
        embedBuilder.setTimestamp(timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
