package ua.com.juja.microservices.keepers.slackbot.exception;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ua.com.juja.microservices.keepers.slackbot.controller.KeepersSlackCommandController;
import ua.com.juja.microservices.keepers.slackbot.model.SlackParsedCommand;
import ua.com.juja.microservices.keepers.slackbot.model.dto.UserDTO;
import ua.com.juja.microservices.keepers.slackbot.model.request.KeeperRequest;
import ua.com.juja.microservices.keepers.slackbot.service.KeeperService;
import ua.com.juja.microservices.keepers.slackbot.service.impl.SlackNameHandlerService;
import ua.com.juja.microservices.utils.SlackUrlUtils;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Danil Kuznetsov
 * @author Nikolay Horushko
 * @author Dmitriy Lyashenko
 */
@RunWith(SpringRunner.class)
@WebMvcTest(KeepersSlackCommandController.class)
public class ExceptionsHandlerTest {

    @Inject
    private MockMvc mvc;

    @MockBean
    private KeeperService keeperService;

    @MockBean
    private SlackNameHandlerService slackNameHandlerService;

    private UserDTO userFrom;
    private UserDTO user1;

    @Before
    public void setup() {
        userFrom = new UserDTO("AAA000", "@from-user");
        user1 = new UserDTO("AAA111", "@slack1");
    }

    @Test
    public void shouldHandleKeepersAPIError() throws Exception {

        final String KEEPER_ADD_COMMAND_TEXT = "@slack_name teems";

        Map<String, UserDTO> users = new HashMap<>();
        users.put(userFrom.getSlack(), userFrom);
        users.put(user1.getSlack(), user1);

        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(userFrom.getSlack(), KEEPER_ADD_COMMAND_TEXT, users);

        ApiError apiError = new ApiError(
                400, "KPR-F1-D4",
                "Sorry, but you're not a keeper",
                "Exception - KeeperAccessException",
                "Something went wrong",
                Collections.EMPTY_LIST
        );

        when(slackNameHandlerService.createSlackParsedCommand(userFrom.getSlack(), KEEPER_ADD_COMMAND_TEXT))
                .thenReturn(slackParsedCommand);
        when(keeperService.sendKeeperAddRequest(any(KeeperRequest.class)))
                .thenThrow(new KeeperExchangeException(apiError, new RuntimeException("exception")));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper-add"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/keeper-add", KEEPER_ADD_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Sorry, but you're not a keeper"));
    }

    @Test
    public void shouldHandleUserAPIError() throws Exception {

        final String KEEPER_ADD_COMMAND_TEXT = "@slack_name teems";

        Map<String, UserDTO> users = new HashMap<>();
        users.put(userFrom.getSlack(), userFrom);

        ApiError apiError = new ApiError(
                400, "USF-F1-D1",
                "User not found",
                "User not found",
                "Something went wrong",
                Collections.EMPTY_LIST
        );

        when(slackNameHandlerService.createSlackParsedCommand(any(), any())).
                thenThrow(new UserExchangeException(apiError, new RuntimeException("exception")));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper-add"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/keeper-add", KEEPER_ADD_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("User not found"));
    }

    @Test
    public void shouldHandleWrongCommandException() throws Exception {

        final String KEEPER_ADD_COMMAND_TEXT = "@slack_name teems";

        when(slackNameHandlerService.createSlackParsedCommand(any(String.class), any(String.class))).
                thenThrow(new WrongCommandFormatException("Wrong command exception"));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper-add"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/keeper-add", KEEPER_ADD_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Wrong command exception"));
    }

    @Test
    public void shouldHandleAllOtherException() throws Exception {

        final String KEEPER_ADD_COMMAND_TEXT = "@slack_name teems";

        when(slackNameHandlerService.createSlackParsedCommand(any(String.class), any(String.class))).
                thenThrow(new RuntimeException("Runtime exception"));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper-add"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/keeper-add", KEEPER_ADD_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Runtime exception"));
    }
}