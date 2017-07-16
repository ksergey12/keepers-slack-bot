package ua.com.juja.microservices.keepers.slackbot.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.com.juja.microservices.keepers.slackbot.exceptions.WrongCommandFormatException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Konstantin Sergey
 */
public class SlackParsedCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String SLACK_NAME_PATTERN = "@([a-zA-z0-9\\.\\_\\-]){1,21}";
    private String from;
    private String text;
    private Map<String, UserDTO> users;
    private List<String> slackNames;
    private int userCount;

    public SlackParsedCommand(String from, String text, Map<String, UserDTO> users) {
        if (!from.startsWith("@")) {
            logger.debug("add '@' to slack name [{}]", from);
            from = "@" + from;
        }
        this.from = from;
        this.text = text;
        this.users = users;
        slackNames = receiveSlackNames();
        userCount = slackNames.size();
    }

    public List<UserDTO> receiveAllUsers() {
        checkSlackNamesPresence();
        Map<String, UserDTO> usersCopy = new HashMap<>();
        usersCopy.putAll(users);
        usersCopy.remove(from);
        logger.debug("Found {} users in text: [{}]", userCount, text);
        return new LinkedList(usersCopy.values());
    }

    public UserDTO receiveFirstUser() {
        checkSlackNamesPresence();
        UserDTO firstUser = users.get(slackNames.get(0));
        logger.debug("Found user: {} in text: [{}]", firstUser.toString(), text);
        return firstUser;
    }

    public UserDTO receiveFromUser() {
        return users.get(from);
    }

    public String getText() {
        return text;
    }

    public int getUserCount() {
        return userCount;
    }

    private List<String> receiveSlackNames() {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile(SLACK_NAME_PATTERN);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    private void checkSlackNamesPresence() {
        if (userCount == 0) {
            logger.warn("The text: [{}] doesn't contain slack name.");
            throw new WrongCommandFormatException(String.format("The text '%s' doesn't contains slackName", text));
        }
    }
}
