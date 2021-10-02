package model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DiscordConfig {

    @JsonProperty
    private String token;

    public String getToken() {
        return token;
    }

}
