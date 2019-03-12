package client.clientApp;

import client.clientApp.controllers.ChatWindowController;
import client.ClientMain;
import client.clientApp.network.Receiver;
import client.clientApp.network.Sender;
import client.clientApp.util.FileManager;
import javafx.application.Platform;
import models.*;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class Client {
   private static Client ourInstance = new Client();

   ///todo Make private
   public static Client getInstance() {
      return ourInstance;
   }

   boolean isRunning;
   Socket socket;
   public Sender sender;
   public Receiver reciever;
   public ConcurrentSkipListMap<String, ConcurrentSkipListSet<User>> channelList;
   private ConcurrentHashMap<String, ArrayList<SerializableLabel>> channelMessages;
   private String currentChannel;
   private User thisUser;
   private UserData userData;
   private String IP;
   private HashSet<UUID> ignoreList;
   private HashSet<String> uncheckedChannels;
  
   private Client() {
      channelList = new ConcurrentSkipListMap<>();
      channelMessages = new ConcurrentHashMap<>();
      thisUser = new User("");
      userData = new UserData();
      ignoreList = new HashSet<>();
      uncheckedChannels = new HashSet<>();
   }

   public void toggleIgnoreOnUser(UUID user_ID){
      if(!thisUser.equals(user_ID)){
         if(userIsIgnored(user_ID)){
            ignoreList.remove(user_ID);
         } else {
            ignoreList.add(user_ID);
         }
      }
   }

   public boolean userIsIgnored(UUID user_ID){
      return ignoreList.contains(user_ID);
   }

   public void changeTitle() {
      ClientMain.primaryStage.setTitle("Chatter Matter - Channel: " + Client.getInstance().getCurrentChannel() + " | Username: " + Client.getInstance().getThisUser().getNickName());

   }

   public void setUserData(UserData userData) {
      this.userData = userData;
   }

   public void tryReconnect(){
      if(this.isRunning){
         final int WAIT = 1000;
         Message m = new Message(MessageType.ERROR);
         for (int i = 1; i < 10; i++) {
            m.CHANNEL = Client.getInstance().currentChannel;
            m.TEXT_CONTENT = "Connection to server was lost. Trying to reconnect in " + i + " seconds...";
            MessageInboxHandler.getInstance().messageSwitch(m);
            try {
               Thread.sleep((i * WAIT));
            } catch (Exception ex) {
            }
            try {
               socket.close();
               socket = new Socket(this.IP, 54322);
               sender.setSocket(socket);
               reciever.setSocket(socket);
               rejoinChannels();
               break;
            } catch (Exception e) {
            }
            if (i == 9) {
               m.TEXT_CONTENT = "The connection to the server has been lost and couldn't be recreated!";
               MessageInboxHandler.getInstance().messageSwitch(m);
               this.kill();
            }
         }
      }
   }

   private void rejoinChannels(){
       ChatWindowController chatWindowController = (ChatWindowController) ClientMain.primaryStage.getUserData();
       Platform.runLater(() -> {
           chatWindowController.channels.clear();
       });
       Message name = new Message(MessageType.NICKNAME_CHANGE);
       name.TEXT_CONTENT = Client.getInstance().getThisUser().getNickName();
       Client.getInstance().sender.sendToServer(name);
       Client.getInstance().channelList.keySet().stream().forEach(channel -> {
           Message joinMessage = new Message(MessageType.JOIN_CHANNEL);
           joinMessage.CHANNEL = channel;
           sender.sendToServer(joinMessage);
       });
   }


   public void kill() {
      isRunning = false;
      try {
         socket.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public User getThisUser() {
      return thisUser;
   }

   public void setThisUser(User thisUser) {
      this.thisUser = thisUser;
   }

   public UserData getUserData() {
      return userData;
   }

   public ConcurrentHashMap<String, ArrayList<SerializableLabel>> getChannelMessages() {
      return channelMessages;
   }

   public void setChannelMessages(ConcurrentHashMap<String, ArrayList<SerializableLabel>> channelMessages) {
      this.channelMessages = channelMessages;
   }

   public String getCurrentChannel() {
      return currentChannel;
   }

   public void setCurrentChannel(String currentChannel) {
      this.currentChannel = currentChannel;
   }

   public boolean isRunning() {
      return this.isRunning;
   }

   public void setIsRunning(boolean isRunning) {
      this.isRunning = isRunning;
   }

   public boolean connect(String ip) {
      try {
         socket = new Socket(ip, 54322);
         isRunning = true;
         this.IP = ip;
         sender = new Sender(socket);
         reciever = new Receiver(socket);
         return true;
      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }
   }

   public void startSenderAndReceiver() {
      sender.start();
      reciever.start();
   }

   public void saveData() {
      this.kill();
      this.userData = new UserData();
      userData.setUsername(thisUser.getNickName());
      this.channelMessages.entrySet().forEach(e -> {
         userData.addChannel(e.getKey(), e.getValue());
      });

      ChatWindowController controller = (ChatWindowController) ClientMain.primaryStage.getUserData();

      controller.channels.forEach(c -> {
         userData.addJoinedChannel(c.getName());
      });

      userData.setDarkMode(controller.getDarkmode_checkbox().isSelected());
      userData.setIP(this.IP);

      // TODO: 2019-02-21 When ignorelist is ready userData.addIgnore();

      FileManager.saveFile(userData, "user-data.ser");
   }

   public HashSet<String> getUncheckedChannels() {
      return uncheckedChannels;
   }
}
