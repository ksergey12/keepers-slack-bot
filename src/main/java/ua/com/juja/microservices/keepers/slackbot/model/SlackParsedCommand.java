package ua.com.juja.microservices.keepers.slackbot.model;

import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.com.juja.microservices.keepers.slackbot.exception.WrongCommandFormatException;
import ua.com.juja.microservices.keepers.slackbot.model.dto.UserDTO;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Konstantin Sergey
 */
@ToString(exclude={"SLACK_NAME_PATTERN","logger"})
public class SlackParsedCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String SLACK_NAME_PATTERN = "@([a-zA-z0-9\\.\\_\\-]){1,21}";
    private String fromSlackName;
    private String text;
    private Map<String, UserDTO> users;
    private List<String> slackNamesInText;
    private int userCount;

    public SlackParsedCommand(String fromSlackName, String text, Map<String, UserDTO> users) {
        if (!fromSlackName.startsWith("@")) {
            logger.debug("add '@' to slack name [{}]", fromSlackName);
            fromSlackName = "@" + fromSlackName;
        }
        this.fromSlackName = fromSlackName;
        this.text = text;
        this.users = users;
        slackNamesInText = getSlackNames();
        userCount = slackNamesInText.size();
        logger.debug("SlackParsedCommand created with parameters: " +
                "fromSlackName: {} text: {} slackNamesInText in the text {} users: {}",
                fromSlackName, text, slackNamesInText.toString(), users.toString());
    }

    public List<UserDTO> getAllUsers() {
        checkSlackNamesPresence();
        Map<String, UserDTO> usersCopy = new HashMap<>();
        usersCopy.putAll(users);
        usersCopy.remove(fromSlackName);
        logger.debug("Found users {} in text: [{}]", users.toString(), text);
        return new LinkedList(usersCopy.values());
    }

    public UserDTO getFirstUser() {
        checkSlackNamesPresence();
        UserDTO firstUser = users.get(slackNamesInText.get(0));
        logger.debug("Found user: {} in text: [{}]", firstUser.toString(), text);
        return firstUser;
    }

    public String getTextWithoutSlackNames(){
        String result = text.replaceAll(SLACK_NAME_PATTERN, "");
        result = result.replaceAll("\\s+"," ").trim();
        return result;
    }

    public UserDTO getFromUser() {
        return users.get(fromSlackName);
    }

    public String getText() {
        return text;
    }

    public int getUserCount() {
        return userCount;
    }

    private List<String> getSlackNames() {
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
            throw new WrongCommandFormatException(String.format("The text '%s' doesn't contain any slackName", text));
        }
    }
}
