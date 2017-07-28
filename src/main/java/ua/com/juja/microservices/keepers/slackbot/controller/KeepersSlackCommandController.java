package ua.com.juja.microservices.keepers.slackbot.controller;

import me.ramswaroop.jbot.core.slack.models.RichMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.com.juja.microservices.keepers.slackbot.model.SlackParsedCommand;
import ua.com.juja.microservices.keepers.slackbot.service.KeeperService;
import ua.com.juja.microservices.keepers.slackbot.service.impl.SlackNameHandlerService;

import java.util.List;

/**
 * @author Nikolay Horushko
 * @author Konstantin Sergey
 */
@RestController
public class KeepersSlackCommandController {
    @Value("${slack.slashCommandToken}")
    private String slackToken;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private KeeperService keeperService;
    private SlackNameHandlerService slackNameHandlerService;

    public KeepersSlackCommandController(KeeperService keeperService, SlackNameHandlerService slackNameHandlerService) {
        this.keeperService = keeperService;
        this.slackNameHandlerService = slackNameHandlerService;
    }

    @PostMapping(value = "/commands/keeper", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RichMessage onReceiveSlashCommandGetKeeperDirections(@RequestParam("token") String token,
                                                                @RequestParam("user_name") String fromUser,
                                                                @RequestParam("text") String text) {
        logger.debug("Received slash command GetKeeperDirections: from user: [{}] command: [{}] token: [{}]",
                fromUser, text, token);

        if (!token.equals(slackToken)) {
            logger.warn("Received invalid slack token: [{}] in command Keeper for user: [{}]", token, fromUser);
            return new RichMessage("Sorry! You're not lucky enough to use our slack command.");
        }
        logger.debug("Started create slackParsedCommand");
        SlackParsedCommand slackParsedCommand = slackNameHandlerService.createSlackParsedCommand(fromUser, text);
        logger.debug("Finished create slackParsedCommand: " + slackParsedCommand.toString());

        String keeperUuid = slackParsedCommand.getFirstUser().getUuid();
        String keeperSlackName = slackParsedCommand.getFirstUser().getSlack();

        List<String> result = keeperService.getKeeperDirections(keeperUuid);
        logger.debug("Received response from Keeper service: [{}]", result.toString());

        String response = "ERROR. Something went wrong and we didn't get keeper directions";
        if (result.size() == 0) {
            response = "За Хранителем " + keeperSlackName + " сейчас не закреплено ни одно направление.";
        }
        if (result.size() > 0) {
            response = "Закрепленные направления за Хранителем " + keeperSlackName
                    + " : " + result.toString();
        }
        logger.info("GetKeeperDirections command processed : user: [{}] text: [{}] and sent response to slack: [{}]",
                fromUser, text, response);

        return new RichMessage(response);
    }
}
