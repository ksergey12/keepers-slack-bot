package ua.com.juja.microservices.keepers.slackbot.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;
import ua.com.juja.microservices.keepers.slackbot.model.dto.CustomRichMessage;
import ua.com.juja.microservices.keepers.slackbot.service.KeeperService;
import ua.com.juja.microservices.utils.SlackUrlUtils;

import javax.inject.Inject;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Nikolay Horushko
 * @author Dmitriy Lyashenko
 * @author Konstantin Sergey
 */
@RunWith(SpringRunner.class)
@WebMvcTest(KeepersSlackCommandController.class)
public class KeepersSlackCommandControllerTest {

    private static final String SORRY_MESSAGE = "Sorry! You're not lucky enough to use our slack command.";
    private static final String IN_PROGRESS = "In progress...";
    private static final String EXAMPLE_URL = "http://example.com";
    private static final String ERROR_MESSAGE = "Something went wrong!";
    private static final String TOKEN_CORRECT = "slashCommandToken";
    private static final String TOKEN_WRONG = "wrongSlackToken";

    @Inject
    private MockMvc mvc;

    @MockBean
    private KeeperService keeperService;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    public void onReceiveSlashCommandKeeperAddIncorrectTokenShouldSendSorryRichMessage() throws Exception {
        // given
        final String KEEPER_ADD_COMMAND_TEXT = "@slack_name teams";

        // then
        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper/add"),
                SlackUrlUtils.getUriVars(TOKEN_WRONG, "/command", KEEPER_ADD_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(SORRY_MESSAGE));

        verifyNoMoreInteractions(keeperService, restTemplate);
    }

    @Test
    public void onReceiveSlashKeeperAddSendOkRichMessage() throws Exception {
        // given
        final String KEEPER_ADD_COMMAND_TEXT = "@slack1 teams";
        final String KEEPER_RESPONSE = "Thanks, we added a new Keeper: @slack1 in direction: teams";

        // when
        when(keeperService.sendKeeperAddRequest("@from-user", KEEPER_ADD_COMMAND_TEXT))
                .thenReturn(KEEPER_RESPONSE);
        when(restTemplate.postForObject(anyString(), any(CustomRichMessage.class), anyObject())).thenReturn("[OK]");

        // then
        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper/add"),
                SlackUrlUtils.getUriVars(TOKEN_CORRECT, "/keeper-add", KEEPER_ADD_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(IN_PROGRESS));

        verify(keeperService).sendKeeperAddRequest("@from-user", KEEPER_ADD_COMMAND_TEXT);
        verify(restTemplate).postForObject(EXAMPLE_URL, new CustomRichMessage(KEEPER_RESPONSE), String.class);
        verifyNoMoreInteractions(keeperService, restTemplate);
    }

    @Test
    public void onReceiveSlashKeeperAddShouldSendExceptionMessage() throws Exception {
        // given
        final String KEEPER_ADD_COMMAND_TEXT = "@slack1 teams";

        // when
        when(keeperService.sendKeeperAddRequest(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException(ERROR_MESSAGE));
        when(restTemplate.postForObject(anyString(), any(CustomRichMessage.class), anyObject())).thenReturn("[OK]");

        // then
        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper/add"),
                SlackUrlUtils.getUriVars(TOKEN_CORRECT, "/keeper-add", KEEPER_ADD_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(IN_PROGRESS));

        verify(keeperService).sendKeeperAddRequest("@from-user", KEEPER_ADD_COMMAND_TEXT);
        verify(restTemplate).postForObject(EXAMPLE_URL, new CustomRichMessage(ERROR_MESSAGE), String.class);
        verifyNoMoreInteractions(keeperService, restTemplate);
    }

    @Test
    public void onReceiveSlashCommandKeeperDismissIncorrectTokenShouldSendSorryRichMessage() throws Exception {
        // given
        final String KEEPER_DISMISS_COMMAND_TEXT = "@slack_name teams";

        // then
        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper/dismiss"),
                SlackUrlUtils.getUriVars(TOKEN_WRONG, "/command", KEEPER_DISMISS_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(SORRY_MESSAGE));

        verifyNoMoreInteractions(keeperService, restTemplate);
    }

    @Test
    public void onReceiveSlashKeeperDismissSendOkRichMessage() throws Exception {
        // given
        final String KEEPER_DISMISS_COMMAND_TEXT = "@slack1 teams";
        final String KEEPER_RESPONSE = "Keeper: @slack1 in direction: teams dismissed";

        // when
        when(keeperService.sendKeeperDismissRequest("@from-user", KEEPER_DISMISS_COMMAND_TEXT))
                .thenReturn(KEEPER_RESPONSE);
        when(restTemplate.postForObject(anyString(), any(CustomRichMessage.class), anyObject())).thenReturn("[OK]");

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper/dismiss"),
                SlackUrlUtils.getUriVars(TOKEN_CORRECT, "/keeper-dismiss", KEEPER_DISMISS_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(IN_PROGRESS));

        verify(keeperService).sendKeeperDismissRequest("@from-user", KEEPER_DISMISS_COMMAND_TEXT);
        verify(restTemplate).postForObject(EXAMPLE_URL, new CustomRichMessage(KEEPER_RESPONSE), String.class);
        verifyNoMoreInteractions(keeperService, restTemplate);
    }

    @Test
    public void onReceiveSlashKeeperDismissShouldSendExceptionMessage() throws Exception {
        // given
        final String KEEPER_DISMISS_COMMAND_TEXT = "@slack1 teams";

        // when
        when(keeperService.sendKeeperDismissRequest(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException(ERROR_MESSAGE));
        when(restTemplate.postForObject(anyString(), any(CustomRichMessage.class), anyObject())).thenReturn("[OK]");

        // then
        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper/dismiss"),
                SlackUrlUtils.getUriVars(TOKEN_CORRECT, "/keeper-dismiss", KEEPER_DISMISS_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(IN_PROGRESS));

        verify(keeperService).sendKeeperDismissRequest("@from-user", KEEPER_DISMISS_COMMAND_TEXT);
        verify(restTemplate).postForObject(EXAMPLE_URL, new CustomRichMessage(ERROR_MESSAGE), String.class);
        verifyNoMoreInteractions(keeperService, restTemplate);
    }

    @Test
    public void onReceiveSlashCommandGetKeeperDirectionsIncorrectTokenShouldSendSorryRichMessage() throws Exception {
        // given
        final String GET_DIRECTIONS_COMMAND_TEXT = "@slack_name";

        // then
        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper"),
                SlackUrlUtils.getUriVars(TOKEN_WRONG, "/command", GET_DIRECTIONS_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(SORRY_MESSAGE));

        verifyNoMoreInteractions(keeperService, restTemplate);
    }

    @Test
    public void onReceiveSlashGetKeeperDirectionsSendOkRichMessage() throws Exception {
        // given
        final String GET_DIRECTIONS_COMMAND_TEXT = "@slack1";
        final String KEEPER_RESPONSE = "The keeper @slack1 has active directions: [direction1, direction2]";

        // when
        when(keeperService.getKeeperDirections("@from-user", GET_DIRECTIONS_COMMAND_TEXT))
                .thenReturn(KEEPER_RESPONSE);
        when(restTemplate.postForObject(anyString(), any(CustomRichMessage.class), anyObject())).thenReturn("[OK]");

        // then
        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper"),
                SlackUrlUtils.getUriVars(TOKEN_CORRECT, "/keeper/AAA111", GET_DIRECTIONS_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(IN_PROGRESS));

        verify(keeperService).getKeeperDirections("@from-user", GET_DIRECTIONS_COMMAND_TEXT);
        verify(restTemplate).postForObject(EXAMPLE_URL, new CustomRichMessage(KEEPER_RESPONSE), String.class);
        verifyNoMoreInteractions(keeperService, restTemplate);
    }

    @Test
    public void onReceiveSlashGetKeeperDirectionsShouldSendExceptionMessage() throws Exception {
        // given
        final String GET_DIRECTIONS_COMMAND_TEXT = "@slack1";

        // when
        when(keeperService.getKeeperDirections(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException(ERROR_MESSAGE));
        when(restTemplate.postForObject(anyString(), any(CustomRichMessage.class), anyObject())).thenReturn("[OK]");

        // then
        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/keeper"),
                SlackUrlUtils.getUriVars(TOKEN_CORRECT, "/keeper/AAA111", GET_DIRECTIONS_COMMAND_TEXT))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(IN_PROGRESS));

        verify(keeperService).getKeeperDirections("@from-user", GET_DIRECTIONS_COMMAND_TEXT);
        verify(restTemplate).postForObject(EXAMPLE_URL, new CustomRichMessage(ERROR_MESSAGE), String.class);
        verifyNoMoreInteractions(keeperService, restTemplate);
    }
}