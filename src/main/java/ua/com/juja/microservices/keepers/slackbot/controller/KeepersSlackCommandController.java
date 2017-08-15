package ua.com.juja.microservices.keepers.slackbot.controller;

import me.ramswaroop.jbot.core.slack.models.RichMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ua.com.juja.microservices.keepers.slackbot.service.KeeperService;

import javax.inject.Inject;

/**
 * @author Nikolay Horushko
 * @author Dmitriy Lyashenko
 * @author Konstantin Sergey
 */
@RestController
@RequestMapping("/commands/keeper")
public class KeepersSlackCommandController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${slack.slashCommandToken}")
    private String slackToken;

    private KeeperService keeperService;

    @Inject
    public KeepersSlackCommandController(KeeperService keeperService) {
        this.keeperService = keeperService;
    }

    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RichMessage addKeeper(@RequestParam("token") String token,
                                 @RequestParam("user_name") String fromUser,
                                 @RequestParam("text") String text) {

        logger.debug("Received slash command KeeperAdd: from user: [{}] command: [{}] token: [{}]",
                fromUser, text, token);

        if (!token.equals(slackToken)) {
            logger.warn("Received invalid slack token: [{}] in command KeeperAdd for user: [{}]", token, fromUser);
            return new RichMessage("Sorry! You're not lucky enough to use our slack command.");
        }

        String response = keeperService.sendKeeperAddRequest(fromUser, text);

        logger.info("Keeper command processed : user: [{}] text: [{}] and sent response into slack: [{}]",
                fromUser, text, response);

        return new RichMessage(response);
    }

    @PostMapping(value = "/dismiss", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RichMessage dismissKeeper(@RequestParam("token") String token,
                                     @RequestParam("user_name") String fromUser,
                                     @RequestParam("text") String text) {

        logger.debug("Received slash command KeeperDismiss: from user: [{}] command: [{}] token: [{}]",
                fromUser, text, token);

        if (!token.equals(slackToken)) {
            logger.warn("Received invalid slack token: [{}] in command KeeperDismiss for user: [{}]", token, fromUser);
            return new RichMessage("Sorry! You're not lucky enough to use our slack command.");
        }

        String response = keeperService.sendKeeperDismissRequest(fromUser, text);

        logger.info("Keeper command processed : user: [{}] text: [{}] and sent response into slack: [{}]",
                fromUser, text, response);

        return new RichMessage(response);
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RichMessage getKeeperDirections(@RequestParam("token") String token,
                                           @RequestParam("user_name") String fromUser,
                                           @RequestParam("text") String text) {
        logger.debug("Received slash command GetKeeperDirections: from user: [{}] command: [{}] token: [{}]",
                fromUser, text, token);

        if (!token.equals(slackToken)) {
            logger.warn("Received invalid slack token: [{}] in command Keeper for user: [{}]", token, fromUser);
            return new RichMessage("Sorry! You're not lucky enough to use our slack command.");
        }

        String response = keeperService.getKeeperDirections(fromUser, text);

        logger.info("GetKeeperDirections command processed : user: [{}] text: [{}] and sent response to slack: [{}]",
                fromUser, text, response);

        return new RichMessage(response);
    }
}