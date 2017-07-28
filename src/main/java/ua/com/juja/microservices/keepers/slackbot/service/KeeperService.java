package ua.com.juja.microservices.keepers.slackbot.service;

import java.util.List;

/**
 * @author Nikolay Horushko
 */
public interface KeeperService {
    List<String> getKeeperDirections(String uuid);
}
