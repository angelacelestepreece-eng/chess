package service;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import dataaccess.DataAccess;
import datamodel.CreateGameResult;
import datamodel.ListGamesResult;
import exception.ResponseException;
import model.AuthData;
import model.GameData;

import java.util.Collection;

import static service.ServiceHelper.*;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public ListGamesResult listGames(String authToken) throws ServiceException {
        AuthData authData = validateAuth(dataAccess, authToken);
        try {
            Collection<GameData> games = dataAccess.getGames();
            return new ListGamesResult(games);
        } catch (ResponseException e) {
            throw wrap(e);
        }
    }

    public CreateGameResult createGame(String gameName, String authToken) throws ServiceException {
        validateNotBlank(gameName, 400, "Error: bad request");
        AuthData authData = validateAuth(dataAccess, authToken);

        try {
            GameData created = dataAccess.createGame(gameName);
            ChessGame newGame = new ChessGame();
            GameData initialized = new GameData(created.gameID(),
                    created.whiteUsername(),
                    created.blackUsername(),
                    created.gameName(),
                    newGame);
            dataAccess.updateGame(initialized);
            return new CreateGameResult(initialized.gameID());
        } catch (ResponseException e) {
            throw wrap(e);
        }
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws ServiceException {
        validateNotBlank(authToken, 400, "Error: bad request");
        validateNotBlank(playerColor, 400, "Error: bad request");

        AuthData authData = validateAuth(dataAccess, authToken);

        try {
            GameData game = dataAccess.getGame(gameID);
            if (game == null) {
                throw new ServiceException(400, "Error: bad request");
            }

            String username = authData.username();
            ChessGame currentGame = game.game();
            if (currentGame == null) {
                currentGame = new ChessGame();
            }

            GameData updatedGame;
            switch (playerColor.toLowerCase()) {
                case "white" -> {
                    if (game.whiteUsername() != null && !game.whiteUsername().equals(username)) {
                        throw new ServiceException(403, "Error: already taken");
                    }
                    updatedGame = new GameData(game.gameID(), username, game.blackUsername(),
                            game.gameName(), currentGame);
                }
                case "black" -> {
                    if (game.blackUsername() != null && !game.blackUsername().equals(username)) {
                        throw new ServiceException(403, "Error: already taken");
                    }
                    updatedGame = new GameData(game.gameID(), game.whiteUsername(), username,
                            game.gameName(), currentGame);
                }
                default -> throw new ServiceException(400, "Error: bad request");
            }

            dataAccess.updateGame(updatedGame);
        } catch (ResponseException e) {
            throw wrap(e);
        }
    }

    public void clear() throws ServiceException {
        try {
            dataAccess.clear();
        } catch (ResponseException e) {
            throw wrap(e);
        }
    }

    public GameData getGame(int gameID) throws ServiceException {
        try {
            GameData game = dataAccess.getGame(gameID);
            if (game == null) {
                throw new ServiceException(400, "Error: bad request");
            }
            return game;
        } catch (ResponseException e) {
            throw wrap(e);
        }
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws ServiceException {
        AuthData authData = validateAuth(dataAccess, authToken);

        try {
            GameData gameData = dataAccess.getGame(gameID);
            if (gameData == null) {
                throw new ServiceException(400, "Error: bad request");
            }

            ChessGame game = gameData.game();
            if (game == null) {
                throw new ServiceException(400, "Error: game is over");
            }

            try {
                game.makeMove(move);
            } catch (InvalidMoveException e) {
                throw new ServiceException(400, "Error: invalid move");
            }

            GameData updated = new GameData(gameData.gameID(), gameData.whiteUsername(),
                    gameData.blackUsername(), gameData.gameName(), game);
            dataAccess.updateGame(updated);
        } catch (ResponseException e) {
            throw wrap(e);
        }
    }

    public void resignGame(String authToken, int gameID) throws ServiceException {
        AuthData authData = validateAuth(dataAccess, authToken);

        try {
            GameData gameData = dataAccess.getGame(gameID);
            if (gameData == null) {
                throw new ServiceException(400, "Error: bad request");
            }

            GameData resigned = new GameData(gameData.gameID(), gameData.whiteUsername(),
                    gameData.blackUsername(), gameData.gameName(), null);
            dataAccess.updateGame(resigned);
        } catch (ResponseException e) {
            throw wrap(e);
        }
    }

    public void updateGame(GameData game) throws ServiceException {
        try {
            dataAccess.updateGame(game);
        } catch (ResponseException e) {
            throw wrap(e);
        }
    }
}
