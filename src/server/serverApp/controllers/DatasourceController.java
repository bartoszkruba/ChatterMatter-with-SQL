package server.serverApp.controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatasourceController extends Thread {
   private static DatasourceController ourInstance = new DatasourceController();

   public static DatasourceController getInstance() {
      return ourInstance;
   }

   private final String DB_NAME = "chatter_matter";
   private final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/" + DB_NAME;
   private final String USER = "root";
   private final String PASSWORD = "password";

   private Connection conn;

   private boolean running;

   private DatasourceController() {

   }

   public boolean openConnection() {
      try {
         this.conn = DriverManager.getConnection(CONNECTION_STRING, USER, PASSWORD);
         return true;
      } catch (SQLException e) {
         System.out.println("Couldn't open connection: " + e.getMessage());
         return false;
      }
   }

   public void closeConnection() {
      try {
         if (conn != null) {
            conn.close();
         }
      } catch (SQLException e) {
         System.out.println("Couldn't close connection: " + e.getMessage());
      }
   }

   @Override
   public void run() {
      running = true;
      while (running) {
         try {
            Thread.sleep(10);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      }
      this.closeConnection();
   }

   public void setRunning(boolean running) {
      this.running = running;
   }
}
