package model.story;

public enum ItemType {
    OBJECT("https://static.thenounproject.com/png/2007252-200.png"),
    DOCUMENT("https://cdn1.iconfinder.com/data/icons/round2-set/25/Doc_ic-512.png"),
    PHOTO("https://cdn4.iconfinder.com/data/icons/ionicons/512/icon-camera-512.png");

    private String iconUrl;

    ItemType(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}
