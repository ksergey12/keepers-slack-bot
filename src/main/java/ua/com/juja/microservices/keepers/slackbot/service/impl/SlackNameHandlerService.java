package ua.com.juja.microservices.keepers.slackbot.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ua.com.juja.microservices.keepers.slackbot.service.UserService;

import javax.inject.Inject;

/**
 * @author
 */
@Service
public class SlackNameHandlerService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private UserService userService;

    @Inject
    public SlackNameHandlerService(UserService userService) {
        this.userService = userService;
    }

    //todo task #9
}
