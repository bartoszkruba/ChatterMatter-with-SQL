package server.serverApp.controllers;

import models.Channel;
import models.User;

import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class ActiveChannelController {
   private static ActiveChannelController ourInstance = new ActiveChannelController();

   public static ActiveChannelController getInstance() {
      return ourInstance;
   }


   // TODO: 2019-02-14 consider changing set to treemap with UUID as key and channel som value
   private ConcurrentSkipListSet<Channel> channels;

   private ActiveChannelController() {
      this.channels = new ConcurrentSkipListSet<>();
   }


   public Channel getChannel(String name) {
      for (Channel c : this.channels) {
         if (c.getName().equals(name)) {
            return c;
         }
      }
      return null;
   }

   public int getAmountOfChannels() {
      return this.channels.size();

   }

   public boolean removeUserFromChannel(UUID userID, String channel) {
      boolean value = this.getChannel(channel).removeUser(ActiveUserController.getInstance().getUser(userID));
      if (this.getChannel(channel).getUsers().size() == 0) {
         this.removeChannel(channel);
      }
      return value;
   }

   public boolean addChannel(String name) {
      return channels.add(new Channel(name));
   }

   public boolean removeChannel(String name) {

      var n = new Object() {
         boolean value = false;
      };

      this.channels = channels.stream()
              .filter(c -> {
                 if (!c.getName().equals(name)) {
                    return true;
                 }
                 n.value = true;
                 return false;
              })
              .collect(Collectors.toCollection(ConcurrentSkipListSet::new));

      return n.value;
   }

   public void removeUserFromChannels(User user) {
      this.channels.forEach(c -> c.removeUser(user));
   }

   public boolean userIsInChannel(String channel, User user){
      return getChannel(channel) != null && getChannel(channel).getUsers().contains(user);
   }

   public void addUserToChannel(User user, String name) {
      Channel channel = null;
      try {
         channel = this.channels.stream()
                 .filter(c -> c.getName().equals(name))
                 .toArray(Channel[]::new)[0];
      } catch (ArrayIndexOutOfBoundsException e) {
      }

      if (channel == null) {
         this.addChannel(name);
         this.addUserToChannel(user, name);
      } else {
         channel.addUser(user);
      }

   }

   public String[] getChannelsForUser(User user) {
      return this.channels.stream()
              .filter(c -> c.getUsers().contains(user))
              .map(c -> c.getName())
              .toArray(String[]::new);
   }

   public String[] getChannelList(){
      return this.channels.stream().map(channel -> channel.getName()).sorted(Comparator.comparing(String::toLowerCase)).toArray(String[]::new);
   }



}
