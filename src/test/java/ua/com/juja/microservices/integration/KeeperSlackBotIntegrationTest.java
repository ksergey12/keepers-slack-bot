package ua.com.juja.microservices.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;
import ua.com.juja.microservices.keepers.slackbot.KeeperSlackBotApplication;
import ua.com.juja.microservices.keepers.slackbot.model.dto.UserDTO;
import ua.com.juja.microservices.utils.SlackUrlUtils;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Nikolay Horushko
 * @author Dmitriy Lyashenko
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {KeeperSlackBotApplication.class})
@AutoConfigureMockMvc
public class KeeperSlackBotIntegrationTest {

    @Inject
    private RestTemplate restTemplate;

    @Inject
    private MockMvc mvc;
    private MockRestServiceServer mockServer;

    @Value("${keepers.baseURL}")
    private String urlBase;
    @Value("${endpoint.addKeeper}")
    private String urlAddKeeper;

    @Value("${user.baseURL}")
    private String urlBaseUser;
    @Value("${endpoint.userSearch}")
    private String urlGetUser;

    private UserDTO userFrom = new UserDTO("f2034f11-561a-4e01-bfcf-ec615c1ba61a", "@from-user");
    private UserDTO user1 = new UserDTO("f2034f22-562b-4e02-bfcf-ec615c1ba62b", "@slack1");
    private UserDTO user2 = new UserDTO("f2034f33-563c-4e03-bfcf-ec615c1ba63c", "@slack2");

    @Before
    public void setup() {
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    public void onReceiveSlashCommandKeeperAddReturnOkRichMessage() throws Exception {
        final String KEEPER_ADD_COMMAND_TEXT = "@slack1 teems";
        final List<UserDTO> usersInCommand = Arrays.asList(new UserDTO[]{user1, userFrom});
        mockSuccessUsersService(usersInCommand);

        final String EXPECTED_REQUEST_TO_KEEPERS = "{" +
                "\"from\":\"f2034f11-561a-4e01-bfcf-ec615c1ba61a\"," +
                "\"uuid\":\"f2034f22-562b-4e02-bfcf-ec615c1ba62b\"," +
                "\"direction\":\"teems\"" +
                "}";

        final String EXPECTED_RESPONSE_FROM_KEEPERS= "[\"1000\"]";

        mockSuccessKeepersService(urlBase + urlAddKeeper, EXPECTED_REQUEST_TO_KEEPERS,
                EXPECTED_RESPONSE_FROM_KEEPERS);

        final String EXPECTED_RESPONSE_TO_SLACK = "Thanks, we added a new Keeper: @slack1 in direction: teems";

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper-add"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/keeper-add", KEEPER_ADD_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(EXPECTED_RESPONSE_TO_SLACK));
    }

    @Test
    public void returnErrorMessageIfKeeperAddCommandConsistTwoOrMoreSlackNames() throws Exception {
        final String KEEPER_ADD_COMMAND_TEXT = "@slack1 @slack2 teems";
        final List<UserDTO> usersInCommand = Arrays.asList(new UserDTO[]{user1, user2, userFrom});
        mockSuccessUsersService(usersInCommand);

        final String EXPECTED_RESPONSE_TO_SLACK = "We found 2 slack names in your command: '@slack1 @slack2 teems' " +
                " You can't make two Keepers on one direction.";

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper-add"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/keeper-add", KEEPER_ADD_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(EXPECTED_RESPONSE_TO_SLACK));
    }

    @Test
    public void returnErrorMessageIfKeeperAddCommandConsistTwoOrMoreDirections() throws Exception {
        final String KEEPER_ADD_COMMAND_TEXT = "@slack1 teems else";
        final List<UserDTO> usersInCommand = Arrays.asList(new UserDTO[]{user1, userFrom});
        mockSuccessUsersService(usersInCommand);

        final String EXPECTED_RESPONSE_TO_SLACK = "We found several directions in your command: 'teems else'  " +
                "You can make Keeper only on one direction.";

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper-add"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/keeper-add", KEEPER_ADD_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(EXPECTED_RESPONSE_TO_SLACK));
    }

    @Test
    public void returnErrorMessageIfKeeperAddCommandWithNoConsistDirections() throws Exception {
        final String KEEPER_ADD_COMMAND_TEXT = "@slack1";
        final List<UserDTO> usersInCommand = Arrays.asList(new UserDTO[]{user1, userFrom});
        mockSuccessUsersService(usersInCommand);

        final String EXPECTED_RESPONSE_TO_SLACK = "We didn't find direction in your command: '@slack1'";

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper-add"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/keeper-add", KEEPER_ADD_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(EXPECTED_RESPONSE_TO_SLACK));
    }

    @Test
    public void returnClientErrorMessageWhenUserServiceIsFail() throws Exception {
        final String KEEPER_ADD_COMMAND_TEXT = "@slack1 teems";
        final List<UserDTO> usersInCommand = Arrays.asList(new UserDTO[]{user1, userFrom});
        mockFailUsersService(usersInCommand);

        final String EXPECTED_RESPONSE_TO_SLACK = "Oops something went wrong :(";

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper-add"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/keeper-add", KEEPER_ADD_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(EXPECTED_RESPONSE_TO_SLACK));
    }

    @Test
    public void returnClientErrorMessageWhenKeepersServiceIsFail() throws Exception {
        final String KEEPER_ADD_COMMAND_TEXT = "@slack1 teems";
        final List<UserDTO> usersInCommand = Arrays.asList(new UserDTO[]{user1, userFrom});
        mockSuccessUsersService(usersInCommand);

        final String EXPECTED_REQUEST_TO_KEEPERS = "{" +
                "\"from\":\"f2034f11-561a-4e01-bfcf-ec615c1ba61a\"," +
                "\"uuid\":\"f2034f22-562b-4e02-bfcf-ec615c1ba62b\"," +
                "\"direction\":\"teems\"" +
                "}";

        mockFailKeepersService(urlBase + urlAddKeeper, EXPECTED_REQUEST_TO_KEEPERS);

        final String EXPECTED_RESPONSE_TO_SLACK = "Oops something went wrong :(";

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper-add"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/keeper-add", KEEPER_ADD_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(EXPECTED_RESPONSE_TO_SLACK));
    }

    private void mockFailUsersService(List<UserDTO> users) throws JsonProcessingException {
        List<String> slackNames = new ArrayList<>();
        for (UserDTO user : users) {
            slackNames.add(user.getSlack());
        }
        ObjectMapper mapper = new ObjectMapper();
        mockServer.expect(requestTo(urlBaseUser + urlGetUser))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(content().string(String.format("{\"slackNames\":%s}", mapper.writeValueAsString(slackNames))))
                .andRespond(withBadRequest().body("{\"httpStatus\":400,\"internalErrorCode\":1," +
                        "\"clientMessage\":\"Oops something went wrong :(\"," +
                        "\"developerMessage\":\"General exception for this service\"," +
                        "\"exceptionMessage\":\"very big and scare error\",\"detailErrors\":[]}"));

    }

    private void mockFailKeepersService(String expectedURI, String expectedRequestBody) {
        mockServer.expect(requestTo(expectedURI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(request -> assertThat(request.getHeaders().getContentType().toString(),
                        containsString("application/json")))
                .andExpect(request -> assertThat(request.getBody().toString(), equalTo(expectedRequestBody)))
                .andRespond(withBadRequest().body("{\"httpStatus\":400,\"internalErrorCode\":1," +
                        "\"clientMessage\":\"Oops something went wrong :(\"," +
                        "\"developerMessage\":\"General exception for this service\"," +
                        "\"exceptionMessage\":\"very big and scare error\",\"detailErrors\":[]}"));

    }

    private void mockSuccessUsersService(List<UserDTO> users) throws JsonProcessingException {
        List<String> slackNames = new ArrayList<>();
        for (UserDTO user : users) {
            slackNames.add(user.getSlack());
        }
        ObjectMapper mapper = new ObjectMapper();
        mockServer.expect(requestTo(urlBaseUser + urlGetUser))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(content().string(String.format("{\"slackNames\":%s}", mapper.writeValueAsString(slackNames))))
                .andRespond(withSuccess(mapper.writeValueAsString(users), MediaType.APPLICATION_JSON_UTF8));
    }

    private void mockSuccessKeepersService(String expectedURI, String expectedRequestBody, String response) {
        mockServer.expect(requestTo(expectedURI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(request -> assertThat(request.getHeaders().getContentType().toString(),
                        containsString("application/json")))
                .andExpect(request -> assertThat(request.getBody().toString(), equalTo(expectedRequestBody)))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

    }
}
