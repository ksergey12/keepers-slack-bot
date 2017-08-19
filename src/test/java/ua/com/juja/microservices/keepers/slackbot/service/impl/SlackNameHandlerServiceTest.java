package ua.com.juja.microservices.keepers.slackbot.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import ua.com.juja.microservices.keepers.slackbot.model.SlackParsedCommand;
import ua.com.juja.microservices.keepers.slackbot.model.dto.UserDTO;
import ua.com.juja.microservices.keepers.slackbot.service.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * @author Nikolay Horushko
 * @author Dmitriy Lyashenko
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SlackNameHandlerServiceTest {

    @Inject
    private SlackNameHandlerService slackNameHandlerService;

    @MockBean
    private UserService userService;

    private UserDTO userFrom;
    private UserDTO user1;
    private UserDTO user2;

    @Before
    public void setup() {
        userFrom = new UserDTO("AAA000", "@slackFrom");
        user1 = new UserDTO("AAA111", "@slack1");
        user2 = new UserDTO("AAA222", "@slack2");
    }

    @Test
    public void getSlackParsedCommandOneSlackInText() throws Exception {
        //given
        String text = "text " + user1.getSlack() + " TexT text.";
        List<String> requestToUserService = Arrays.asList(new String[]{user1.getSlack(), userFrom.getSlack()});
        List<UserDTO> responseFromUserService = Arrays.asList(new UserDTO[]{userFrom, user1});
        when(userService.findUsersBySlackNames(requestToUserService)).thenReturn(responseFromUserService);
        SlackParsedCommand expected = new SlackParsedCommand(userFrom, text, Collections.singletonList(user1));
        //when
        SlackParsedCommand actual = slackNameHandlerService.createSlackParsedCommand(userFrom.getSlack(), text);
        //then
        assertEquals(expected, actual);
    }

    @Test
    public void getSlackParsedCommandTwoSlackInText() throws Exception {
        //given
        String text = "text " + user1.getSlack() + " TexT " + user2.getSlack() + " text.";
        List<String> requestToUserService = Arrays.asList(new String[]{user1.getSlack(), user2.getSlack(), userFrom.getSlack()});
        List<UserDTO> responseFromUserService = Arrays.asList(new UserDTO[]{userFrom, user1, user2});
        when(userService.findUsersBySlackNames(requestToUserService)).thenReturn(responseFromUserService);
        SlackParsedCommand expected = new SlackParsedCommand(userFrom, text, Arrays.asList(user1, user2));
        //when
        SlackParsedCommand actual = slackNameHandlerService.createSlackParsedCommand(userFrom.getSlack(), text);
        //then
        assertEquals(expected, actual);
    }

    @Test
    public void getSlackParsedCommandWithoutSlackInText() throws Exception {
        //given
        String text = "text without slack name TexT text.";
        List<String> requestToUserService = Arrays.asList(new String[]{userFrom.getSlack()});
        List<UserDTO> responseFromUserService = Arrays.asList(new UserDTO[]{userFrom});
        when(userService.findUsersBySlackNames(requestToUserService)).thenReturn(responseFromUserService);
        SlackParsedCommand expected = new SlackParsedCommand(userFrom, text, new ArrayList<>());
        //when
        SlackParsedCommand actual = slackNameHandlerService.createSlackParsedCommand(userFrom.getSlack(), text);
        //then
        assertEquals(expected, actual);
    }
}