package ua.com.juja.microservices.keepers.slackbot.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ua.com.juja.microservices.keepers.slackbot.dao.KeeperRepository;
import ua.com.juja.microservices.keepers.slackbot.service.KeeperService;

import javax.inject.Inject;
import java.util.List;


/**
 * @author Nikolay Horushko
 * @author Konstantin Sergey
 */
@Service
public class DefaultKeeperService implements KeeperService {
    private KeeperRepository keeperRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    public DefaultKeeperService(KeeperRepository keeperRepository) {
        this.keeperRepository = keeperRepository;
    }

    @Override
    public List<String> getKeeperDirections(String uuid) {
        logger.info("Received request to get directions of keeper with uuid: [{}]", uuid);
        List<String> result = keeperRepository.getKeeperDirections(uuid);
        logger.debug("Received response from keeperRepository: [{}]", result.toString());
        return result;
    }
}
