package dataAccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public class MemoryDataAccess implements DataAccess {
    private final UserDAO users = new UserMemoryDataAccess();
    private final AuthDAO auths = new AuthMemoryDataAccess();
    private final GameDAO games = new GameMemoryDataAccess();

    public void clear() {
        users.clear();
        auths.clear();
        games.clear();
    }

    public void createUser(UserData user) {
        users.createUser(user);
    }

    public UserData getUser(String username) {
        return users.getUser(username);
    }

    public AuthData createAuth(UserData user) {
        return auths.createAuth(user);
    }

    public AuthData getAuth(String authToken) {
        return auths.getAuth(authToken);
    }

    public void deleteAuth(String authToken) {
        auths.deleteAuth(authToken);
    }

    public Collection<GameData> getGames() {
        return games.getGames();
    }

    public GameData createGame(String gameName) {
        return games.createGame(gameName);
    }

    public void saveGame(GameData game) {
        games.saveGame(game);
    }

    public GameData getGame(int gameID) {
        return games.getGame(gameID);
    }
}
