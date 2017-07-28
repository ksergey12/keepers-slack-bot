package ua.com.juja.microservices.keepers.slackbot.dao;

import java.util.List;

/**
 * @author Nikolay Horushko
 */
public interface KeeperRepository {
    List<String> getKeeperDirections(String uuid);
}
