package dataaccess.memorydataaccess;

import chess.ChessGame;
import dataaccess.GameDAO;
import dataaccess.ResponseException;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GameMemoryDataAccess implements GameDAO {
    private final AtomicInteger gameCounter = new AtomicInteger(1);
    private final HashMap<Integer, GameData> games = new HashMap<>();

    public Collection<GameData> getGames() throws ResponseException {
        return games.values();
    }

    public GameData createGame(String gameName) throws ResponseException {
        int newGameID = gameCounter.getAndIncrement();
        GameData gameData = new GameData(newGameID, null, null, gameName, new ChessGame());
        saveGame(gameData);
        return gameData;
    }

    public void saveGame(GameData game) throws ResponseException {
        if (games.containsKey(game.gameID())) {
            throw new ResponseException(ResponseException.Code.BadRequest, "Game ID already exists");
        }
        games.put(game.gameID(), game);
    }

    public void updateGame(GameData game) throws ResponseException {
        if (!games.containsKey(game.gameID())) {
            throw new ResponseException(ResponseException.Code.NotFound, "Game ID doesn't exist");
        }
        games.put(game.gameID(), game);
    }

    public GameData getGame(int gameID) throws ResponseException {
        return games.get(gameID);
    }

    public void clear() throws ResponseException {
        games.clear();
    }

}
