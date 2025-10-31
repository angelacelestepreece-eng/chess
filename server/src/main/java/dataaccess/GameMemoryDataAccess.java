package dataaccess;

import chess.ChessGame;
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
        games.put(game.gameID(), game);
    }

    public GameData getGame(int gameID) throws ResponseException {
        return games.get(gameID);
    }

    public void clear() throws ResponseException {
        games.clear();
    }

}
