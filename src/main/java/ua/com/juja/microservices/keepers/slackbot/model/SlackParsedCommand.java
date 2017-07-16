package ua.com.juja.microservices.keepers.slackbot.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author Konstantin Sergey
 */
public class SlackParsedCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String SLACK_NAME_PATTERN = "@([a-zA-z0-9\\.\\_\\-]){1,21}";
    private String from;
    private String text;
    private Map<String, UserDTO> users;
    private int usersCount;

    public SlackParsedCommand(String from, String text, Map<String, UserDTO> users) {
        this.from = from;
        this.text = text;
        this.users = users;
    }

    public UserDTO receiveFirstUser() {
        return null;
    }

    public List<UserDTO> receiveAllUsers() {
        return null;
    }

    public UserDTO receiveFromUser() {
        return users.get(from);
    }

    public String getText() {
        return text;
    }

    public int getUsersCount() {
        return usersCount;
    }
}
