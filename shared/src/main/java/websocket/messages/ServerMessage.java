package websocket.messages;

import model.GameData;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    private final ServerMessageType serverMessageType;
    private final String messageText;
    private final GameData gameData;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
        this.messageText = null;
        this.gameData = null;
    }

    public ServerMessage(GameData gameData) {
        this.serverMessageType = ServerMessageType.LOAD_GAME;
        this.gameData = gameData;
        this.messageText = null;
    }

    public ServerMessage(ServerMessageType type, String messageText) {
        if (type == ServerMessageType.LOAD_GAME) {
            throw new IllegalArgumentException("Use the GameData constructor for LOAD_GAME");
        }
        this.serverMessageType = type;
        this.messageText = messageText;
        this.gameData = null;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    public String getMessageText() {
        return messageText;
    }

    public GameData getGameData() {
        return gameData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage that)) {
            return false;
        }
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}
