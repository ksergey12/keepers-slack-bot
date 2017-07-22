package ua.com.juja.microservices.keepers.slackbot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import ua.com.juja.microservices.keepers.slackbot.service.KeeperService;
import ua.com.juja.microservices.keepers.slackbot.service.impl.SlackNameHandlerService;

/**
 * @author Nikolay Horushko
 */
@RestController
public class KeepersSlackCommandController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private KeeperService keeperService;
    private SlackNameHandlerService slackNameHandlerService;

    public KeepersSlackCommandController(KeeperService keeperService, SlackNameHandlerService slackNameHandlerService) {
        this.keeperService = keeperService;
        this.slackNameHandlerService = slackNameHandlerService;
    }
}
