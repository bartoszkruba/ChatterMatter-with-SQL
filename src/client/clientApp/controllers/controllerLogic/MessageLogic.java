package client.clientApp.controllers.controllerLogic;

import client.ClientMain;
import client.clientApp.Client;
import client.clientApp.controllers.ChatWindowController;
import models.Message;
import models.MessageType;

import java.util.UUID;

public class MessageLogic {
    ChatWindowController chatWindowController = (ChatWindowController) ClientMain.primaryStage.getUserData();

    public void sendMessage(String input) {
        boolean msgIsOk = false;
        String textToBeSent = input;
        Message message = new Message();
        message.CHANNEL = Client.getInstance().getCurrentChannel();
        // Check if message is a whisper or channel message
        if (textToBeSent.trim().toLowerCase().startsWith("/w")) {
            message.TYPE = MessageType.WHISPER_MESSAGE;
            try {
                message.RECEIVER = UUID.fromString(textToBeSent.substring(3, 39));
                textToBeSent = textToBeSent.substring(39);
                msgIsOk = true;
            } catch (Exception e) {
            }
        } else {
            message.TYPE = MessageType.CHANNEL_MESSAGE;
            msgIsOk = true;
        }
        message.TEXT_CONTENT = textToBeSent;
        if (msgIsOk) {
            Client.getInstance().sender.sendToServer(message);
        }
    }
}
