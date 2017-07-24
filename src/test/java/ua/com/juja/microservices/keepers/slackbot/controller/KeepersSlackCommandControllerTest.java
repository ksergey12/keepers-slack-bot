package ua.com.juja.microservices.keepers.slackbot.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ua.com.juja.microservices.keepers.slackbot.model.SlackParsedCommand;
import ua.com.juja.microservices.keepers.slackbot.model.dto.UserDTO;
import ua.com.juja.microservices.keepers.slackbot.model.request.KeeperRequest;
import ua.com.juja.microservices.keepers.slackbot.service.KeeperService;
import ua.com.juja.microservices.keepers.slackbot.service.impl.SlackNameHandlerService;
import ua.com.juja.microservices.utils.SlackUrlUtils;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Nikolay Horushko
 * @author Dmitriy Lyashenko
 */
@RunWith(SpringRunner.class)
@WebMvcTest(KeepersSlackCommandController.class)
public class KeepersSlackCommandControllerTest {

    private static final String SORRY_MESSAGE = "Sorry! You're not lucky enough to use our slack command.";

    @Inject
    private MockMvc mvc;

    @MockBean
    private KeeperService keeperService;

    @MockBean
    private SlackNameHandlerService slackNameHandlerService;

    private UserDTO userFrom;
    private UserDTO user1;
    private UserDTO user2;
    private UserDTO user3;

    @Before
    public void setup() {
        userFrom = new UserDTO("AAA000", "@from-user");
        user1 = new UserDTO("AAA111", "@slack1");
        user2 = new UserDTO("AAA222", "@slack2");
        user3 = new UserDTO("AAA333", "@slack3");
    }

    @Test
    public void onReceiveSlashCommandDailyWhenIncorrectTokenShouldReturnSorryRichMessage() throws Exception {
        final String KEEPER_ADD_COMMAND_TEXT = "@slack_name teems";

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper-add"),
                SlackUrlUtils.getUriVars("wrongSlackToken", "/command", KEEPER_ADD_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(SORRY_MESSAGE));
    }

    @Test
    public void onReceiveSlashCommandDailyReturnOkRichMessage() throws Exception {
        final String KEEPER_ADD_COMMAND_TEXT = "@slack_name teems";

        Map<String, UserDTO> users = new HashMap<>();
        users.put(userFrom.getSlack(), userFrom);
        users.put(user1.getSlack(), user1);

        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(userFrom.getSlack(), KEEPER_ADD_COMMAND_TEXT, users);
        final String[] KEEPER_RESPONSE = {"1000"};

        when(slackNameHandlerService.createSlackParsedCommand(userFrom.getSlack(), KEEPER_ADD_COMMAND_TEXT))
                .thenReturn(slackParsedCommand);
        when(keeperService.sendKeeperAddRequest(any(KeeperRequest.class))).thenReturn(KEEPER_RESPONSE);

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper-add"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/keeper-add", KEEPER_ADD_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text")
                .value("Thanks, we added a new Keeper 1000 in direction {teems}"));
    }

    @Test
    public void onReceiveSlashCommandDailyShouldReturnErrorMessageIfOccurException() throws Exception {
        final String KEEPER_ADD_COMMAND_TEXT = "@slack_name teems";

        Map<String, UserDTO> users = new HashMap<>();
        users.put(userFrom.getSlack(), userFrom);
        users.put(user1.getSlack(), user1);

        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(userFrom.getSlack(), KEEPER_ADD_COMMAND_TEXT, users);

        when(slackNameHandlerService.createSlackParsedCommand(userFrom.getSlack(), KEEPER_ADD_COMMAND_TEXT))
                .thenReturn(slackParsedCommand);
        when(keeperService.sendKeeperAddRequest(any(KeeperRequest.class)))
                .thenThrow(new RuntimeException("something went wrong"));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper-add"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/keeper-add", KEEPER_ADD_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("something went wrong"));
    }
}