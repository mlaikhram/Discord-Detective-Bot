package model.story;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class InventoryItem implements DiscordEmbeddable {

    private String itemId;
    private String title;
    private FreeformEmbedBody embedBody;
    private Color color;
    private Member founder;
    private ItemType itemType;


    @Override
    public MessageEmbed getEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title);
        embedBuilder.setAuthor(itemId);
        embedBuilder.setColor(color);
        embedBuilder = embedBody.applyToEmbedBuilder(embedBuilder);
        embedBuilder.setThumbnail(itemType.getIconUrl());
        embedBuilder.setFooter((itemType == ItemType.PHOTO ? "Taken by " : "Found by ") + founder.getEffectiveName(), founder.getUser().getEffectiveAvatarUrl());
        return embedBuilder.build();
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public FreeformEmbedBody getEmbedBody() {
        return embedBody;
    }

    public void setEmbedBody(FreeformEmbedBody embedBody) {
        this.embedBody = embedBody;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Member getFounder() {
        return founder;
    }

    public void setFounder(Member founder) {
        this.founder = founder;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }
}
