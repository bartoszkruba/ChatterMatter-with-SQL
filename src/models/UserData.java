package models;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserData implements Serializable {

   private String username;
   private HashSet<UUID> ignoreList;
   private HashSet<String> joinedChannels;
   private boolean darkMode;
   private String IP;

   public UserData() {
      this.username = "Unknown";
      this.ignoreList = new HashSet<>();
      this.joinedChannels = new HashSet<>();
      darkMode = false;
   }

   public UserData(String username, HashSet<UUID> ignoreList) {
      this.username = username;
      this.ignoreList = ignoreList;
      this.joinedChannels = new HashSet<>();
      darkMode = false;
   }

   public UserData setUsername(String username) {
      this.username = username;
      return this;
   }


   public UserData addIgnore(UUID ignoreID) {
      this.ignoreList.add(ignoreID);
      return this;
   }

   public UserData addIgnores(Collection<UUID> ignoreIDs) {
      this.ignoreList.addAll(ignoreIDs);
      return this;
   }

   public String getUsername() {
      return username;
   }

   public HashSet<UUID> getIgnoreList() {
      return ignoreList;
   }

   public boolean addJoinedChannel(String channel) {
      return this.joinedChannels.add(channel);
   }

   public HashSet<String> getJoinedChannels() {
      return joinedChannels;
   }


   public boolean isDarkMode() {
      return darkMode;
   }

   public void setDarkMode(boolean darkMode) {
      this.darkMode = darkMode;
   }

   public String getIP() {
      return IP;
   }

   public void setIP(String IP) {
      this.IP = IP;
   }
}
