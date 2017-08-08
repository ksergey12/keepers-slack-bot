package ua.com.juja.microservices.keepers.slackbot.controller;

import me.ramswaroop.jbot.core.slack.models.RichMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ua.com.juja.microservices.keepers.slackbot.exception.WrongCommandFormatException;
import ua.com.juja.microservices.keepers.slackbot.model.SlackParsedCommand;
import ua.com.juja.microservices.keepers.slackbot.model.dto.UserDTO;
import ua.com.juja.microservices.keepers.slackbot.model.request.KeeperRequest;
import ua.com.juja.microservices.keepers.slackbot.service.KeeperService;
import ua.com.juja.microservices.keepers.slackbot.service.impl.SlackNameHandlerService;

import java.util.Arrays;

/**
 * @author Nikolay Horushko
 * @author Dmitriy Lyashenko
 */
@RestController
public class KeepersSlackCommandController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${slack.slashCommandToken}")
    private String slackToken;

    private KeeperService keeperService;
    private SlackNameHandlerService slackNameHandlerService;

    public KeepersSlackCommandController(KeeperService keeperService, SlackNameHandlerService slackNameHandlerService) {
        this.keeperService = keeperService;
        this.slackNameHandlerService = slackNameHandlerService;
    }

    @PostMapping(value = "/commands/keeper-add", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RichMessage onReceiveSlashCommandKeeperAdd(@RequestParam("token") String token,
                                                     @RequestParam("user_name") String fromUser,
                                                     @RequestParam("text") String text) {

        logger.debug("Received slash command KeeperAdd: from user: [{}] command: [{}] token: [{}]",
                fromUser, text, token);

        if (!token.equals(slackToken)) {
            logger.warn("Received invalid slack token: [{}] in command KeeperAdd for user: [{}]", token, fromUser);
            return new RichMessage("Sorry! You're not lucky enough to use our slack command.");
        }

        logger.debug("Started create slackParsedCommand and create keeper request");
        SlackParsedCommand slackParsedCommand = slackNameHandlerService.createSlackParsedCommand(fromUser, text);
        KeeperRequest keeperRequest = new KeeperRequest(slackParsedCommand.getFromUser().getUuid(),
                                                        receiveToUser(slackParsedCommand).getUuid(),
                                                        receiveToDirections(slackParsedCommand));

        logger.debug("Finished create slackParsedCommand and create keeper request");

        String[] result = keeperService.sendKeeperAddRequest(keeperRequest);
        logger.debug("Received response from Keeper service: [{}]", Arrays.toString(result));

        String response = "ERROR. Something went wrong. Keeper was not created :(";

        if (result.length > 0) {
            response = String.format("Thanks, we added a new Keeper: %s in direction: %s",
                    slackParsedCommand.getFirstUser().getSlack(), keeperRequest.getDirection());
        }

        logger.info("Keeper command processed : user: [{}] text: [{}] and sent response into slack: [{}]",
                fromUser, text, response);

        return new RichMessage(response);
    }

    @PostMapping(value = "/commands/keeper-dismiss", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RichMessage onReceiveSlashCommandKeeperDismiss(@RequestParam("token") String token,
                                                      @RequestParam("user_name") String fromUser,
                                                      @RequestParam("text") String text) {

        logger.debug("Received slash command KeeperDismiss: from user: [{}] command: [{}] token: [{}]",
                fromUser, text, token);

        if (!token.equals(slackToken)) {
            logger.warn("Received invalid slack token: [{}] in command KeeperDismiss for user: [{}]", token, fromUser);
            return new RichMessage("Sorry! You're not lucky enough to use our slack command.");
        }

        logger.debug("Started create slackParsedCommand and create keeper request");
        SlackParsedCommand slackParsedCommand = slackNameHandlerService.createSlackParsedCommand(fromUser, text);
        KeeperRequest keeperRequest = new KeeperRequest(slackParsedCommand.getFromUser().getUuid(),
                                                        receiveToUser(slackParsedCommand).getUuid(),
                                                        receiveToDirections(slackParsedCommand));

        logger.debug("Finished create slackParsedCommand and create keeper request");

        String[] result = keeperService.sendKeeperDismissRequest(keeperRequest);
        logger.debug("Received response from Keeper service: [{}]", Arrays.toString(result));

        String response = "ERROR. Something went wrong. Keeper was not dismissed :(";

        if (result.length > 0) {
            response = String.format("Keeper: %s in direction: %s dismissed" ,
                    slackParsedCommand.getFirstUser().getSlack(), keeperRequest.getDirection());
        }

        logger.info("Keeper command processed : user: [{}] text: [{}] and sent response into slack: [{}]",
                fromUser, text, response);

        return new RichMessage(response);
    }

    private UserDTO receiveToUser(SlackParsedCommand slackParsedCommand) {

        int userCount = slackParsedCommand.getUserCount();

        if (userCount > 1) {
            throw new WrongCommandFormatException(String.format("We found %d slack names in your command: '%s' " +
                            " You can't make more than one Keepers or dismiss more then one Keepers on one direction.",
                    slackParsedCommand.getUserCount(), slackParsedCommand.getText()));
        }

        if (userCount == 0) {
            throw new WrongCommandFormatException(String.format("We didn't find slack name in your command. '%s'" +
                    " You must write user's slack name to make Keeper or dismiss Keeper.", slackParsedCommand.getText()));
        }

        return slackParsedCommand.getFirstUser();
    }

    private String receiveToDirections(SlackParsedCommand parsedCommand){

        String textWithoutSlackNames = parsedCommand.getTextWithoutSlackNames();

        if (textWithoutSlackNames.length() == 0){
            throw new WrongCommandFormatException(String.format("We didn't find direction in your command: '%s'",
                    parsedCommand.getText()));
        }

        if (textWithoutSlackNames.split(" ").length > 1){
            throw new WrongCommandFormatException(String.format("We found several directions in your command: '%s' " +
                    " You can make Keeper or dismiss Keeper on one direction only.", parsedCommand.getTextWithoutSlackNames()));
        }

        return parsedCommand.getTextWithoutSlackNames();
    }
}
