package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import exception.ResponseException;

import java.util.Collection;

public class MySQLDataAccess implements DataAccess {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public MySQLDataAccess() throws ResponseException {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new ResponseException(ResponseException.Code.ServerError, "Failed to create database: " + e.getMessage());
        }
        userDAO = new MySQLUserDAO();
        authDAO = new MySQLAuthDAO();
        gameDAO = new MySQLGameDAO();
    }

    @Override
    public void clear() throws ResponseException {
        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
    }

    @Override
    public void createUser(UserData user) throws ResponseException {
        userDAO.createUser(user);
    }

    @Override
    public UserData getUser(String username) throws ResponseException {
        return userDAO.getUser(username);
    }

    @Override
    public AuthData createAuth(UserData user) throws ResponseException {
        return authDAO.createAuth(user);
    }

    @Override
    public AuthData getAuth(String authToken) throws ResponseException {
        return authDAO.getAuth(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws ResponseException {
        authDAO.deleteAuth(authToken);
    }

    @Override
    public GameData createGame(String gameName) throws ResponseException {
        return gameDAO.createGame(gameName);
    }

    @Override
    public Collection<GameData> getGames() throws ResponseException {
        return gameDAO.getGames();
    }

    @Override
    public GameData getGame(int gameID) throws ResponseException {
        return gameDAO.getGame(gameID);
    }

    @Override
    public void saveGame(GameData game) throws ResponseException {
        gameDAO.saveGame(game);
    }

    @Override
    public void updateGame(GameData game) throws ResponseException {
        gameDAO.updateGame(game);
    }
}
