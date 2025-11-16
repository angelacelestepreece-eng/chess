package dataaccess.memorydataaccess;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import exception.ResponseException;

import java.util.Collection;

public class MemoryDataAccess implements DataAccess {
    private final UserDAO users = new UserMemoryDataAccess();
    private final AuthDAO auths = new AuthMemoryDataAccess();
    private final GameDAO games = new GameMemoryDataAccess();

    public void clear() throws ResponseException {
        users.clear();
        auths.clear();
        games.clear();
    }

    public void createUser(UserData user) throws ResponseException {
        users.createUser(user);
    }

    public UserData getUser(String username) throws ResponseException {
        return users.getUser(username);
    }

    public AuthData createAuth(UserData user) throws ResponseException {
        return auths.createAuth(user);
    }

    public AuthData getAuth(String authToken) throws ResponseException {
        return auths.getAuth(authToken);
    }

    public void deleteAuth(String authToken) throws ResponseException {
        auths.deleteAuth(authToken);
    }

    public Collection<GameData> getGames() throws ResponseException {
        return games.getGames();
    }

    public GameData createGame(String gameName) throws ResponseException {
        return games.createGame(gameName);
    }

    public void saveGame(GameData game) throws ResponseException {
        games.saveGame(game);
    }

    public void updateGame(GameData game) throws ResponseException {
        games.updateGame(game);
    }

    public GameData getGame(int gameID) throws ResponseException {
        return games.getGame(gameID);
    }
}
