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

    @Before
    public void setup() {
        userFrom = new UserDTO("AAA000", "@from-user");
    }

    @Test
    public void shouldHandleGamificationAPIError() throws Exception {

        final String DAILY_COMMAND_TEXT = "daily description text";

        Map<String, UserDTO> users = new HashMap<>();
        users.put(userFrom.getSlack(), userFrom);

        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(userFrom.getSlack(), DAILY_COMMAND_TEXT, users);

        ApiError apiError = new ApiError(
                400, "GMF-F5-D2",
                "You cannot give more than one thanks for day to one person",
                "The reason of the exception is 'Thanks' achievement",
                "Something went wrong",
                Collections.EMPTY_LIST
        );

        when(slackNameHandlerService.createSlackParsedCommand(userFrom.getSlack(), DAILY_COMMAND_TEXT))
                .thenReturn(slackParsedCommand);
        when(keeperService.sendKeeperAddRequest(any(KeeperRequest.class)))
                .thenThrow(new KeeperExchangeException(apiError, new RuntimeException("exception")));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/daily"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/daily", DAILY_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("You cannot give more than one thanks for day to one person"));
    }

    @Test
    public void shouldHandleUserAPIError() throws Exception {

        final String DAILY_COMMAND_TEXT = "daily description text";

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

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/daily"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/daily", DAILY_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("User not found"));
    }

    @Test
    public void shouldHandleWrongCommandException() throws Exception {

        final String COMMAND_TEXT = "@slack1 -2th @slack2 -3th @slack3";

        when(slackNameHandlerService.createSlackParsedCommand(any(String.class), any(String.class))).
                thenThrow(new WrongCommandFormatException("Wrong command exception"));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/codenjoy"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/daily", COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Wrong command exception"));
    }

    @Test
    public void shouldHandleAllOtherException() throws Exception {

        final String COMMAND_TEXT = "@slack1 -2th @slack2 -3th @slack3";

        when(slackNameHandlerService.createSlackParsedCommand(any(String.class), any(String.class))).
                thenThrow(new RuntimeException("Runtime exception"));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/codenjoy"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/daily", COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Runtime exception"));
    }
}