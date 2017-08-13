package ua.com.juja.microservices.keepers.slackbot.service;

/**
 * @author Nikolay Horushko
 * @author Dmitriy Lyashenko
 */
public interface KeeperService {
    String sendKeeperAddRequest(String fromUser, String text);

    String sendKeeperDismissRequest(String fromUser, String text);

    String getKeeperDirections(String fromUser, String text);
}
