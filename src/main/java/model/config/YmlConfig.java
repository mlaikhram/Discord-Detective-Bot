package model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class YmlConfig {

    @JsonProperty
    private DiscordConfig discord;

    @JsonProperty
    private String casePath;

    public DiscordConfig getDiscord() {
        return discord;
    }

    public String getCasePath() {
        return casePath;
    }

}
