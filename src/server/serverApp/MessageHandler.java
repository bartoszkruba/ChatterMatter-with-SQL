package server.serverApp;


import models.*;
import server.serverApp.controllers.DatasourceController;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageHandler implements Runnable {

   private LinkedBlockingQueue<Sendable> messages;
   private String[] badWordList;
   private String[] betterWordList;
   private final AdminSystemMonitoring adminSystemMonitoring;
   private boolean isRunning;

   public MessageHandler(LinkedBlockingQueue<Sendable> messages, AdminSystemMonitoring adminSystemMonitoring) {
      this.messages = messages;
      this.adminSystemMonitoring = adminSystemMonitoring;
      this.isRunning = true;
      badWordList = new String[]
              {"fuck", "pussy", "cunt", "whore", "nigger", "ass",
                      "bitch", "cock", "poop", "shit", "fag", "dick", "slut",
                      "linux", "iphone", "mac", "torvalds", "crap", "bastard",
                      "crap", "cum"};
      betterWordList = new String[]
              {"flower", "potato", "tomato", "love", "kitten"};
   }

   @Override
   public void run() {
      while (isRunning) {
         if (messages.size() > 0) {
            processMessages();
         }
         try {
            Thread.sleep(1);
         } catch (InterruptedException e) {
         }

      }
   }

   private void processMessages() {
      Message m = (Message) this.messages.remove();
      fixMessageContent(m);
      switch (m.TYPE) {
         case DISCONNECT:
            sendToChannel(m);
            break;
         case CHANNEL_MESSAGE:
            sendToChannelFromUser(m);
            break;
         case JOIN_CHANNEL:
            addUserToChannel(m);
            break;
         case LEAVE_CHANNEL:
            removeUserFromChannel(m);
            break;
         case WHISPER_MESSAGE:
            sendWhisperToUser(m);
            break;
         case NICKNAME_CHANGE:
            changeUserNickName(m);
            break;
      }
   }

   public void kill() {
      this.isRunning = false;
   }

   private void fixMessageContent(Message m) {
      // Only apply is there is text-content and the message is sent to a channel or user
      if (m.TEXT_CONTENT != null && m.TYPE == MessageType.CHANNEL_MESSAGE || m.TYPE == MessageType.WHISPER_MESSAGE) {
         // Remove multiple whitespaces, replace bad words, fix the length to 200 chars
         m.TEXT_CONTENT = m.TEXT_CONTENT
                 .replaceAll("^ +([^ ]+)|( ?) *([^ ]+)( ) *| *$", "$1$2$3$4")
                 .replaceAll("(?i)((" + String.join("|", badWordList) + ")\\w*\\b)",
                         betterWordList[(int) (Math.random() * betterWordList.length)])
                 .replaceFirst("^(.{0,200}).*$", "$1");
      }
   }


   private boolean checkIfChannelIsValid(String channel) {
      return channel.matches("^[\\w]{3,10}$") &&
              !channel.matches("(?i).*(" + String.join("|", badWordList) + ").*");
   }

   private void sendToChannelFromUser(Message m) {
      if (m.TEXT_CONTENT != null && !m.TEXT_CONTENT.equals("") && !spamProtectorDetectsDanger(m)) {
         sendToChannel(m);
         DatasourceController.getInstance().addMessageToSave(m);
      }
   }

   private void sendToChannel(Message m) {
      if (m.CHANNEL == null || m.CHANNEL.equals("")) {
         return;
      }
      Channel channel = ActiveChannelController.getInstance().getChannel(m.CHANNEL);
      if (channel != null) {
         SortedSet<User> users = channel.getUsers();
         if (users != null) {
            users.forEach(u -> {
               try {
                  ActiveUserController.getInstance().getUserOutbox(u).add(m);
               } catch (Exception e) {
               }
            });
         }
      }
   }

   private void changeUserNickName(Message m) {
      // Trim the string
      m.TEXT_CONTENT = m.TEXT_CONTENT.trim();
      if (!m.TEXT_CONTENT.equals(m.NICKNAME)) {
         if (validUserNickName(m.TEXT_CONTENT)) {
            // If the username is valid, change it on the server
            User user = ActiveUserController.getInstance().getUser(m.SENDER);
            String oldUsername = user.getNickName();
            user.setNickName(m.TEXT_CONTENT);

            // Send new username to all channels the user is active in

            adminSystemMonitoring.addToLog("User " + m.SENDER + "(" + oldUsername + ")" + " changed name to " + m.TEXT_CONTENT);

            sendOutNewUserNickName(user, m);
            DatasourceController.getInstance().addMessageToSave(m);
         } else {
            // If username was invalid, send an error to the user
            sendErrorToUser(MessageType.ERROR, m.SENDER, m.CHANNEL, "The username you wanted is not valid. " +
                    "\nNames should be 3-10 characters long containing only letters and/or numbers. " +
                    "\nNo offensive words are allowed as names. ");
         }
      }
   }

   private void sendErrorToUser(MessageType messageType, UUID user_ID, String channel, String errorText) {
      if (user_ID != null && channel != null) {
         Message errorMessage = new Message(messageType);
         errorMessage.TEXT_CONTENT = errorText;
         errorMessage.RECEIVER = user_ID;
         errorMessage.CHANNEL = channel;
         sendToUser(errorMessage);
      }
   }

   private void sendOutNewUserNickName(User user, Message m) {
      String[] userChannels = ActiveChannelController.getInstance().getChannelsForUser(user);

      if (userChannels.length > 0) {
         Arrays.stream(userChannels).forEach(channel -> {
            Message messageToBeSent = new Message(MessageType.NICKNAME_CHANGE);
            messageToBeSent.TEXT_CONTENT = m.TEXT_CONTENT;
            messageToBeSent.SENDER = m.SENDER;
            messageToBeSent.NICKNAME = m.NICKNAME;
            messageToBeSent.CHANNEL = channel;
            sendToChannel(messageToBeSent);
         });
      } else {
         Message messageToBeSent = new Message(MessageType.NICKNAME_CHANGE);
         messageToBeSent.TEXT_CONTENT = m.TEXT_CONTENT;
         messageToBeSent.SENDER = m.SENDER;
         messageToBeSent.NICKNAME = m.NICKNAME;
         messageToBeSent.RECEIVER = m.SENDER;
         sendToUser(messageToBeSent);
      }
   }


   private boolean validUserNickName(String newName) {
      return newName.matches("^[\\w]{3,10}$") &&
              !newName.matches("(?i).*(" + String.join("|", badWordList) + ").*");
   }

   private void addUserToChannel(Message m) {
      String channel = m.CHANNEL;
      User user = ActiveUserController.getInstance().getUser(m.SENDER);

      boolean userIsInChannel = ActiveChannelController.getInstance().userIsInChannel(channel, user);

      if (channel != null && user != null && !userIsInChannel && checkIfChannelIsValid(m.CHANNEL)) {

         m.NICKNAME = user.getNickName();
         ActiveChannelController.getInstance().addUserToChannel(user, channel);
         Channel c = ActiveChannelController.getInstance().getChannel(m.CHANNEL);

         if (ActiveUserController.getInstance().getUserOutbox(user).add(c)) {
            adminSystemMonitoring.addToLog("Adding User " + m.SENDER + "(" + user.getNickName() + ")" + " to channel " + m.CHANNEL);
            sendChannelHistory(user, m.CHANNEL);
         }

         sendToChannel(m);
      }
   }

   private void sendChannelHistory(User user, String channel) {
      new Thread(() -> {
         List<Message> messages = DatasourceController.getInstance().queryAllMessagesFromChannel(channel);
         messages.forEach(m -> {
            m.setRECEIVER(user.getID());
            sendToUser(m);
         });
      }).start();
   }

   private void removeUserFromChannel(Message m) {
      User user = ActiveUserController.getInstance().getUser(m.SENDER);
      if (m.SENDER != null && m.CHANNEL != null && !m.CHANNEL.equals("") && user != null) {
         this.sendToChannel(m);
         adminSystemMonitoring.addToLog(ActiveChannelController.getInstance().removeUserFromChannel(m.SENDER, m.CHANNEL) ? "User " + m.SENDER + "(" + user.getNickName() + ")" +
                 " removed from channel " + m.CHANNEL : "");
      }
   }

   private void sendWhisperToUser(Message m) {
      User user = ActiveUserController.getInstance().getUser(m.RECEIVER);
      if (user.equals(m.SENDER)) {
         sendErrorToUser(MessageType.ERROR, m.SENDER, m.CHANNEL, "Only loosers chat with themselves");
      } else if (user != null && ActiveChannelController.getInstance().userIsInChannel(m.CHANNEL, user)) {
         if (m.TEXT_CONTENT != null && !m.TEXT_CONTENT.equals("") && !spamProtectorDetectsDanger(m)) {
            sendWhisperToSender(m);
            sendToUser(m);
         }
      } else {
         sendErrorToUser(MessageType.ERROR, m.SENDER, m.CHANNEL, "The user does not exist in this channel anymore");
      }
   }

   private void sendToUser(Message m) {
      try {
         ActiveUserController.getInstance().getUserOutbox(m.RECEIVER).add(m);
      } catch (Exception e) {
         adminSystemMonitoring.addToLog("A message was sent to a user that doesn't exist on the server");
      }
   }

   private void sendWhisperToSender(Message m) {
      User receiver = ActiveUserController.getInstance().getUser(m.RECEIVER);
      Message replay = new Message(m.TYPE);
      replay.RECEIVER = m.SENDER;
      replay.SENDER = m.SENDER;
      replay.TEXT_CONTENT = m.TEXT_CONTENT;
      replay.CHANNEL = m.CHANNEL;
      replay.CHANNEL = m.CHANNEL;
      replay.NICKNAME = receiver.getNickName();
      sendToUser(replay);
   }

   private boolean spamProtectorDetectsDanger(Message message) {
      User user = ActiveUserController.getInstance().getUser(message.SENDER);
      boolean isSpamming = user.userIsSpammingServer();
      if (isSpamming) {
         sendErrorToUser(MessageType.WARNING, message.SENDER, message.CHANNEL, "Stop spamming the chat...");
      }
      return isSpamming;
   }


}
