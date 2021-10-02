package model.story;

public enum ItemType {
    OBJECT(""),
    DOCUMENT(""),
    PHOTO("");

    private String iconUrl;

    ItemType(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}
