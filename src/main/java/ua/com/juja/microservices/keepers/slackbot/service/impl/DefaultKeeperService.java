package ua.com.juja.microservices.keepers.slackbot.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ua.com.juja.microservices.keepers.slackbot.dao.KeeperRepository;
import ua.com.juja.microservices.keepers.slackbot.model.request.KeeperRequest;
import ua.com.juja.microservices.keepers.slackbot.service.KeeperService;

import javax.inject.Inject;
import java.util.List;
import java.util.Arrays;


/**
 * @author Nikolay Horushko
 * @author Dmitriy Lyashenko
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
    public String[] sendKeeperAddRequest(KeeperRequest keeperRequest) {
        logger.debug("Received KeeperRequest: [{}]", keeperRequest.toString());
        String[] ids = keeperRepository.addKeeper(keeperRequest);
        logger.info("Added Keeper: [{}]", Arrays.toString(ids));
        return ids;
    }

    @Override
    public String[] sendKeeperDismissRequest(KeeperRequest keeperRequest) {
        logger.debug("Received KeeperRequest: [{}]", keeperRequest.toString());
        String[] ids = keeperRepository.dismissKeeper(keeperRequest);
        logger.info("Dismissed Keeper: [{}]", Arrays.toString(ids));
        return ids;
    }

    @Override
    public List<String> getKeeperDirections(KeeperRequest keeperRequest) {
        logger.debug("Received request to get directions of keeper with uuid: [{}]", keeperRequest.toString());
        List<String> result = keeperRepository.getKeeperDirections(keeperRequest);
        logger.info("Received response from keeperRepository: [{}]", result.toString());
        return result;
    }
}