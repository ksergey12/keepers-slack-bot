package ua.com.juja.microservices.keepers.slackbot.model;

import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import ua.com.juja.microservices.keepers.slackbot.exception.WrongCommandFormatException;
import ua.com.juja.microservices.keepers.slackbot.model.dto.UserDTO;

import java.util.*;

/**
 * @author Konstantin Sergey
 */
@ToString(exclude = {"SLACK_NAME_PATTERN", "logger"})
public class SlackParsedCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String SLACK_NAME_PATTERN = "@([a-zA-z0-9\\.\\_\\-]){1,21}";
    private UserDTO fromUser;
    private String text;
    private List<UserDTO> usersInText;

    public SlackParsedCommand(UserDTO fromUser, String text, List<UserDTO> usersInText) {
        this.fromUser = fromUser;
        this.text = text;
        this.usersInText = usersInText;

        logger.debug("SlackParsedCommand created with parameters: " +
                        "fromSlackName: {} text: {} userCountInText {} users: {}",
                fromUser, text, usersInText.size(), usersInText.toString());
    }

    public List<UserDTO> getAllUsersFromText() {
        return usersInText;
    }

    public UserDTO getFirstUserFromText() {
        if (usersInText.size() == 0) {
            logger.warn("The text: '{}' doesn't contain any slack names", text);
            throw new WrongCommandFormatException(String.format("The text '%s' doesn't contain any slack names", text));
        } else {
            return usersInText.get(0);
        }
    }

    public String getTextWithoutSlackNames() {
        String result = text.replaceAll(SLACK_NAME_PATTERN, "");
        result = result.replaceAll("\\s+", " ").trim();
        return result;
    }

    public UserDTO getFromUser() {
        return fromUser;
    }

    public String getText() {
        return text;
    }

    public int getUserCountInText() {
        return usersInText.size();
    }
}
