package ua.com.juja.microservices.keepers.slackbot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import ua.com.juja.microservices.keepers.slackbot.exception.BaseBotException;
import ua.com.juja.microservices.keepers.slackbot.model.dto.CustomRichMessage;
import ua.com.juja.microservices.keepers.slackbot.service.KeeperService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Nikolay Horushko
 * @author Dmitriy Lyashenko
 * @author Konstantin Sergey
 */
@RestController
@RequestMapping("/commands/keeper")
public class KeepersSlackCommandController {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final String SORRY_MESSAGE = "Sorry! You're not lucky enough to use our slack command.";
    private final String IN_PROGRESS = "In progress...";

    @Value("${slack.slashCommandToken}")
    private String slackToken;

    private KeeperService keeperService;
    private RestTemplate restTemplate;

    @Inject
    public KeepersSlackCommandController(KeeperService keeperService,
                                         RestTemplate restTemplate) {
        this.keeperService = keeperService;
        this.restTemplate = restTemplate;
    }

    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void addKeeper(@RequestParam("token") String token,
                         @RequestParam("user_name") String fromUser,
                         @RequestParam("text") String text,
                         @RequestParam("response_url") String responseUrl,
                         HttpServletResponse httpServletResponse) throws IOException {
        try {
            LOGGER.debug("Received slash command KeeperAdd: from user: [{}] command: [{}] token: [{}] responseUrl: [{}]",
                    fromUser, text, token, responseUrl);

            if (!token.equals(slackToken)) {
                LOGGER.warn("Received invalid slack token: [{}] in command KeeperAdd for user: [{}]", token, fromUser);
                sendQuickResponse(httpServletResponse, SORRY_MESSAGE);
            } else {
                sendQuickResponse(httpServletResponse, IN_PROGRESS);
                String response = keeperService.sendKeeperAddRequest(fromUser, text);
                LOGGER.info("KeeperAdd command processed : user: [{}] text: [{}] and sent response into slack: [{}]",
                        fromUser, text, response);
                sendDelayedResponse(responseUrl, response);
            }
        } catch (BaseBotException bex){
            sendBBEMessage(responseUrl, bex);
        } catch (Exception ex){
            sendEMessage(responseUrl, ex);
        }
    }

    @PostMapping(value = "/dismiss", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void dismissKeeper(@RequestParam("token") String token,
                             @RequestParam("user_name") String fromUser,
                             @RequestParam("text") String text,
                             @RequestParam("response_url") String responseUrl,
                             HttpServletResponse httpServletResponse) {
        try {
            LOGGER.debug("Received slash command KeeperDismiss: from user: [{}] command: [{}] token: [{}]",
                    fromUser, text, token);

            if (!token.equals(slackToken)) {
                LOGGER.warn("Received invalid slack token: [{}] in command KeeperDismiss for user: [{}]", token, fromUser);
                sendQuickResponse(httpServletResponse, SORRY_MESSAGE);
            } else {
                sendQuickResponse(httpServletResponse, IN_PROGRESS);
                String response = keeperService.sendKeeperDismissRequest(fromUser, text);
                LOGGER.info("KeeperDismiss command processed : user: [{}] text: [{}] and sent response into slack: [{}]",
                        fromUser, text, response);
                sendDelayedResponse(responseUrl, response);
            }
        } catch (BaseBotException bex){
            sendBBEMessage(responseUrl, bex);
        } catch (Exception ex){
            sendEMessage(responseUrl, ex);
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void getKeeperDirections(@RequestParam("token") String token,
                                   @RequestParam("user_name") String fromUser,
                                   @RequestParam("text") String text,
                                   @RequestParam("response_url") String responseUrl,
                                   HttpServletResponse httpServletResponse) {
        try {
            LOGGER.debug("Received slash command GetKeeperDirections: from user: [{}] command: [{}] token: [{}]",
                    fromUser, text, token);

            if (!token.equals(slackToken)) {
                LOGGER.warn("Received invalid slack token: [{}] in command Keeper for user: [{}]", token, fromUser);
                sendQuickResponse(httpServletResponse, SORRY_MESSAGE);
            } else {
                sendQuickResponse(httpServletResponse, IN_PROGRESS);
                String response = keeperService.getKeeperDirections(fromUser, text);
                LOGGER.info("GetKeeperDirections command processed : user: [{}] text: [{}] and sent response to slack: [{}]",
                        fromUser, text, response);
                sendDelayedResponse(responseUrl, response);
            }
        } catch (BaseBotException bex){
            sendBBEMessage(responseUrl, bex);
        } catch (Exception ex){
            sendEMessage(responseUrl, ex);
        }
    }

    private void sendQuickResponse(HttpServletResponse httpServletResponse, String message) throws IOException {
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter printWriter = httpServletResponse.getWriter()) {
            printWriter.print(message);
            printWriter.flush();
        }
        LOGGER.info("Sent a quick response with message '{}'", message);
    }

    private void sendDelayedResponse(String responseUrl, String response) {
        String slackAnswer = restTemplate.postForObject(responseUrl, new CustomRichMessage(response), String.class);
        LOGGER.info("Slack answered: [{}]", slackAnswer == null ? "null" : slackAnswer);
    }

    private void sendBBEMessage(String responseUrl, BaseBotException bex) {
        LOGGER.warn("There was an exceptional situation: [{}]", bex.detailMessage());
        try {
            String slackAnswer = restTemplate.postForObject(responseUrl, new CustomRichMessage(bex.getMessage()), String.class);
            LOGGER.warn("Slack answered: [{}]", slackAnswer == null ? "null" : slackAnswer);
        } catch (Exception e){
            LOGGER.warn("Nested exception: [{}]", e.getMessage());
        }
    }

    private void sendEMessage(String responseUrl, Exception ex) {
        LOGGER.warn("There was an exceptional situation: [{}]", ex.getMessage());
        try {
            String slackAnswer = restTemplate.postForObject(responseUrl, new CustomRichMessage(ex.getMessage()), String.class);
            LOGGER.warn("Slack answered: [{}]", slackAnswer == null ? "null" : slackAnswer);
        } catch (Exception e) {
            LOGGER.warn("Nested exception: [{}]", e.getMessage());
        }
    }
}