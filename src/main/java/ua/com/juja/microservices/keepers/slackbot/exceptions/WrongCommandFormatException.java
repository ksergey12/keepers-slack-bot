package ua.com.juja.microservices.keepers.slackbot.exceptions;

/**
 * @author Nikolay Horushko
 */
public class WrongCommandFormatException extends RuntimeException {
    public WrongCommandFormatException(String message) {
        super(message);
    }
}