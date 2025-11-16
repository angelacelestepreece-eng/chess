package service;

import dataaccess.DataAccess;
import datamodel.CreateGameResult;
import datamodel.ListGamesResult;
import model.AuthData;
import model.GameData;
import exception.ResponseException;

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
            GameData gameData = dataAccess.createGame(gameName);
            return new CreateGameResult(gameData.gameID());
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
}
