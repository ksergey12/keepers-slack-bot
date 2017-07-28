package ua.com.juja.microservices.keepers.slackbot.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ua.com.juja.microservices.keepers.slackbot.dao.KeeperRepository;
import ua.com.juja.microservices.keepers.slackbot.exception.ApiError;
import ua.com.juja.microservices.keepers.slackbot.exception.KeeperExchangeException;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * @author Nikolay Horushko
 * @author Konstantin Sergey
 */
@Repository
public class RestKeeperRepository extends AbstractRestRepository implements KeeperRepository {
    private RestTemplate restTemplate;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    public RestKeeperRepository(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${keepers.baseURL}")
    private String urlBase;
    @Value("${endpoint.getKeeperDirections}")
    private String urlGetKeeperDirections;

    @Override
    public List<String> getKeeperDirections(String uuid) {
        HttpEntity<String> request = new HttpEntity<>(uuid, setupBaseHttpHeaders());
        List<String> result;

        try {
            logger.debug("Started request to Keepers service. Request is : [{}]", request.toString());
            ResponseEntity<String[]> response = restTemplate.exchange(
                    urlBase + urlGetKeeperDirections + "/" + uuid, HttpMethod.GET, request, String[].class);
            result = Arrays.asList(response.getBody());
            logger.debug("Finished request to Keepers service. Response is: [{}]", response.toString());
        } catch (HttpClientErrorException ex) {
            ApiError error = convertToApiError(ex);
            logger.warn("Keepers service returned an error: [{}]", error);
            throw new KeeperExchangeException(convertToApiError(ex), ex);
        }

        logger.info("Got keeper directions: [{}]", result.toString());
        return result;
    }
}
