package ua.com.juja.microservices.keepers.slackbot.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.com.juja.microservices.keepers.slackbot.exception.WrongCommandFormatException;
import ua.com.juja.microservices.keepers.slackbot.model.dto.UserDTO;
import ua.com.juja.microservices.keepers.slackbot.utils.Utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Konstantin Sergey
 */
@ToString(exclude = {"slackNamePattern", "logger"})
@EqualsAndHashCode
public class SlackParsedCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private String slackNamePattern;
    private UserDTO fromUser;
    private String text;
    private List<UserDTO> usersInText;

    public SlackParsedCommand(UserDTO fromUser, String text, List<UserDTO> usersInText) {
        this.fromUser = fromUser;
        this.text = text;
        this.usersInText = usersInText;
        slackNamePattern = Utils.getProperty(
                "application.properties",
                "keepers.slackNamePattern"
        );
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
        String result = text.replaceAll(slackNamePattern, "");
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

    public Map<String, UserDTO> getUsersWithTokens(String[] tokens) {
        logger.debug("Recieve tokens: [{}] for searching. in the text: [{}]", tokens, text);
        List<Token> sortedTokenList = receiveTokensWithPositionInText(tokens);
        return findSlackNameInTokenText(sortedTokenList);
    }

    private List<Token> receiveTokensWithPositionInText(String[] tokens) {
        Set<Token> result = new TreeSet<>();
        for (String token : tokens) {
            if (!text.contains(token)) {
                throw new WrongCommandFormatException(String.format("Token '%s' didn't find in the string '%s'",
                        token, text));
            }
            int tokenCounts = text.split(token).length - 1;
            if (tokenCounts > 1) {
                throw new WrongCommandFormatException(String.format("The text '%s' contains %d tokens '%s', " +
                        "but expected 1", text, tokenCounts, token));
            }
            result.add(new Token(token, text.indexOf(token)));
        }
        return new ArrayList<>(result);
    }

    private Map<String, UserDTO> findSlackNameInTokenText(List<Token> sortedTokenList) {
        Map<String, UserDTO> result = new HashMap<>();

        for (int index = 0; index < sortedTokenList.size(); index++) {
            Token currentToken = sortedTokenList.get(index);
            Pattern pattern = Pattern.compile(slackNamePattern);
            Matcher matcher = pattern.matcher(text.substring(text.indexOf(currentToken.getToken())));
            if (matcher.find()) {
                String foundedSlackName = matcher.group().trim();
                int indexFoundedSlackName = text.indexOf(foundedSlackName);
                for (int j = index + 1; j < sortedTokenList.size(); j++) {
                    if (indexFoundedSlackName > sortedTokenList.get(j).getPositionInText()) {
                        logger.warn("The text: [{}] doesn't contain slack name for token: [{}]",
                                text, currentToken.getToken());
                        throw new WrongCommandFormatException(String.format("The text '%s' doesn't contain slackName " +
                                "for token '%s'", text, currentToken.getToken()));
                    }
                }
                addFoundedSlackNameToResult(currentToken, foundedSlackName, result);
            } else {
                logger.warn("The text: [{}] doesn't contain slack name for token: [{}]",
                        text, sortedTokenList.get(index).getToken());
                throw new WrongCommandFormatException(String.format("The text '%s' " +
                        "doesn't contain slackName for token '%s'", text, sortedTokenList.get(index).getToken()));
            }
        }
        return result;
    }

    private void addFoundedSlackNameToResult(Token currentToken, String foundedSlackName, Map<String, UserDTO> result) {
        for (UserDTO item : usersInText) {
            if (item.getSlack().equals(foundedSlackName)) {
                logger.debug("Found user: {} for token:", item, currentToken.getToken());
                result.put(currentToken.getToken(), item);
            }
        }
    }

    @AllArgsConstructor
    @Getter
    class Token implements Comparable {
        private String token;
        private int positionInText;

        @Override
        public int compareTo(Object object) {
            Token thatToken = (Token) object;
            return positionInText - thatToken.getPositionInText();
        }
    }
}