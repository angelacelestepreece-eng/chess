package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO {
    GameData createGame(String gameName);

    void saveGame(GameData game);

    GameData getGame(int gameID);

    Collection<GameData> getGames();

    void clear();
}
