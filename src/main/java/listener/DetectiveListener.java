package listener;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.EnumSet;

public class DetectiveListener extends ListenerAdapter {

    private static final String ADMIN_CONSOLE = "admin-console";
    private static final String RULES = "rules";
    private static final String BIOS = "bios";
    private static final String INVENTORY = "inventory";
    private static final String NOTES = "notes";
    private static final String LOOKUP = "lookup";
    private static final String CHAT_LOGS = "chat-logs";
    private static final String LOCATIONS = "locations";

    @Override
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        Guild guild = event.getGuild();

        guild.createTextChannel(ADMIN_CONSOLE).setTopic("Used for creating and restarting detective games.").addPermissionOverride(guild.getPublicRole(), Collections.emptySet(), EnumSet.of(Permission.VIEW_CHANNEL)).queue();

        guild.createTextChannel(RULES).addPermissionOverride(guild.getPublicRole(), Collections.emptySet(), EnumSet.of(Permission.MESSAGE_WRITE)).queue();


        guild.createTextChannel(BIOS).setTopic("Contains the bios for all relevant characters. If new characters are discovered, they will get added here.").addPermissionOverride(guild.getPublicRole(), Collections.emptySet(), EnumSet.of(Permission.MESSAGE_WRITE)).queue();
        guild.createTextChannel(INVENTORY).setTopic("Contains all items you have found so far. If new items are discovered, they will get added here.").addPermissionOverride(guild.getPublicRole(), Collections.emptySet(), EnumSet.of(Permission.MESSAGE_WRITE)).queue();
        guild.createTextChannel(NOTES).setTopic("Free space for all players to leave notes and discuss ideas.").addPermissionOverride(guild.getPublicRole(), Collections.emptySet(), EnumSet.of(Permission.USE_SLASH_COMMANDS)).queue();
        guild.createTextChannel(LOOKUP).setTopic("Used for searching keywords to discover new locations or characters. Type a search term and wait for a response.").addPermissionOverride(guild.getPublicRole(), Collections.emptySet(), EnumSet.of(Permission.USE_SLASH_COMMANDS)).queue();

        guild.createCategory(CHAT_LOGS).addPermissionOverride(guild.getPublicRole(), Collections.emptySet(), EnumSet.of(Permission.MESSAGE_WRITE)).queue();
        guild.createCategory(LOCATIONS).queue();
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        TextChannel textChannel = event.getMessage().getTextChannel();
        if (textChannel.getName().equals(ADMIN_CONSOLE)) {
            
        }
    }
}
