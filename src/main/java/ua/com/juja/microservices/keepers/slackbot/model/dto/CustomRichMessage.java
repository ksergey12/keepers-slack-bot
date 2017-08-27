package ua.com.juja.microservices.keepers.slackbot.model.dto;

import me.ramswaroop.jbot.core.slack.models.RichMessage;

/**
 * @author Dmitriy Lyashenko
 */
public class CustomRichMessage extends RichMessage{

    public CustomRichMessage() {
        super();
    }

    public CustomRichMessage(String text) {
        super(text);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomRichMessage custom = (CustomRichMessage) o;

        if (this.getUsername() != null ? !this.getUsername().equals(custom.getUsername())
                : custom.getUsername() != null) return false;
        if (this.getIconEmoji() != null ? !this.getIconEmoji().equals(custom.getIconEmoji())
                : custom.getIconEmoji() != null) return false;
        if (this.getChannel() != null ? !this.getChannel().equals(custom.getChannel())
                : custom.getChannel() != null) return false;
        if (this.getResponseType() != null ? !this.getResponseType().equals(custom.getResponseType())
                : custom.getResponseType() != null) return false;
        return this.getText() != null ? this.getText() .equals(custom.getText() ) : custom.getText()  == null;
    }

    @Override
    public int hashCode() {
        int result = this.getUsername() != null ? this.getUsername().hashCode() : 0;
        result = 31 * result + (this.getIconEmoji() != null ? this.getIconEmoji().hashCode() : 0);
        result = 31 * result + (this.getChannel() != null ? this.getChannel().hashCode() : 0);
        result = 31 * result + (this.getResponseType() != null ? this.getResponseType().hashCode() : 0);
        result = 31 * result + (this.getText() != null ? this.getText().hashCode() : 0);
        return result;
    }
}
