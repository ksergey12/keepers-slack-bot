package ua.com.juja.microservices.keepers.slackbot.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ua.com.juja.microservices.keepers.slackbot.service.KeeperService;
import ua.com.juja.microservices.utils.SlackUrlUtils;

import javax.inject.Inject;

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

    @Test
    public void onReceiveSlashCommandKeeperAddIncorrectTokenShouldReturnSorryRichMessage() throws Exception {
        final String KEEPER_ADD_COMMAND_TEXT = "@slack_name teams";

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper/add"),
                SlackUrlUtils.getUriVars("wrongSlackToken", "/command", KEEPER_ADD_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(SORRY_MESSAGE));
    }

    @Test
    public void onReceiveSlashKeeperAddReturnOkRichMessage() throws Exception {
        final String KEEPER_ADD_COMMAND_TEXT = "@slack1 teams";
        final String KEEPER_RESPONSE = "Thanks, we added a new Keeper: @slack1 in direction: teams";

        when(keeperService.sendKeeperAddRequest("@from-user", KEEPER_ADD_COMMAND_TEXT))
                .thenReturn(KEEPER_RESPONSE);

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper/add"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/keeper-add", KEEPER_ADD_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text")
                .value("Thanks, we added a new Keeper: @slack1 in direction: teams"));
    }

    @Test
    public void onReceiveSlashKeeperAddShouldReturnErrorMessageIfOccurException() throws Exception {
        final String KEEPER_ADD_COMMAND_TEXT = "@slack1 teams";

        when(keeperService.sendKeeperAddRequest(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("something went wrong"));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper/add"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/keeper-add", KEEPER_ADD_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("something went wrong"));
    }

    @Test
    public void onReceiveSlashCommandKeeperDismissIncorrectTokenShouldReturnSorryRichMessage() throws Exception {
        final String KEEPER_DISMISS_COMMAND_TEXT = "@slack_name teams";

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper/dismiss"),
                SlackUrlUtils.getUriVars("wrongSlackToken", "/command", KEEPER_DISMISS_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(SORRY_MESSAGE));
    }

    @Test
    public void onReceiveSlashKeeperDismissReturnOkRichMessage() throws Exception {
        final String KEEPER_DISMISS_COMMAND_TEXT = "@slack1 teams";
        final String KEEPER_RESPONSE = "Keeper: @slack1 in direction: teams dismissed";

        when(keeperService.sendKeeperDismissRequest("@from-user", KEEPER_DISMISS_COMMAND_TEXT))
                .thenReturn(KEEPER_RESPONSE);

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper/dismiss"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/keeper-dismiss", KEEPER_DISMISS_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text")
                        .value("Keeper: @slack1 in direction: teams dismissed"));
    }

    @Test
    public void onReceiveSlashKeeperDismissShouldReturnErrorMessageIfOccurException() throws Exception {
        final String KEEPER_DISMISS_COMMAND_TEXT = "@slack1 teams";

        when(keeperService.sendKeeperDismissRequest(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("something went wrong"));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper/dismiss"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/keeper-dismiss", KEEPER_DISMISS_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("something went wrong"));
    }

    @Test
    public void getKeeperDirectionsReturnEmptyRichMessage() throws Exception {
        // given
        final String GET_DIRECTIONS_COMMAND_TEXT = "@slack1";

        // when
        when(keeperService.getKeeperDirections("@from-user", GET_DIRECTIONS_COMMAND_TEXT))
                .thenReturn("The keeper @slack1 has no active directions.");

        // then
        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/keeper/AAA111", GET_DIRECTIONS_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text")
                        .value("The keeper @slack1 has no active directions."));
    }

    @Test
    public void getKeeperDirectionsReturnOkRichMessage() throws Exception {
        // given
        final String GET_DIRECTIONS_COMMAND_TEXT = "@slack1";

        // when
        when(keeperService.getKeeperDirections("@from-user", GET_DIRECTIONS_COMMAND_TEXT))
                .thenReturn("The keeper @slack1 has active directions: [direction1, direction2]");

        // then
        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/keeper/AAA111", GET_DIRECTIONS_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text")
                        .value("The keeper @slack1 has active directions: [direction1, direction2]"));
    }

    @Test
    public void getKeeperDirectionsReturnErrorRichMessage() throws Exception {
        // given
        final String GET_DIRECTIONS_COMMAND_TEXT = "@slack1";

        // when
        when(keeperService.getKeeperDirections(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("ERROR. Something went wrong and we didn't get keeper directions"));
        // then
        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/keeper/AAA111", GET_DIRECTIONS_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text")
                        .value("ERROR. Something went wrong and we didn't get keeper directions"));
    }
}