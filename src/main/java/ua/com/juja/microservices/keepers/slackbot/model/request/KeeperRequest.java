package ua.com.juja.microservices.keepers.slackbot.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import ua.com.juja.microservices.keepers.slackbot.exception.WrongCommandFormatException;
import ua.com.juja.microservices.keepers.slackbot.model.SlackParsedCommand;
import ua.com.juja.microservices.keepers.slackbot.model.dto.UserDTO;

/**
 * @author Dmitriy Lyashenko
 */
@Getter
@ToString
public class KeeperRequest {

    @JsonProperty("from")
    private String from;
    @JsonProperty("uuid")
    private String uuid;
    @JsonProperty("direction")
    private String direction;

    public KeeperRequest(String from, String uuid, String direction) {
        this.from = from;
        this.uuid = uuid;
        this.direction = direction;
    }

    public KeeperRequest(SlackParsedCommand parsedCommand) {
        this.from = parsedCommand.getFromUser().getUuid();
        this.uuid = receiveToUser(parsedCommand).getUuid();
        this.direction = receiveToDirections(parsedCommand);
    }

    private UserDTO receiveToUser(SlackParsedCommand slackParsedCommand) {
        if (slackParsedCommand.getUserCount() > 1) {
            throw new WrongCommandFormatException(String.format("We found %d slack names in your command: '%s' " +
                            " You can't make two Keepers on one direction.", slackParsedCommand.getUserCount(),
                    slackParsedCommand.getText()));
        }

        if (slackParsedCommand.getUserCount() == 0) {
            throw new WrongCommandFormatException(String.format("We didn't find slack name in your command. '%s'" +
                    " You must write user's slack name to make Keeper.", slackParsedCommand.getText()));
        }

        return slackParsedCommand.getFirstUser();
    }

    private String receiveToDirections(SlackParsedCommand parsedCommand){
        if (parsedCommand.getTextWithoutSlackNames().length() == 0){
            throw new WrongCommandFormatException(String.format("We didn't find direction in your command: '%s'",
                    parsedCommand.getText()));
        }

        if (parsedCommand.getTextWithoutSlackNames().split(" ").length > 1){
            throw new WrongCommandFormatException(String.format("We found several directions in your command: '%s' " +
                            " You can make Keeper only on one direction.", parsedCommand.getTextWithoutSlackNames()));
        }

        return parsedCommand.getTextWithoutSlackNames();
    }
}
