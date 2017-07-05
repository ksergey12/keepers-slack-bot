package ua.com.juja.microservices.keepers.slackbot.dao;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import ua.com.juja.microservices.keepers.slackbot.model.UserDTO;

import java.util.List;

/**
 * @author
 */
@Repository
public class RestUserRepository implements UserRepository{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<UserDTO> findUsersBySlackNames(List<String> slackNames) {
        //todo task #7
        return null;
    }
}
