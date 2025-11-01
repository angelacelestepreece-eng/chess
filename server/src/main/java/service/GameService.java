package service;

import dataaccess.DataAccess;
import dataaccess.ResponseException;
import datamodel.CreateGameResult;
import datamodel.ListGamesResult;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public ListGamesResult listGames(String authToken) throws ServiceException {
        if (authToken == null || authToken.isBlank()) {
            throw new ServiceException(401, "Error: unauthorized");
        }

        try {
            AuthData authData = dataAccess.getAuth(authToken);
            if (authData == null) {
                throw new ServiceException(401, "Error: unauthorized");
            }
            Collection<GameData> games = dataAccess.getGames();
            return new ListGamesResult(games);
        } catch (ResponseException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Internal Server Error";
            throw new ServiceException(500, "Error: " + msg);
        }
    }

    public CreateGameResult createGame(String gameName, String authToken) throws ServiceException {
        if (gameName == null || gameName.isBlank()) {
            throw new ServiceException(400, "Error: bad request");
        }
        if (authToken == null || authToken.isBlank()) {
            throw new ServiceException(401, "Error: unauthorized");
        }

        try {
            AuthData authData = dataAccess.getAuth(authToken);
            if (authData == null) {
                throw new ServiceException(401, "Error: unauthorized");
            }
            GameData gameData = dataAccess.createGame(gameName);
            int gameID = gameData.gameID();
            return new CreateGameResult(gameID);
        } catch (ResponseException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Internal Server Error";
            throw new ServiceException(500, "Error: " + msg);
        }
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws ServiceException {
        if (authToken == null || authToken.isBlank() || playerColor == null || playerColor.isBlank()) {
            throw new ServiceException(400, "Error: bad request");
        }

        try {
            AuthData authData = dataAccess.getAuth(authToken);
            if (authData == null) {
                throw new ServiceException(401, "Error: unauthorized");
            }
            GameData game = dataAccess.getGame(gameID);
            if (game == null) {
                throw new ServiceException(400, "Error: bad request");
            }

            String username = authData.username();
            GameData updatedGame;

            switch (playerColor.toLowerCase()) {
                case "white" -> {
                    if (game.whiteUsername() != null) {
                        throw new ServiceException(403, "Error: already taken");
                    }
                    updatedGame = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
                }
                case "black" -> {
                    if (game.blackUsername() != null) {
                        throw new ServiceException(403, "Error: already taken");
                    }
                    updatedGame = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
                }
                default -> throw new ServiceException(400, "Error: bad request");
            }

            dataAccess.updateGame(updatedGame);
        } catch (ResponseException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Internal Server Error";
            throw new ServiceException(500, "Error: " + msg);
        }
    }

    public void clear() throws ServiceException {
        try {
            dataAccess.clear();
        } catch (ResponseException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Internal Server Error";
            throw new ServiceException(500, "Error: " + msg);
        }
    }
}
