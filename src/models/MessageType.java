package models;

import java.io.Serializable;

public enum MessageType implements Serializable {
    CONNECT,
    DISCONNECT,
    CHANNEL_MESSAGE,
    WHISPER_MESSAGE,
    NICKNAME_CHANGE,
    JOIN_CHANNEL,
    LEAVE_CHANNEL,
    ERROR,
    WARNING
}
