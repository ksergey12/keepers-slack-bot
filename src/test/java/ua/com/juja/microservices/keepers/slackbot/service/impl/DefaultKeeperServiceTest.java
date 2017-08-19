package ua.com.juja.microservices.keepers.slackbot.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import ua.com.juja.microservices.keepers.slackbot.dao.KeeperRepository;
import ua.com.juja.microservices.keepers.slackbot.model.SlackParsedCommand;
import ua.com.juja.microservices.keepers.slackbot.model.dto.UserDTO;
import ua.com.juja.microservices.keepers.slackbot.model.request.KeeperRequest;
import ua.com.juja.microservices.keepers.slackbot.service.KeeperService;

import javax.inject.Inject;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Nikolay Horushko
 * @author Dmitriy Lyashenko
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DefaultKeeperServiceTest {

    @MockBean
    private KeeperRepository keeperRepository;

    @MockBean
    private SlackNameHandlerService slackNameHandlerService;

    @Inject
    private KeeperService keeperService;

    private List<UserDTO> usersInText;
    UserDTO fromUser;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        fromUser = new UserDTO("uuid0", "@from");
        usersInText = new ArrayList<>();
        usersInText.add(new UserDTO("uuid1", "@slack_name"));
    }

    @Test
    public void shouldSaveKeeperAndReturnValidText() {
        //given
        String[] expectedKeeperId = {"100"};
        final String KEEPER_ADD_COMMAND_TEXT = "@slack_name teams";
        KeeperRequest keeperRequest = new KeeperRequest("uuid0", "uuid1", "teams");
        when(keeperRepository.addKeeper(keeperRequest)).thenReturn(expectedKeeperId);
        when(slackNameHandlerService.createSlackParsedCommand("@from", KEEPER_ADD_COMMAND_TEXT))
                .thenReturn(new SlackParsedCommand(fromUser, KEEPER_ADD_COMMAND_TEXT, usersInText));

        //when
        String result = keeperService.sendKeeperAddRequest("@from", KEEPER_ADD_COMMAND_TEXT);

        //then
        assertEquals("Thanks, we added a new Keeper: @slack_name in direction: teams", result);
        verify(keeperRepository).addKeeper(keeperRequest);
        verify(slackNameHandlerService).createSlackParsedCommand("@from", KEEPER_ADD_COMMAND_TEXT);
    }

    @Test
    public void shouldDismissKeeperAndReturnValidText() {
        //given
        String[] expectedKeeperId = {"100"};
        final String KEEPER_DISMISS_COMMAND_TEXT = "@slack_name teams";
        KeeperRequest keeperRequest = new KeeperRequest("uuid0", "uuid1", "teams");
        when(keeperRepository.dismissKeeper(keeperRequest)).thenReturn(expectedKeeperId);
        when(slackNameHandlerService.createSlackParsedCommand("@from", KEEPER_DISMISS_COMMAND_TEXT))
                .thenReturn(new SlackParsedCommand(fromUser, KEEPER_DISMISS_COMMAND_TEXT, usersInText));

        //when
        String result = keeperService.sendKeeperDismissRequest("@from", KEEPER_DISMISS_COMMAND_TEXT);

        //then
        assertEquals("Keeper: @slack_name in direction: teams dismissed", result);
        verify(keeperRepository).dismissKeeper(keeperRequest);
        verify(slackNameHandlerService).createSlackParsedCommand("@from", KEEPER_DISMISS_COMMAND_TEXT);
    }

    @Test
    public void getKeeperDirections() {
        //Given
        String[] expected = {"direction1"};
        final String GET_KEEPER_DIRECTIONS_COMMAND_TEXT = "@slack_name";
        KeeperRequest keeperRequest = new KeeperRequest("uuid0", "uuid1", "");
        when(keeperRepository.getKeeperDirections(keeperRequest)).thenReturn(expected);
        when(slackNameHandlerService.createSlackParsedCommand("@from", GET_KEEPER_DIRECTIONS_COMMAND_TEXT))
                .thenReturn(new SlackParsedCommand(fromUser, GET_KEEPER_DIRECTIONS_COMMAND_TEXT, usersInText));

        //When
        String result = keeperService.getKeeperDirections("@from", GET_KEEPER_DIRECTIONS_COMMAND_TEXT);

        //Then
        assertEquals("The keeper @slack_name has active directions: [direction1]", result);
        verify(keeperRepository).getKeeperDirections(keeperRequest);
        verify(slackNameHandlerService).createSlackParsedCommand("@from", GET_KEEPER_DIRECTIONS_COMMAND_TEXT);
    }

    @Test
    public void getKeeperDirectionsWithEmptyResult() {
        //Given
        String[] emptyArray = {};
        final String GET_KEEPER_DIRECTIONS_COMMAND_TEXT = "@slack_name";
        KeeperRequest keeperRequest = new KeeperRequest("uuid0", "uuid1", "");
        when(keeperRepository.getKeeperDirections(keeperRequest)).thenReturn(emptyArray);
        when(slackNameHandlerService.createSlackParsedCommand("@from", GET_KEEPER_DIRECTIONS_COMMAND_TEXT))
                .thenReturn(new SlackParsedCommand(fromUser, GET_KEEPER_DIRECTIONS_COMMAND_TEXT, usersInText));

        //When
        String result = keeperService.getKeeperDirections("@from", GET_KEEPER_DIRECTIONS_COMMAND_TEXT);

        //Then
        assertEquals("The keeper @slack_name has no active directions.", result);
        verify(keeperRepository).getKeeperDirections(keeperRequest);
        verify(slackNameHandlerService).createSlackParsedCommand("@from", GET_KEEPER_DIRECTIONS_COMMAND_TEXT);
    }
}