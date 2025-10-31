package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public interface DataAccess {
    void clear() throws ResponseException;

    void createUser(UserData user) throws ResponseException;

    UserData getUser(String username) throws ResponseException;

    AuthData createAuth(UserData user);

    AuthData getAuth(String auth);

    void deleteAuth(String authToken);

    Collection<GameData> getGames();

    GameData createGame(String gameName);

    void saveGame(GameData game);

    GameData getGame(int gameID);
}
