package models;

import java.io.Serializable;
import java.util.Calendar;
import java.util.UUID;


// TODO: 2019-02-13 Maybe implement comparable interface
// TODO: 2019-02-12 Undersök vad vi behöver för trädsortering.
public class User implements Serializable, Comparable<User> {

   private final UUID ID;
   private String nickName;
   private transient long spammerTimer;
   private transient byte warnings;

   public User(String nickName) {
      this.nickName = nickName;
      this.ID = UUID.randomUUID();
      this.spammerTimer = Calendar.getInstance().getTimeInMillis();
   }
   public User(String nickName, UUID id) {
      this.nickName = nickName;
      this.ID = id;
      this.spammerTimer = Calendar.getInstance().getTimeInMillis();
   }

   public boolean userIsSpammingServer(){
      long timeNow = Calendar.getInstance().getTimeInMillis();
      boolean validation = timeNow - this.spammerTimer < 200;
      this.spammerTimer = timeNow;
      return validation;
   }

   public void setNickName(String nickName){
      this.nickName = nickName;
   }

   @Override
   public int hashCode() {
      return this.ID.hashCode() + 57;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (obj instanceof UUID) {
         return this.ID.toString().equals(((UUID) obj).toString());
      }
      if (obj instanceof User) {
         User u = (User) obj;
         return this.ID.equals(u.ID);
      }

      return false;
   }

   @Override
   public int compareTo(User o) {
//      return this.nickName.compareTo(o.getNickName());
      return this.getID().toString().compareTo(o.ID.toString());
   }

   public UUID getID() {
      return ID;
   }

   public String getNickName() {
      return nickName;
   }

   @Override
   public String toString() {
      return this.getNickName();
   }
}
