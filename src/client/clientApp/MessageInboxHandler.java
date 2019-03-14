package client.clientApp;

import client.clientApp.controllers.ChatWindowController;
import client.ClientMain;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import models.*;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;

public class MessageInboxHandler {
   private static MessageInboxHandler ourInstance = new MessageInboxHandler();

   public static MessageInboxHandler getInstance() {
      return ourInstance;
   }

   private ChatWindowController chatWindowController;
   private MessageCreator messageCreator;
   private AudioClip mediaPlayer;
   private Media sound;

   private MessageInboxHandler() {
//      chatWindowController = (ChatWindowController) ClientMain.primaryStage.getUserData();
      messageCreator = new MessageCreator();
      String musicFile = "src" + File.separator + "client" + File.separator + "clientApp" + File.separator + "sound" + File.separator + "Message.mp3";
      sound = new Media(new File(musicFile).toURI().toString());
      mediaPlayer = new AudioClip(sound.getSource());
   }

   public void messageSwitch(Message message) {
      if (Client.getInstance().userIsIgnored(message.SENDER) &&
              (message.TYPE == MessageType.CHANNEL_MESSAGE || message.TYPE == MessageType.WHISPER_MESSAGE))
         return; // Don't proceed with ignored users
      if (message.TIMESTAMP == null) {
         message.TIMESTAMP = getTimeStamp();
      }
      Platform.runLater(() -> {
         switch (message.TYPE) {
            case WHISPER_MESSAGE:
            case CHANNEL_MESSAGE:
            case ERROR:
            case WARNING:
               printLabelOnClient(message);
               addMessageToList(message);
               playSound(message);
               break;
            case JOIN_CHANNEL:
               addMessageToList(message);
               addUserToList(message);
               printLabelOnClient(message);
               chatWindowController.printUsers();
               break;
            case LEAVE_CHANNEL:
            case DISCONNECT:
               removeUserFromList(message);
               addMessageToList(message);
               printLabelOnClient(message);
               chatWindowController.printUsers();
               break;
            case NICKNAME_CHANGE:
               changeNickname(message);
               printLabelOnClient(message);
               addMessageToList(message);
               chatWindowController.printUsers();
               break;
            case CONNECT:
               connect(message);
               break;
         }
      });
   }


   private void printLabelOnClient(Message message) {
      Label label = messageCreator.createLabel(message);
      if (message.CHANNEL != null && message.CHANNEL.equals(Client.getInstance().getCurrentChannel())) {
         chatWindowController.getChatBox().getChildren().add(label);
      }
   }

   private void addMessageToList(Message message) {
      Label label = messageCreator.createLabel(message);
      if (message.CHANNEL != null) {

         ArrayList<Label> channel = Client.getInstance().getChannelMessages().get(message.CHANNEL);
         if (channel != null) {
            channel.add(label);
         }
         if (!message.CHANNEL.equals(Client.getInstance().getCurrentChannel()) && (message.TYPE.equals(MessageType.WHISPER_MESSAGE) || (message.TYPE.equals(MessageType.CHANNEL_MESSAGE)))) {
            Client.getInstance().getUncheckedChannels().add(message.CHANNEL);
            chatWindowController.getChannel_list_view().refresh();
         }
      }

   }

   public void addChannel(Channel channel) {
      ArrayList<Label> list = Client.getInstance().getChannelMessages().getOrDefault(channel.getName(), new ArrayList<>());
      Client.getInstance().getChannelMessages().put(channel.getName(), list);
      Client.getInstance().setCurrentChannel(channel.getName());
      Platform.runLater(() -> {
         chatWindowController.channels.add(channel);
         chatWindowController.getChannel_list_view().requestFocus();
         chatWindowController.getChannel_list_view().getSelectionModel().select(channel);
         chatWindowController.getChannel_list_view().getFocusModel().getFocusedItem();
         chatWindowController.getInput_text().requestFocus();
         chatWindowController.getInput_text().clear();
         if (chatWindowController.channels.size() == 1) {
            chatWindowController.getChannel_list_view().getSelectionModel().selectFirst();
         }
      });
   }

   private void changeNickname(Message message) {
      if (message.SENDER != null && message.SENDER.equals(Client.getInstance().getThisUser().getID())) {
         Client.getInstance().getThisUser().setNickName(message.TEXT_CONTENT);
         Client.getInstance().changeTitle();
      }

      if (message.CHANNEL != null) {
         ConcurrentSkipListSet<User> channel = Client.getInstance().channelList.get(message.CHANNEL);

         if (channel != null) {
            channel.forEach(user -> {
               if (user.getID().equals(message.SENDER)) {
                  user.setNickName(message.TEXT_CONTENT);
               }
            });
         }
      }

   }

   private void connect(Message message) {
      Client.getInstance().setThisUser(new User(message.NICKNAME, message.RECEIVER));
      Client.getInstance().changeTitle();
   }

   private void addUserToList(Message message) {
      Client.getInstance().channelList.get(message.CHANNEL).add(new User(message.NICKNAME, message.SENDER));
   }

   private void removeUserFromList(Message message) {
      User user = Client.getInstance().channelList.get(message.CHANNEL)
              .stream()
              .filter(u -> u.getID().equals(message.SENDER))
              .toArray(User[]::new)[0];
      Client.getInstance().channelList.get(message.CHANNEL).remove(user);
   }


   private String getTimeStamp() {
      LocalDateTime now = LocalDateTime.now();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
      return "[" + now.format(formatter) + "] ";
   }

   private void playSound(Message message) {
      if (!chatWindowController.mute_checkbox.isSelected() && !message.CHANNEL.equals(Client.getInstance().getCurrentChannel())) {
         mediaPlayer.play();
      }
   }

   public void createNewChatWindowController() {
      chatWindowController = (ChatWindowController) ClientMain.primaryStage.getUserData();
   }
}
