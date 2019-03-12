package client.clientApp;

import javafx.fxml.FXML;
import models.SerializableLabel;
import models.Message;

public class MessageCreator {
    private String[] joinMessages;

    public MessageCreator() {
        joinMessages = new String[]{
                "glhf!",
                "Everyone, look busy!",
                "You must construct additional pylons.",
                "Ermagherd!",
                "Stay awhile and listen.",
                "We were expecting you 8===D",
                "We hope you brought pizza.",
                "Leave your weapons by the door.",
                "Hide your bananas.",
                "Seems OP - please nerf.",
                "Hold my beer.",
                "As the prophecy foretold."
        };
    }

    private SerializableLabel labelCreator(String text, String id) {
        SerializableLabel label = new SerializableLabel();
        label.setText(text);
        label.setId(id);
        label.setWrapText(true);
        label.save();
        return label;
    }

    @FXML
    public SerializableLabel createLabel(Message message) {
        SerializableLabel label = null;
        switch (message.TYPE) {
            case DISCONNECT:
                label = labelCreator(message.TIMESTAMP + message.NICKNAME + " has disconnected.", "disconnect_message");
                break;
            case CHANNEL_MESSAGE:
                label = labelCreator(message.TIMESTAMP + message.NICKNAME + ": " + message.TEXT_CONTENT, "channel_message");
                break;
            case JOIN_CHANNEL:
                label = labelCreator(message.TIMESTAMP + message.NICKNAME + " has joined the channel. " + joinMessages[(int)(Math.random()*joinMessages.length)], "join_message");
                break;
            case LEAVE_CHANNEL:
                label = labelCreator(message.TIMESTAMP + message.NICKNAME + " has left the channel.", "leave_channel");
                break;
            case WHISPER_MESSAGE:
                if(Client.getInstance().getThisUser().getID().equals(message.SENDER)){
                    label = labelCreator(message.TIMESTAMP + "You whispered " + message.NICKNAME + ": " + message.TEXT_CONTENT, "whisper_message");
                }else {
                    label = labelCreator(message.TIMESTAMP + message.NICKNAME + " whispers: " + message.TEXT_CONTENT, "whisper_message");
                }
                break;
            case NICKNAME_CHANGE:
                label = labelCreator(message.TIMESTAMP + message.NICKNAME + " is now " + message.TEXT_CONTENT + ".", "nickname_message");
                break;
            case ERROR:
                label = labelCreator("ERROR: " + message.TEXT_CONTENT, "error");
                break;
            case WARNING:
                label = labelCreator("Warning: " + message.TEXT_CONTENT, "warning");
                break;
        }
        return label;
    }
}
