package server.serverApp;

import models.Message;
import models.MessageType;
import models.Sendable;
import models.User;
import models.Encryption;

import javax.crypto.SealedObject;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class ClientHandler implements Runnable {

   private ObjectOutputStream streamOut;
   private ObjectInputStream streamIn;
   private Socket socket;
   private ServerApp serverApp;
   private boolean isRunning = false;
   private final int TIMEOUT_MS = 50;
   private final User user;
   private final LinkedBlockingDeque<Sendable> userOutbox;
   private final LinkedBlockingQueue<Sendable> messageHandlerQueue;
   private final int WAIT_IN_SETUP = 500;
   private Encryption encryption;

   public ClientHandler(Socket socket, ServerApp serverApp, LinkedBlockingQueue<Sendable> messageHandlerQueue) {
      this.socket = socket;
      this.serverApp = serverApp;
      this.user = new User(createRandomNick());
      this.userOutbox = new LinkedBlockingDeque<>();
      this.messageHandlerQueue = messageHandlerQueue;
      this.encryption = new Encryption();

      ActiveUserController.getInstance().addUser(this.user, this.userOutbox);


      try {
         streamIn = new ObjectInputStream(socket.getInputStream());
         streamOut = new ObjectOutputStream(socket.getOutputStream());
         socket.setSoTimeout(TIMEOUT_MS);
         isRunning = true;
      } catch (IOException e) {
         serverApp.adminSystemMonitoring.addToLog("Failed to create streams with "+socket.getInetAddress());
      } catch (Exception e) {
         serverApp.adminSystemMonitoring.addToLog(e.getMessage()+socket.getInetAddress());
      }

      Message m = new Message(MessageType.CONNECT);
      m.RECEIVER = this.user.getID();
      m.NICKNAME = user.getNickName();
      this.userOutbox.add(m);

      serverApp.adminSystemMonitoring.addToLog(socket.getInetAddress().toString() + " connected");
   }

   private String createRandomNick(){
      String[] alpha = new String[]{"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
      String name = "User_";
      for (int i = 0; i < 5; i++) {
         name = name.concat((alpha[(int)(ThreadLocalRandom.current().nextDouble() * alpha.length)]));
      }
      return name;
   }

   private void readMessage() {
      // TODO: 2019-02-14 try reconnect

      try {
         SealedObject encryptedObject = (SealedObject) streamIn.readObject();
         Message message = (Message) encryption.decryptObject(encryptedObject);
         message.SENDER = this.user.getID();
         message.NICKNAME = this.user.getNickName();
         messageHandlerQueue.add(message);
      } catch (SocketTimeoutException e) {
      } catch (IOException e) {
         // Kolla om möjlig återanslutning till server
         serverApp.adminSystemMonitoring.addToLog("Error in Clientreader " + socket.getInetAddress());
         tryDisconnect();
         // todo kolla om klienten är död, prova återanslutning
      } catch (ClassNotFoundException e) {
         serverApp.adminSystemMonitoring.addToLog("Faulty class sent from " + socket.getInetAddress());
      } catch (Exception e) {
         serverApp.adminSystemMonitoring.addToLog(e.getMessage());
      }
   }

   private void writeMessage() {
      if (clientHasMessages()) {
         Sendable m = this.userOutbox.getFirst();
         SealedObject encryptedObject = encryption.encryptObject(m);
         try{
            streamOut.reset();
            streamOut.writeObject(encryptedObject);
            this.userOutbox.removeFirst();
         }catch(IOException e){
            serverApp.adminSystemMonitoring.addToLog("Error in Clientwriter " + socket.getInetAddress());
            tryDisconnect();
         }
      }
   }

   private boolean clientHasMessages() {
      return userOutbox.size() > 0;
   }

   private void tryDisconnect() {
      cleanUpAfterUser();
      this.isRunning = false;
      serverApp.adminSystemMonitoring.addToLog("Connection to " + socket.getInetAddress() + " lost");

   }

   private void cleanUpAfterUser() {
      String[] userChannels = ActiveChannelController.getInstance().getChannelsForUser(this.user);
      Stream.of(userChannels).forEach(c -> {
         Message message = new Message(MessageType.DISCONNECT);
         message.CHANNEL = c;
         message.SENDER = this.user.getID();
         message.NICKNAME = this.user.getNickName();
         this.messageHandlerQueue.add(message);
      });
      ActiveUserController.getInstance().removeUser(this.user);
      try {
         this.socket.close();
      } catch (IOException e) {
      }
   }

   public void waitForClient(){
      try{
         Thread.sleep(WAIT_IN_SETUP);
      }catch(Exception e){}
   }

   @Override
   public void run() {
      waitForClient();
      while (serverApp.isRunning() && this.isRunning) {
         readMessage();
         writeMessage();
      }
   }
}
