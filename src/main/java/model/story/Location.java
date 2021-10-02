package model.story;

import java.util.ArrayList;

public class Location {

    private String name;
    private String description;
    private ArrayList<LocationCommand> commands;
    private boolean isStarter;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<LocationCommand> getCommands() {
        return commands;
    }

    public void setCommands(ArrayList<LocationCommand> commands) {
        this.commands = commands;
    }

    public boolean isStarter() {
        return isStarter;
    }

    public void setStarter(boolean starter) {
        isStarter = starter;
    }
}
