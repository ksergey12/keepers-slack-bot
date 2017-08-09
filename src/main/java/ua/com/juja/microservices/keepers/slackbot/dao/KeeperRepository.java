package ua.com.juja.microservices.keepers.slackbot.dao;

import ua.com.juja.microservices.keepers.slackbot.model.request.KeeperRequest;

import java.util.List;

/**
 * @author Nikolay Horushko
 * @author Dmitriy Lyashenko
 */
public interface KeeperRepository {
    String[] addKeeper(KeeperRequest keeperRequest);

    String[] dismissKeeper(KeeperRequest keeperRequest);

    List<String> getKeeperDirections(KeeperRequest keeperRequest);
}
