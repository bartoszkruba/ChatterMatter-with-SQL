package models;

import models.annotations.Column;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Message implements Serializable, Sendable {

   public UUID SENDER;
   public UUID RECEIVER;

   @Column("nickname")
   public String NICKNAME;
   @Column("message_type")
   public MessageType TYPE;
   @Column("text_content")
   public String TEXT_CONTENT;
   @Column("channel")
   public String CHANNEL;
   @Column("message_timestamp")
   public String TIMESTAMP;

   public Message() {
      this.SENDER = null;
      this.RECEIVER = null;
      this.NICKNAME = null;
      this.TYPE = null;
      this.TEXT_CONTENT = null;
      this.CHANNEL = null;
      this.TIMESTAMP = null;
   }

   public Message(MessageType type) {
      this.TYPE = type;
      this.SENDER = null;
      this.RECEIVER = null;
      this.NICKNAME = null;
      this.TEXT_CONTENT = null;
      this.CHANNEL = null;
      this.TIMESTAMP = null;
   }

   public Message setSENDER(UUID SENDER) {
      this.SENDER = SENDER;
      return this;
   }

   public Message setRECEIVER(UUID RECEIVER) {
      this.RECEIVER = RECEIVER;
      return this;

   }

   public Message setNICKNAME(String NICKNAME) {
      this.NICKNAME = NICKNAME;
      return this;

   }

   public Message setTYPE(MessageType TYPE) {
      this.TYPE = TYPE;
      return this;

   }

   public Message setTYPE(String TYPE) throws Exception {
      switch (TYPE) {
         case "CONNECT":
            this.TYPE = MessageType.CONNECT;
            break;
         case "DISCONNECT":
            this.TYPE = MessageType.DISCONNECT;
            break;
         case "CHANNEL_MESSAGE":
            this.TYPE = MessageType.CHANNEL_MESSAGE;
            break;
         case "WHISPER_MESSAGE":
            this.TYPE = MessageType.WHISPER_MESSAGE;
            break;
         case "NICKNAME_CHANGE":
            this.TYPE = MessageType.NICKNAME_CHANGE;
            break;
         case "JOIN_CHANNEL":
            this.TYPE = MessageType.JOIN_CHANNEL;
            break;
         case "LEAVE_CHANNEL":
            this.TYPE = MessageType.LEAVE_CHANNEL;
            break;
         case "ERROR":
            this.TYPE = MessageType.ERROR;
            break;
         case "WARNING":
            this.TYPE = MessageType.ERROR;
            break;
         default:
            throw new Exception("Incorrect String");
      }
      return this;
   }

   public Message setTEXT_CONTENT(String TEXT_CONTENT) {
      this.TEXT_CONTENT = TEXT_CONTENT;
      return this;
   }

   public Message setCHANNEL(String CHANNEL) {
      this.CHANNEL = CHANNEL;
      return this;
   }

   public Message setTIMESTAMP(String TIMESTAMP) {
      this.TIMESTAMP = TIMESTAMP;
      return this;
   }

   public Message setTIMESTAMP(LocalDateTime timestamp) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
      this.TIMESTAMP = "[" + timestamp.format(formatter) + "] ";
      return this;
   }

   @Override
   public String toString() {
      return "I am a message that was sent as an object :)";
   }
}
