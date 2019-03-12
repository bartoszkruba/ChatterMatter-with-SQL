package server.serverApp.controllers;

import models.MessageType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatasourceController extends Thread {
   private static DatasourceController ourInstance = new DatasourceController();

   public static DatasourceController getInstance() {
      return ourInstance;
   }

   private final String DB_NAME = "chatter_matter";
   private final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/" + DB_NAME;
   private final String USER = "root";
   private final String PASSWORD = "password";

   private final String TABLE_MESSAGES = "messages";
   private final String COLUMN_MESSAGES_CHANNEL = "channel";
   private final String COLUMN_MESSAGES_TIMESTAMP = "message_timestamp";
   private final String COLUMN_MESSAGES_TEXT_CONTENT = "text_content";
   private final String COLUMN_MESSAGES_NICKNAME = "nickname";
   private final String COLUMN_MESSAGES_TYPE = "message_type";

   private final String CREATE_TABLE_MESSAGES = "CREATE TABLE IF NOT EXISTS " + TABLE_MESSAGES +
           "(" +
           COLUMN_MESSAGES_CHANNEL + " VARCHAR(200) NOT NULL, " +
           COLUMN_MESSAGES_TIMESTAMP + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
           COLUMN_MESSAGES_TEXT_CONTENT + " VARCHAR(255) NOT NULL, " +
           COLUMN_MESSAGES_NICKNAME + " VARCHAR(50) NOT NULL, " +
           COLUMN_MESSAGES_TYPE + " ENUM('" + MessageType.CHANNEL_MESSAGE + "', '" + MessageType.NICKNAME_CHANGE + "') NOT NULL, " +
           "PRIMARY KEY (" + COLUMN_MESSAGES_CHANNEL + ", " + COLUMN_MESSAGES_TIMESTAMP + ")" +
           ")";

   private Connection conn;

   private boolean running;

   private DatasourceController() {

   }

   public boolean createTables() {
      return this.createTableMessages();
   }

   public boolean createTableMessages() {
      String sql = CREATE_TABLE_MESSAGES;
      try (Statement statement = conn.createStatement()) {
         System.out.println(sql);
         statement.executeUpdate(sql);
         return true;
      } catch (SQLException e) {
         System.out.println("Couldn't create table " + TABLE_MESSAGES + ": " + e.getMessage());
         return false;
      }
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
