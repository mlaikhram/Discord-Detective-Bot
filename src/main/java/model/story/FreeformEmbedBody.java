package model.story;

import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;

public class FreeformEmbedBody {

    private String description;
    private ArrayList<EmbedField> fields;
    private String imageUrl;

    public EmbedBuilder applyToEmbedBuilder(EmbedBuilder embedBuilder) {
        embedBuilder.setDescription(description);
        for (EmbedField embedField : fields) {
            embedBuilder.addField(embedField.getName(), embedField.getValue(), embedField.isInline());
        }
        embedBuilder.setImage(imageUrl);

        return  embedBuilder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<EmbedField> getFields() {
        return fields;
    }

    public void setFields(ArrayList<EmbedField> fields) {
        this.fields = fields;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
