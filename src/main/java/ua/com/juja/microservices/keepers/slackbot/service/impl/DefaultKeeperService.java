package ua.com.juja.microservices.keepers.slackbot.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ua.com.juja.microservices.keepers.slackbot.dao.KeeperRepository;
import ua.com.juja.microservices.keepers.slackbot.service.KeeperService;

import javax.inject.Inject;


/**
 * @author Nikolay Horushko
 */
@Service
public class DefaultKeeperService implements KeeperService {
    private KeeperRepository keeperRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    public DefaultKeeperService(KeeperRepository keeperRepository) {
        this.keeperRepository = keeperRepository;
    }
}
