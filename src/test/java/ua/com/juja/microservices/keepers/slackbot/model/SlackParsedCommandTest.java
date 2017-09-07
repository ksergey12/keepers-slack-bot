package ua.com.juja.microservices.keepers.slackbot.model;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ua.com.juja.microservices.keepers.slackbot.exception.WrongCommandFormatException;
import ua.com.juja.microservices.keepers.slackbot.model.dto.UserDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

/**
 * @author Konstantin Sergey
 */
public class SlackParsedCommandTest {
    private List<UserDTO> usersInText;
    private UserDTO fromUser;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        fromUser = new UserDTO("uuid0", "@from");
        usersInText = new ArrayList<>();
    }

    @Test
    public void getFirstUserInText() {
        //given
        usersInText.add(new UserDTO("uuid1", "@slack1"));
        String text = "text text @slack1 text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //when
        UserDTO result = slackParsedCommand.getFirstUserFromText();
        //then
        assertEquals("UserDTO(uuid=uuid1, slack=@slack1)", result.toString());
    }

    @Test
    public void getFirstUserInTextThrowExceptionIfNotUser() {
        //given
        String text = "text text text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        thrown.expect(WrongCommandFormatException.class);
        thrown.expectMessage(containsString("The text 'text text text' doesn't contain any slack names"));
        //when
        slackParsedCommand.getFirstUserFromText();
    }

    @Test
    public void getAllUsers() {
        //given
        usersInText.add(new UserDTO("uuid1", "@slack1"));
        usersInText.add(new UserDTO("uuid2", "@slack2"));
        usersInText.add(new UserDTO("uuid3", "@slack3"));
        String text = "text @slack3 text@slack2 text @slack1";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //when
        List<UserDTO> result = slackParsedCommand.getAllUsersFromText();
        //then
        assertEquals("[UserDTO(uuid=uuid1, slack=@slack1), UserDTO(uuid=uuid2, slack=@slack2)," +
                        " UserDTO(uuid=uuid3, slack=@slack3)]",
                result.toString());
    }

    @Test
    public void getText() {
        //given
        String text = "text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //when
        String result = slackParsedCommand.getText();
        //then
        assertEquals("text", result);
    }

    @Test
    public void getFromUser() {
        //given
        String text = "text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //when
        UserDTO result = slackParsedCommand.getFromUser();
        //then
        assertEquals("UserDTO(uuid=uuid0, slack=@from)", result.toString());
    }

    @Test
    public void getUserCount() {
        //given
        usersInText.add(new UserDTO("uuid1", "@slack1"));
        String text = "text @slack1 text";
        //when
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        assertEquals(1, slackParsedCommand.getUserCountInText());
    }

    @Test
    public void getTextWithoutSlackNames() {
        //given
        String text = "   @slack text  @slack text @slack ";
        //when
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        assertEquals("text text", slackParsedCommand.getTextWithoutSlackNames());
    }

    @Test
    public void getUsersByTokensWithTwoTokens() {
        //given
        String[] tokens = new String[]{"-t1", "-t2"};
        usersInText.add(new UserDTO("uuid1", "@slack1"));
        usersInText.add(new UserDTO("uuid2", "@slack2"));

        ArrayList<String> text = new ArrayList<>(Arrays.asList(
                "-t1 @slack1 -t2 @slack2",
                "-t2 @slack2 -t1 @slack1",
                "-t1@slack1 -t2@slack2",
                "text -t2 @slack2 text -t1 @slack1 text",
                "text -t2 @slack2 text -t1 @slack1 text",
                "text -t2 @slack2 -t1text @slack1 text"
        ));

        final int[] index = {0};
        text.forEach(item -> {
            //when
            SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text.get(index[0]++), usersInText);
            Map<String, UserDTO> result = slackParsedCommand.getUsersWithTokens(tokens);
            //then
            assertEquals("{-t1=UserDTO(uuid=uuid1, slack=@slack1), -t2=UserDTO(uuid=uuid2, slack=@slack2)}",
                    result.toString());
        });
    }

    @Test
    public void getUsersByTokensWithThreeTokens() {
        //given
        String[] tokens = new String[]{"-t1", "-t2", "-t3"};
        usersInText.add(new UserDTO("uuid1", "@slack1"));
        usersInText.add(new UserDTO("uuid2", "@slack2"));
        usersInText.add(new UserDTO("uuid3", "@slack3"));

        ArrayList<String> text = new ArrayList<>(Arrays.asList(
                "-t1 @slack1 -t2 @slack2 -t3 @slack3",
                "-t1@slack1 -t2 @slack2 -t3 @slack3",
                "-t1@slack1 -t2@slack2 -t3@slack3",
                "-t1 @slack1 -t3 @slack3 -t2 @slack2",
                "-t1@slack1 -t3@slack3 -t2@slack2",
                "-t1@slack1 text -t3@slack3 text -t2@slack2 text",
                "-t1 text @slack1 text -t3 text @slack3 text -t2 text @slack2 text",
                "-t1 text@slack1 text -t3 text@slack3 text -t2 text@slack2 text",
                "text -t2 @slack2 -t1 text @slack1 text -t3 @slack3"
        ));

        final int[] index = {0};
        text.forEach(item -> {
            //when
            SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text.get(index[0]++), usersInText);
            Map<String, UserDTO> result = slackParsedCommand.getUsersWithTokens(tokens);
            //then
            assertEquals("{-t1=UserDTO(uuid=uuid1, slack=@slack1), -t2=UserDTO(uuid=uuid2, slack=@slack2)," +
                    " -t3=UserDTO(uuid=uuid3, slack=@slack3)}", result.toString());
        });
    }

    @Test
    public void getUsersByTokensThrowExceptionIfTokenNotFound() {
        //given
        usersInText.add(new UserDTO("uuid1", "@slack1"));
        usersInText.add(new UserDTO("uuid2", "@slack2"));

        String[] tokens = new String[]{"-t1", "-t2"};
        String text = "text-t2@slack2 text@slack1 text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        thrown.expect(WrongCommandFormatException.class);
        thrown.expectMessage(containsString("Token '-t1' didn't find in the string 'text-t2@slack2 text@slack1 text'"));
        //when
        slackParsedCommand.getUsersWithTokens(tokens);
    }

    @Test
    public void getUsersByTokensError1() {
        //given
        usersInText.add(new UserDTO("uuid1", "@slack1"));
        usersInText.add(new UserDTO("uuid2", "@slack2"));

        String[] tokens = new String[]{"-t1", "-t2"};
        String text = "text-t2 @slack2 text -t1 text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        thrown.expect(WrongCommandFormatException.class);
        thrown.expectMessage(containsString("The text 'text-t2 @slack2 text -t1 text' doesn't " +
                "contain slackName for token '-t1'"));
        //when
        slackParsedCommand.getUsersWithTokens(tokens);
    }

    @Test
    public void getUsersByTokensError2() {
        //given
        usersInText.add(new UserDTO("uuid1", "@slack1"));
        usersInText.add(new UserDTO("uuid2", "@slack2"));

        String[] tokens = new String[]{"-t1", "-t2"};
        String text = "text-t2 -t1@slack2 text text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        thrown.expect(WrongCommandFormatException.class);
        thrown.expectMessage(containsString("The text 'text-t2 -t1@slack2 text text' doesn't contain " +
                "slackName for token '-t2'"));
        //when
        slackParsedCommand.getUsersWithTokens(tokens);
    }

    @Test
    public void getUsersByTokensError3() {
        //given
        usersInText.add(new UserDTO("uuid1", "@slack1"));
        usersInText.add(new UserDTO("uuid2", "@slack2"));
        usersInText.add(new UserDTO("uuid3", "@slack3"));

        String[] tokens = new String[]{"-t1", "-t2", "-t3"};
        String text = "-t1 @slack1 -t2 -t3 @slack2 -t3 @slack3";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        thrown.expect(WrongCommandFormatException.class);
        thrown.expectMessage(containsString("The text '-t1 @slack1 -t2 -t3 @slack2 -t3 @slack3'" +
                " contains 2 tokens '-t3', but expected 1"));
        //when
        slackParsedCommand.getUsersWithTokens(tokens);
    }

    @Test
    public void getUsersByTokensErrorTextContainsMoreThanOneToken() {
        //given
        usersInText.add(new UserDTO("uuid1", "@slack1"));
        usersInText.add(new UserDTO("uuid2", "@slack2"));

        String[] tokens = new String[]{"-t1", "-t2"};
        String text = "text-t2 -t1@slack2 text -t1 text";
        SlackParsedCommand slackParsedCommand = new SlackParsedCommand(fromUser, text, usersInText);
        //then
        thrown.expect(WrongCommandFormatException.class);
        thrown.expectMessage(containsString("The text 'text-t2 -t1@slack2 text -t1 text' contains 2 tokens '-t1'," +
                " but expected 1"));
        //when
        slackParsedCommand.getUsersWithTokens(tokens);
    }
}