package ua.com.juja.microservices.keepers.slackbot.model;

import java.util.Map;

/**
 * @author
 */
public class SlackParsedCommand {
   private String from;
   private String text;
   private Map<String, UserDTO> users;

    public SlackParsedCommand(String from, String text, Map<String, UserDTO> users) {
        this.from = from;
        this.text = text;
        this.users = users;
        //todo task #11
    }
}
