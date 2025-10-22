package dataAccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GameMemoryDataAccess implements GameDAO {
    private HashMap<Integer, GameData> games = new HashMap<>();

    public Collection<GameData> getGames() {
        return games.values();
    }

    public GameData createGame(String gameName) {
        int newGameID = gameCounter.getAndIncrement();
        GameData gameData = new GameData(newGameID, null, null, gameName, new ChessGame());
        saveGame(gameData);
        return gameData;
    }

    public void saveGame(GameData game) {
        games.put(game.gameID(), game);
    }

    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    public void clear() {
        games.clear();
    }

    private final AtomicInteger gameCounter = new AtomicInteger(1);

}
