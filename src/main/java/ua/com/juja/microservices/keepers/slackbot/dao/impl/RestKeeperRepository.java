package ua.com.juja.microservices.keepers.slackbot.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import ua.com.juja.microservices.keepers.slackbot.dao.KeeperRepository;

import javax.inject.Inject;

/**
 * @author Nikolay Horushko
 */
@Repository
public class RestKeeperRepository extends AbstractRestRepository implements KeeperRepository {
    private RestTemplate restTemplate;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    public RestKeeperRepository(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
