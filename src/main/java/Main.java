import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import listener.DetectiveListener;
import model.config.YmlConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
        File file = new File("bot.yml");
        YmlConfig config = new ObjectMapper(new YAMLFactory()).readValue(file, YmlConfig.class);
        DetectiveListener detectiveListener = new DetectiveListener(config);
        JDA jda = JDABuilder.createDefault(config.getDiscord().getToken())
                .addEventListeners(detectiveListener)
                .setActivity(Activity.listening("instructions"))
                .build();
        System.out.println("ID " + jda.getSelfUser().getIdLong());
        detectiveListener.setId(jda.getSelfUser().getIdLong());
        jda.awaitReady();
    }
}
