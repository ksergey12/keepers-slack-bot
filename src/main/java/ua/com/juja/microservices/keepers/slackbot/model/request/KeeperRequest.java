package ua.com.juja.microservices.keepers.slackbot.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import ua.com.juja.microservices.keepers.slackbot.model.SlackParsedCommand;

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
        this.uuid = parsedCommand.getFirstUser().getUuid();
        this.direction = parsedCommand.getTextWithoutSlackNames();
    }
}
