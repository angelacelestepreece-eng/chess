package dataAccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import service.ServiceException;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryDataAccess implements DataAccess {
    private HashMap<String, UserData> users = new HashMap<>();
    private HashMap<String, AuthData> auths = new HashMap<>();
    private HashMap<Integer, GameData> games = new HashMap<>();


    public void saveUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public void clear() {
        users.clear();
        auths.clear();
        games.clear();
    }

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public AuthData createAuth(UserData user) {
        String authToken = generateToken();
        AuthData auth = new AuthData(user.username(), authToken);
        auths.put(authToken, auth);
        return auth;
    }

    @Override
    public AuthData getAuth(String authToken) {
        return auths.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) {
        auths.remove(authToken);
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Collection<GameData> getGames() {
        return games.values();
    }

    @Override
    public GameData createGame(String gameName) {
        int newGameID = gameCounter.getAndIncrement();
        GameData gameData = new GameData(newGameID, null, null, gameName, new ChessGame());
        saveGame(gameData);
        return gameData;
    }

    @Override
    public void saveGame(GameData game) {
        games.put(game.gameID(), game);
    }

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    private final AtomicInteger gameCounter = new AtomicInteger(1);

    public static String generateUniqueGameID() {
        return UUID.randomUUID().toString();
    }

}
