package server.serverApp;


import models.Sendable;
import server.serverApp.controllers.DatasourceController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerApp {

   private ServerSocket serverSocket;
   private final int PORT = 54322;
   private ExecutorService clientHandlers;
   private MessageHandler messageHandler;
   private DatasourceController datasource;
   private boolean running = false;
   private final LinkedBlockingQueue<Sendable> messageHandlerQueue;
   AdminSystemMonitoring adminSystemMonitoring;

   public ServerApp() {

      try {
         serverSocket = new ServerSocket(PORT);
         running = true;
      } catch (IOException e) {
         System.out.println("Could not create a server");
      } catch (Exception e) {
         e.printStackTrace();
      }

      clientHandlers = Executors.newCachedThreadPool();
      messageHandlerQueue = new LinkedBlockingQueue<>();
      adminSystemMonitoring = new AdminSystemMonitoring(this);
      messageHandler = new MessageHandler(messageHandlerQueue, adminSystemMonitoring);
      datasource = DatasourceController.getInstance();
   }

   public boolean isRunning() {
      return running;
   }


   void kill() {
      clientHandlers.shutdown();
      messageHandler.kill();
      datasource.setRunning(false);
      running = false;
      try {
         serverSocket.close();
      } catch (Exception e) {
      }
   }

   public void run() {
      if (!datasource.openConnection()) {
         System.out.println("Closing server");
         this.kill();
         return;
      }
      new Thread(messageHandler).start();
      new Thread(adminSystemMonitoring).start();
      datasource.start();
      System.out.println("Server is running on port " + PORT);
      System.out.println("Type 'help' for command list");
      while (running) {
         try {
            Socket socket = serverSocket.accept();
            clientHandlers.submit(new ClientHandler(socket, this, messageHandlerQueue));
         } catch (IOException e) {
            adminSystemMonitoring.addToLog("Failed to connect to client");
         } catch (Exception e) {
            adminSystemMonitoring.addToLog(e.getMessage());
         }
      }
   }
}