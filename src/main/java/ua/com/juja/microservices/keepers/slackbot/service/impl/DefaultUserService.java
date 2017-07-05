package ua.com.juja.microservices.keepers.slackbot.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ua.com.juja.microservices.keepers.slackbot.model.UserDTO;
import ua.com.juja.microservices.keepers.slackbot.service.UserService;

import java.util.List;

/**
 * @author
 */
@Service
public class DefaultUserService implements UserService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<UserDTO> findUsersBySlackNames(List<String> slackNames) {
        //todo task #7
        return null;
    }
}
