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
    private final String message;
    private final String errorMessage;
    private final GameData game;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
        this.message = null;
        this.game = null;
        this.errorMessage = null;
    }

    public ServerMessage(GameData game) {
        this.serverMessageType = ServerMessageType.LOAD_GAME;
        this.game = game;
        this.message = null;
        this.errorMessage = null;
    }

    public ServerMessage(String message) {
        this.serverMessageType = ServerMessageType.NOTIFICATION;
        this.message = message;
        this.errorMessage = null;
        this.game = null;
    }

    public ServerMessage(ServerMessageType type, String errorMessage) {
        if (type == ServerMessageType.LOAD_GAME) {
            throw new IllegalArgumentException("This constructor is only for ERROR messages");
        }
        this.serverMessageType = type;
        this.message = null;
        this.game = null;
        this.errorMessage = errorMessage;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public GameData getGame() {
        return game;
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
