package ua.com.juja.microservices.keepers.slackbot.service;

import ua.com.juja.microservices.keepers.slackbot.model.UserDTO;

import java.util.List;

/**
 * @author Nikolay Horushko
 */
public interface UserService {
    List<UserDTO> findUsersBySlackNames(List<String> slackNames);
}
