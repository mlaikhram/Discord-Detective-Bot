package model.story;

import java.util.ArrayList;

public class LocationCommand {

    private CommandType commandType;
    private String noun;
    private String itemId;
    private String passcode;

    private String successMessage;
    private ArrayList<InventoryItem> unlockItems;

    public CommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    public String getNoun() {
        return noun;
    }

    public void setNoun(String noun) {
        this.noun = noun;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public ArrayList<InventoryItem> getUnlockItems() {
        return unlockItems;
    }

    public void setUnlockItems(ArrayList<InventoryItem> unlockItems) {
        this.unlockItems = unlockItems;
    }
}
