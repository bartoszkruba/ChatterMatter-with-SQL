package server.serverApp;

import models.Message;
import models.Sendable;
import models.User;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public class ActiveUserController {
   private static ActiveUserController ourInstance = new ActiveUserController();

   public static ActiveUserController getInstance() {
      return ourInstance;
   }

   private ConcurrentHashMap<User, LinkedBlockingDeque<Sendable>> connectedUsers = new ConcurrentHashMap<>();

   private ActiveUserController() {
   }

   public boolean addUser(User user, LinkedBlockingDeque<Sendable> outbox) {
      if (this.connectedUsers.containsKey(user)) {
         return false;
      }
      this.connectedUsers.put(user, outbox);
      return true;
   }

   public boolean removeUser(User user) {
      // TODO: 2019-02-13 Remove user from all channels
      ActiveChannelController.getInstance().removeUserFromChannels(user);
      return this.connectedUsers.remove(user) != null;
   }

   public LinkedBlockingDeque<Sendable> getUserOutbox(User user) {
      return connectedUsers.get(user);
   }

   public LinkedBlockingDeque<Sendable> getUserOutbox(UUID ID) {
      for (Map.Entry e : this.connectedUsers.entrySet()) {
         UUID entryID = ((User) e.getKey()).getID();
         if (entryID.equals(ID)) {
            return (LinkedBlockingDeque) e.getValue();
         }
      }
      return null;
   }

   public Map getUsers() {
      return Collections.unmodifiableMap(this.connectedUsers);
   }

   public void printUsers(){
      this.connectedUsers.keySet().stream().sorted(Comparator.comparing(User::getNickName)).forEach(user -> {
        System.out.println(user.getNickName() +" "+ user.getID());
      });

   }

   public User getUser(UUID ID) {
      for (Map.Entry e : this.connectedUsers.entrySet()) {
         if (((User) e.getKey()).getID().equals(ID)) {
            return (User) e.getKey();
         }
      }
      return null;
   }

}
