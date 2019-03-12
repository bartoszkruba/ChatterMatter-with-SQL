package models;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable, Sendable {

    public UUID SENDER;
    public UUID RECEIVER;
    public String NICKNAME;
    public MessageType TYPE;
    public String TEXT_CONTENT;
    public String CHANNEL;
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

    @Override
    public String toString() {
        return "I am a message that was sent as an object :)";
    }
}
