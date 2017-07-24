package ua.com.juja.microservices.keepers.slackbot.service;

import ua.com.juja.microservices.keepers.slackbot.model.request.KeeperRequest;

/**
 * @author Nikolay Horushko
 * @author Dmitriy Lyashenko
 */
public interface KeeperService {
    String[] sendKeeperAddRequest(KeeperRequest keeperRequest);
}
