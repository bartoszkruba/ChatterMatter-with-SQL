package client.clientApp.network;

import client.clientApp.Client;
import client.clientApp.MessageInboxHandler;
import models.Channel;
import models.Message;
import models.Sendable;
import models.Encryption;

import javax.crypto.SealedObject;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class Receiver extends Thread {
   private Socket socket;
   private ObjectInputStream objectInputStream;
   private Encryption decrypt;

   public Receiver(Socket socket) {
      this.decrypt = new Encryption();
      this.setSocket(socket);
   }

   public void setSocket(Socket socket) {
      this.socket = socket;
      try {
         this.objectInputStream = new ObjectInputStream(socket.getInputStream());
      } catch (Exception e) {
         Client.getInstance().tryReconnect();
      }
   }

   @Override
   public void run() {
      while (Client.getInstance().isRunning()) {
         while (socket.isClosed() && Client.getInstance().isRunning()){
            // receiver waiting for reconnect
            try{
               Thread.sleep(100);
            }catch (Exception e){}
         }
         try {
            SealedObject encryptedObject = (SealedObject) objectInputStream.readObject();
            Sendable inData = (Sendable) decrypt.decryptObject(encryptedObject);
            if (inData instanceof Message) {
               Message message = (Message) inData;
               MessageInboxHandler.getInstance().messageSwitch(message);
            } else if (inData instanceof Channel) {
               Channel channel = (Channel) inData;
               Client.getInstance().channelList.put(channel.getName(), channel.getUsers());
               MessageInboxHandler.getInstance().addChannel(channel);
            }
         } catch (ClassNotFoundException e) {
         } catch (Exception e) {
            Client.getInstance().tryReconnect();
         }
      }
   }
}
