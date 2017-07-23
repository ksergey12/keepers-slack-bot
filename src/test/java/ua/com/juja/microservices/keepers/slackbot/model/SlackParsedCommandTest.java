package ua.com.juja.microservices.keepers.slackbot.model;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ua.com.juja.microservices.keepers.slackbot.exception.WrongCommandFormatException;
import ua.com.juja.microservices.keepers.slackbot.model.dto.UserDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

/**
 * @author Konstantin Sergey
 */
public class SlackParsedCommandTest {
    private Map<String, UserDTO> users;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        users = new HashMap<>();
        users.put("@from", new UserDTO("uuid0", "@from"));
        users.put("@slack1", new UserDTO("uuid1", "@slack1"));
        users.put("@slack2", new UserDTO("uuid2", "@slack2"));
    }

    @Test
    public void getFirstUserInText() {
        //given
        String text = "text text @slack1 text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand("@from", text, users);
        //when
        UserDTO result = slackParsedCommand.getFirstUser();
        //then
        assertEquals("UserDTO(uuid=uuid1, slack=@slack1)", result.toString());
    }

    @Test
    public void getFirstUserInTextThrowExceptionIfNotUser() {
        //given
        String text = "text text text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand("@from", text, users);
        //then
        thrown.expect(WrongCommandFormatException.class);
        thrown.expectMessage(containsString("The text 'text text text' doesn't contain any slackName"));
        //when
        slackParsedCommand.getFirstUser();
    }

    @Test
    public void getAllUsers() {
        //given
        String text = "text @slack2 text@slack1 text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand("@from", text, users);
        //when
        List<UserDTO> result = slackParsedCommand.getAllUsers();
        //then
        assertEquals("[UserDTO(uuid=uuid2, slack=@slack2), UserDTO(uuid=uuid1, slack=@slack1)]",
                result.toString());
    }

    @Test
    public void getText() {
        //given
        String text = "text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand("@from", text, users);
        //when
        String result = slackParsedCommand.getText();
        //then
        assertEquals("text", result);
    }

    @Test
    public void getFromUser() {
        //given
        String text = "text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand("from", text, users);
        //when
        UserDTO result = slackParsedCommand.getFromUser();
        //then
        assertEquals("UserDTO(uuid=uuid0, slack=@from)", result.toString());
    }

    @Test
    public void getUserCount() {
        //given
        String text1 = "text @slack text";
        String text2 = "@slack text @slack text @slack";
        //when
        SlackParsedCommand slackParsedCommand1 = new SlackParsedCommand("@from", text1, users);
        SlackParsedCommand slackParsedCommand2 = new SlackParsedCommand("@from", text2, users);
        //then
        assertEquals(1, slackParsedCommand1.getUserCount());
        assertEquals(3, slackParsedCommand2.getUserCount());
    }

    @Test
    public void getTextWithoutSlackNames() {
        //given
        String text = "   @slack text  @slack text @slack ";
        //when
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand("@from", text, users);
        //then
        assertEquals("text text", slackParsedCommand.getTextWithoutSlackNames());
    }
}