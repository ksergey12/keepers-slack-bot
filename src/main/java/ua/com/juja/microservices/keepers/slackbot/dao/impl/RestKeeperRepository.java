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
import ua.com.juja.microservices.keepers.slackbot.model.request.KeeperRequest;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Nikolay Horushko
 * @author Dmitriy Lyashenko
 * @author Konstantin Sergey
 */
@Repository
public class RestKeeperRepository extends AbstractRestRepository implements KeeperRepository {
    private RestTemplate restTemplate;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${keepers.baseURL}")
    private String urlBaseKeeper;

    @Inject
    public RestKeeperRepository(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String[] addKeeper(KeeperRequest keeperRequest) {
        return getCommonResponse(keeperRequest, HttpMethod.POST);
    }

    @Override
    public String[] dismissKeeper(KeeperRequest keeperRequest) {
        return getCommonResponse(keeperRequest, HttpMethod.PUT);
    }

    private String[] getCommonResponse(KeeperRequest keeperRequest, HttpMethod method) {
        logger.debug("Received KeeperRequest: [{}]", keeperRequest.toString());

        HttpEntity<KeeperRequest> request = new HttpEntity<>(keeperRequest, setupBaseHttpHeaders());
        String[] result;

        try {
            logger.debug("Started request to Keepers service. Request is : [{}]", request.toString());
            ResponseEntity<String[]> response = restTemplate.exchange(urlBaseKeeper,
                    method, request, String[].class);
            result = response.getBody();
            logger.debug("Finished request to Keepers service. Response is: [{}]", response.toString());
        } catch (HttpClientErrorException ex) {
            ApiError error = convertToApiError(ex);
            logger.warn("Keepers service returned an error: [{}]", error);
            throw new KeeperExchangeException(error, ex);
        }

        logger.info("Saved Keeper: [{}]", Arrays.toString(result));
        return result;
    }

    @Override
    public List<String> getKeeperDirections(KeeperRequest keeperRequest) {
        HttpEntity<KeeperRequest> request = new HttpEntity<>(keeperRequest, setupBaseHttpHeaders());
        List<String> result = new ArrayList<>();

        try {
            logger.debug("Started request to Keepers service. Request is : [{}]", request.toString());
            ResponseEntity<String[]> response = restTemplate.exchange(
                    urlBaseKeeper + "/" + keeperRequest.getUuid(), HttpMethod.GET, request, String[].class);
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
