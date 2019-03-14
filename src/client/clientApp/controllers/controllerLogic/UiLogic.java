package client.clientApp.controllers.controllerLogic;

import client.ClientMain;
import client.clientApp.Client;
import client.clientApp.controllers.ChatWindowController;
import client.clientApp.network.Sender;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.web.WebView;
import models.Message;
import models.MessageType;
import models.User;
import models.UserData;

import java.io.IOException;
import java.util.UUID;

public class UiLogic {
    ChatWindowController chatWindowController = (ChatWindowController) ClientMain.primaryStage.getUserData();
    UserData userdata = Client.getInstance().getUserData();
    Sender sender = Client.getInstance().sender;

    public void loadCss() {
        String css = this.getClass().getResource("/client/clientApp/css/normal.css").toExternalForm();
        Platform.runLater(() -> ClientMain.primaryStage.getScene().getStylesheets().add(css));
    }

    public void toggleDarkMode(Boolean checkbox) {
        String darkmodeCss = this.getClass().getResource("/client/clientApp/css/darkmode.css").toExternalForm();
        String normalCss = this.getClass().getResource("/client/clientApp/css/normal.css").toExternalForm();
        Platform.runLater(() ->
        {
            if (checkbox) {
                ClientMain.primaryStage.getScene().getStylesheets().remove(normalCss);
                ClientMain.primaryStage.getScene().getStylesheets().add(darkmodeCss);
            } else {
                ClientMain.primaryStage.getScene().getStylesheets().remove(darkmodeCss);
                ClientMain.primaryStage.getScene().getStylesheets().add(normalCss);
            }
        });
    }

    public void recreateOldSession() {
        oldNickName();
        joinOldChannels();
    }

    private void oldNickName() {
        Message nickChangeMessage = new Message(MessageType.NICKNAME_CHANGE);
        nickChangeMessage.TEXT_CONTENT = userdata.getUsername();
        sender.sendToServer(nickChangeMessage);
    }

    private void joinOldChannels() {
//        if (userdata.getJoinedChannels().size() > 0) {
//            userdata.getJoinedChannels().forEach(c -> {
//                Message channelJoinMessage = new Message(MessageType.JOIN_CHANNEL);
//                channelJoinMessage.CHANNEL = c;
//                sender.sendToServer(channelJoinMessage);
//            });
//        } else {
//            Message joinGeneral = new Message(MessageType.JOIN_CHANNEL);
//            joinGeneral.CHANNEL = "General";
//            sender.sendToServer(joinGeneral);
//        }

        userdata.getJoinedChannels().forEach(c -> {
            Message channelJoinMessage = new Message(MessageType.JOIN_CHANNEL);
            channelJoinMessage.CHANNEL = c;
            sender.sendToServer(channelJoinMessage);
        });
    }

    public void logOut() throws IOException {
        Client.getInstance().saveData();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/clientApp/views/Startup_Window.fxml"));
        Parent startup = loader.load();

        ClientMain.primaryStage.getIcons().add(new Image("/client/clientApp/images/logo.png"));
        ClientMain.primaryStage.setResizable(false);
        ClientMain.primaryStage.setTitle("Chatter Matter");
        ClientMain.primaryStage.setScene(new Scene(startup, 380, 270));

        if (Client.getInstance().getUserData().isDarkMode()) {
            String darkmodeCss = this.getClass().getResource("/client/clientApp/css/darkmode.css").toExternalForm();
            ClientMain.primaryStage.getScene().getStylesheets().add(darkmodeCss);
        } else {
            String normalCss = this.getClass().getResource("/client/clientApp/css/normal.css").toExternalForm();
            ClientMain.primaryStage.getScene().getStylesheets().add(normalCss);
        }

        ClientMain.primaryStage.show();
    }

    public void lurig() {
        Platform.runLater(() -> {
            WebView webview = new WebView();
            webview.getEngine().load(
                    "https://www.youtube.com/embed/PaFnO5LKTSs?autoplay=1"
//                    PaFnO5LKTSs
//                    hj1Uc-vCoJM
            );
            webview.setPrefSize(640, 390);
            ClientMain.primaryStage.setScene(new Scene(webview));
            ClientMain.primaryStage.show();
        });
    }

    public ListCell addCell(ListCell cell, ContextMenu contextMenu) {
        cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
            if (isNowEmpty) {
                cell.setContextMenu(null);
            } else {
                cell.setContextMenu(contextMenu);
            }
        });
        return cell;
    }
}
