package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO {
    GameData createGame(String gameName) throws ResponseException;

    void saveGame(GameData game) throws ResponseException;

    void updateGame(GameData game) throws ResponseException;

    GameData getGame(int gameID) throws ResponseException;

    Collection<GameData> getGames() throws ResponseException;

    void clear() throws ResponseException;
}
