package client.clientApp.controllers.controllerLogic;

import client.ClientMain;
import client.clientApp.Client;
import client.clientApp.controllers.ChatWindowController;
import javafx.application.Platform;
import models.Message;
import models.MessageType;

import java.util.ArrayList;

public class ChannelLogic {
    public void addChannel(String channel) {
        Message message = new Message();
        message.CHANNEL = channel;
        message.TYPE = MessageType.JOIN_CHANNEL;
        Client.getInstance().sender.sendToServer(message);
    }

    public void deleteHistory() {
        Platform.runLater(() -> {
            ChatWindowController chatWindowController = (ChatWindowController) ClientMain.primaryStage.getUserData();
            Client.getInstance().getChannelMessages().clear();
            chatWindowController.getChannels().forEach(c -> Client.getInstance().getChannelMessages().put(c.getName(), new ArrayList<>()));
            chatWindowController.getChatBox().getChildren().clear();
            Client.getInstance().getUncheckedChannels().clear();
            chatWindowController.getChannel_list_view().refresh();
        });
    }
}
