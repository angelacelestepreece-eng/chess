package ui.websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import exception.ResponseException;
import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    private Session session;
    private final NotificationHandler notificationHandler;
    private final Gson gson = new Gson();

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler((MessageHandler.Whole<String>) message -> {
                ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
                notificationHandler.notify(serverMessage);
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void connect(String authToken, int gameId) throws ResponseException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameId));
    }

    public void sendMove(String authToken, int gameId, ChessMove move) throws ResponseException {
        UserGameCommand cmd = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameId);
        cmd.setMove(move);
        sendCommand(cmd);
    }

    public void sendLeave(String authToken, int gameId) throws ResponseException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameId));
    }

    public void sendResign(String authToken, int gameId) throws ResponseException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameId));
    }

    private void sendCommand(UserGameCommand cmd) throws ResponseException {
        try {
            this.session.getBasicRemote().sendText(gson.toJson(cmd));
        } catch (IOException ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

}

