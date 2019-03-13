package server.serverApp.controllers;

import models.Message;
import models.MessageType;
import models.annotations.ObjectMapper;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

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

   private final String INSERT_INTO_MESSAGES = "INSERT INTO " + TABLE_MESSAGES + "(" +
           COLUMN_MESSAGES_CHANNEL + ", " + COLUMN_MESSAGES_TEXT_CONTENT + ", " +
           COLUMN_MESSAGES_NICKNAME + ", " + COLUMN_MESSAGES_TYPE + ") VALUES (?, ?, ?, ?)";

   private final String QUERY_MESSAGES_FROM_CHANNEL = "SELECT * FROM " + TABLE_MESSAGES +
           " WHERE " + COLUMN_MESSAGES_CHANNEL + " = ?";

   private Connection conn;
   private PreparedStatement insertIntoMessages;
   private PreparedStatement queryAllMessagesFromChannel;

   private boolean running;
   private LinkedBlockingQueue<Message> messagesToSave;

   public DatasourceController() {
      messagesToSave = new LinkedBlockingQueue<>();
   }

   public boolean openConnection() {
      try {
         this.conn = DriverManager.getConnection(CONNECTION_STRING, USER, PASSWORD);
         this.insertIntoMessages = conn.prepareStatement(INSERT_INTO_MESSAGES);
         this.queryAllMessagesFromChannel = conn.prepareStatement(QUERY_MESSAGES_FROM_CHANNEL);

         return true;
      } catch (SQLException e) {
         System.out.println("Couldn't open connection: " + e.getMessage());
         return false;
      }
   }

   public void closeConnection() {
      try {

         closePreparedStatement(queryAllMessagesFromChannel);
         closePreparedStatement(insertIntoMessages);

         if (conn != null) {
            conn.close();
         }
      } catch (SQLException e) {
         System.out.println("Couldn't close connection: " + e.getMessage());
      }
   }

   private void closePreparedStatement(PreparedStatement statement) throws SQLException {
      if (statement != null) {
         statement.close();
      }
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

   private boolean insertIntoMessages(String channel, String text_content, String nickname, MessageType messageType) {
      try {
         insertIntoMessages.setString(1, channel);
         insertIntoMessages.setString(2, text_content);
         insertIntoMessages.setString(3, nickname);
         insertIntoMessages.setString(4, messageType.toString());

         insertIntoMessages.executeUpdate();
         return true;
      } catch (SQLException e) {
         System.out.println("Couldn't insert message: " + e.getMessage());
         return false;
      }
   }

   public void addMessageToSave(Message message) {
      this.messagesToSave.add(message);
   }

   private void saveMessage(Message message) {
      MessageType type = message.TYPE;
      String channel = message.CHANNEL;
      String text_content = message.TEXT_CONTENT;
      String nickname = message.NICKNAME;
      if ((type == MessageType.CHANNEL_MESSAGE || type == MessageType.NICKNAME_CHANGE) &&
              channel != null &&
              text_content != null &&
              nickname != null) {
         boolean success = insertIntoMessages(channel, text_content, nickname, type);
         while (!success && running) {
            success = insertIntoMessages(channel, text_content, nickname, type);
         }
      } else {
         System.out.println("Couldn't save message: Some fields are empty or contain incorrect data");
      }
   }

   public List<Message> queryAllMessagesFromChannel(String channel) {
      System.out.println("quering messages");
      List<Message> messages = new ArrayList<>();
      try (PreparedStatement statement = conn.prepareStatement(QUERY_MESSAGES_FROM_CHANNEL);) {
         statement.setString(1, channel);
         ResultSet results = statement.executeQuery();

         ObjectMapper<Message> objectMapper = new ObjectMapper<>(Message.class);

         messages = objectMapper.map(results);

      } catch (SQLException e) {
         System.out.println("Couldn't query message: " + e.getMessage());
      }
      return messages;
   }

   @Override
   public void run() {
      running = true;
      while (running) {
         if (!messagesToSave.isEmpty()) {
            saveMessage(messagesToSave.remove());
         }
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
