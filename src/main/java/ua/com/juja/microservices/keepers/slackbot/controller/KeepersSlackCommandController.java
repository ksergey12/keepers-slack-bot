package ua.com.juja.microservices.keepers.slackbot.controller;

import me.ramswaroop.jbot.core.slack.models.RichMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ua.com.juja.microservices.keepers.slackbot.model.SlackParsedCommand;
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
        KeeperRequest keeperRequest = new KeeperRequest(slackParsedCommand);
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
}
