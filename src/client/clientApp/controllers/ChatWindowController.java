package client.clientApp.controllers;

import client.clientApp.Client;
import client.clientApp.controllers.controllerLogic.ChannelLogic;
import client.clientApp.controllers.controllerLogic.MessageLogic;
import client.clientApp.controllers.controllerLogic.UiLogic;
import client.clientApp.controllers.controllerLogic.UserLogic;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import models.Channel;
import models.Message;
import models.MessageType;
import models.User;

import java.io.File;
import java.util.Comparator;

public class ChatWindowController {

    private Image notification = new Image("file:src" + File.separator + "client" + File.separator + "clientApp" + File.separator + "images" + File.separator + "notification.png");

    @FXML private TextField input_text;
    @FXML public CheckBox mute_checkbox;
    @FXML private TextField nickname_change;
    @FXML private Button send_button;
    @FXML private VBox chat_box;
    @FXML private ScrollPane scroll_pane;
    @FXML private Button add_channel_button;
    @FXML private TextField channel_textField;
    @FXML private ListView channel_list_view;
    @FXML private CheckBox darkmode_checkbox;
    @FXML private VBox image_icon;
    @FXML private ListView now_online_list;
    @FXML private Label errorLabel;
    private ContextMenu listContextMenu;
    private ContextMenu ignoreListContextMenu;

    public CheckBox getDarkmode_checkbox() {
        return darkmode_checkbox;
    }

    public VBox getChatBox() {
        return chat_box;
    }

    public ListView getChannel_list_view() {
        return channel_list_view;
    }

    public TextField getInput_text() {
        return input_text;
    }

    public ObservableList<Channel> getChannels() {
        return channels;
    }

    public ObservableList<Channel> channels;
    private UiLogic uilogic = new UiLogic();
    private MessageLogic messageLogic = new MessageLogic();
    private UserLogic userLogic = new UserLogic();
    private ChannelLogic channelLogic = new ChannelLogic();

    public void initialize() {
        uilogic.loadCss();
        addTextLimiter(input_text, 200);
        input_text.setOnAction(e -> sendMessage());
        channel_textField.setOnAction(event -> addChannel());
        send_button.setOnAction(e -> sendMessage());
        add_channel_button.setOnAction(e -> addChannel());
        nickname_change.setOnAction(e -> changeNickName());
        scroll_pane.vvalueProperty().bind(chat_box.heightProperty());
        image_icon.setOnMouseClicked(e -> uilogic.lurig());
        createChannelList();
        createContextMenuForLeavingChannel();
        toggleDarkMode();
        createContextMenuForUser();

        // needs to happen here otherwise it wont work
        // why? dont know
        Client.getInstance().startSenderAndReceiver();
        recreateOldSession();
    }

    private void addTextLimiter(final TextField tf, final int maxLength) {
        tf.textProperty().addListener((ov, oldValue, newValue) -> {
            if (tf.getText().length() > maxLength) {
                String s = tf.getText().substring(0, maxLength);
                tf.setText(s);
            } else if (tf.getText().length() == maxLength) {
                errorLabel.setText("Maximum length reached. You can't write more than " + maxLength + " characters.");
            } else {
                errorLabel.setText("");
            }
        });
    }

    @FXML
    private void sendMessage() {
        String textToBeSent = input_text.getText();
        messageLogic.sendMessage(textToBeSent);
        input_text.clear();
        // Check if message is a whisper or channel message
        if (textToBeSent.trim().toLowerCase().startsWith("/w")) {
            try {
                input_text.appendText(textToBeSent.substring(0, 39).concat(" "));
            } catch (Exception e) {
            }
        }
        input_text.requestFocus();
        input_text.forward();
    }

    @FXML
    private void createContextMenuForUser() {
        ignoreListContextMenu = new ContextMenu();
        ignoreListContextMenu.getItems().addAll(ignoreMenuItem(), whisperMenuItem());
        now_online_list.setCellFactory(new Callback<ListView<User>, ListCell>() {
            @Override
            public ListCell call(ListView<User> param) {
                ListCell<User> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(User item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            setOpacity(1);
                            if (Client.getInstance().getThisUser().getID().equals(item.getID())) {
                                setText("\uD83D\uDC68" + item.getNickName());
                                setStyle("-fx-font-weight: bold");
                            } else if (Client.getInstance().userIsIgnored(item.getID())) {
                                setText("\uD83D\uDD07" + item.getNickName());
                                setStyle("-fx-font-weight: normal");
                                setOpacity(0.5);
                            } else {
                                setText(item.getNickName());
                                setStyle("-fx-font-weight: normal");
                            }
                        }
                    }
                };
                return uilogic.addCell(cell, ignoreListContextMenu);
            }
        });
    }
    private MenuItem ignoreMenuItem() {
        MenuItem ignoreMenuItem = new MenuItem("Toggle ignore");
        ignoreMenuItem.setOnAction((e) -> {
            User user = (User) now_online_list.getSelectionModel().getSelectedItem();
            Client.getInstance().toggleIgnoreOnUser(user.getID());
            printUsers();
        });
        return ignoreMenuItem;
    }

    private MenuItem whisperMenuItem() {
        MenuItem whisperMenuItem = new MenuItem("Whisper");
        whisperMenuItem.setOnAction((e) -> {
            User user = (User) now_online_list.getSelectionModel().getSelectedItem();
            input_text.clear();
            input_text.setText("/w " + user.getID() + " ");
            input_text.requestFocus();
            input_text.forward();
        });
        return whisperMenuItem;
    }

    private void toggleDarkMode() {
        darkmode_checkbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            darkmode_checkbox.setSelected(newValue);
            uilogic.toggleDarkMode(darkmode_checkbox.isSelected());
        });
    }

    @FXML
    public void printUsers() {
        SortedList<User> sortedList = userLogic.printUsers();
        now_online_list.setItems(sortedList);
        now_online_list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    @FXML
    private void addChannel() {
        channelLogic.addChannel(channel_textField.getText());
        channel_textField.clear();
    }

    @FXML
    private void createChannelList() {
        channels = FXCollections.observableArrayList();
        SortedList<Channel> sortedList = new SortedList<>(channels, Comparator.comparing(Channel::getName));
        channel_list_view.setItems(sortedList);
        channel_list_view.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        channel_list_view.getSelectionModel().selectedItemProperty().addListener((ChangeListener<Channel>) (observable, oldValue, newValue) -> {
            chat_box.getChildren().clear();
            if (newValue != null) {
                Client.getInstance().setCurrentChannel(newValue.getName());
                Client.getInstance().getChannelMessages().get(newValue.getName()).forEach(l -> {
                    chat_box.getChildren().add(l);
                    Client.getInstance().getUncheckedChannels().remove(newValue.getName());
                    channel_list_view.refresh();
                    input_text.clear();
                    Platform.runLater(input_text::requestFocus);
                });
            }
            Client.getInstance().changeTitle();
            printUsers();
        });
    }

    @FXML
    private void createContextMenuForLeavingChannel() {
        listContextMenu = new ContextMenu();
        listContextMenu.getItems().addAll(deleteMenuItem());
        channel_list_view.setCellFactory(new Callback<ListView<Channel>, ListCell>() {
            @Override
            public ListCell call(ListView<Channel> param) {
                ListCell<Channel> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(Channel item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            if (Client.getInstance().getUncheckedChannels().contains(item.getName())) {
                                ImageView imageView = new ImageView();
                                imageView.setImage(notification);
                                setText(item.getName());
                                setGraphic(null);
                                setGraphic(imageView);
                            } else {
                                setText(item.getName());
                                setGraphic(null);
                            }
                        }
                    }
                };
                return uilogic.addCell(cell, listContextMenu);
            }
        });
    }
    private MenuItem deleteMenuItem(){
        MenuItem deleteMenuItem = new MenuItem("Leave Channel");
        deleteMenuItem.setOnAction((e) -> {
            Channel channel = (Channel) channel_list_view.getSelectionModel().getSelectedItem();
            Message message = new Message(MessageType.LEAVE_CHANNEL);
            Client.getInstance().sender.sendToServer(message);
            message.CHANNEL = ((Channel) channel_list_view.getSelectionModel().getSelectedItem()).getName();
            channels.remove(channel);
            if (channels.size() == 0) {
                chat_box.getChildren().clear();
                Client.getInstance().setCurrentChannel(null);
            }
        });
        return deleteMenuItem;
    }

    private void recreateOldSession() {
        this.darkmode_checkbox.setSelected(Client.getInstance().getUserData().isDarkMode());
        uilogic.recreateOldSession();
    }

    @FXML
    private void changeNickName() {
        userLogic.changeNickname(nickname_change.getText());
        nickname_change.clear();
    }

    @FXML
    private void deleteHistory() {
        channelLogic.deleteHistory();
    }

    @FXML
    private void logOut() throws Exception {
        uilogic.logOut();
    }

    public void lurig() {
        if (input_text.getText().equals("man") && Client.getInstance().getThisUser().getNickName().equals("spider")) {
            uilogic.lurig();
        }
    }
}