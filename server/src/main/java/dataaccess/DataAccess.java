package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

import exception.ResponseException;

public interface DataAccess {
    void clear() throws ResponseException;

    void createUser(UserData user) throws ResponseException;

    UserData getUser(String username) throws ResponseException;

    AuthData createAuth(UserData user) throws ResponseException;

    AuthData getAuth(String auth) throws ResponseException;

    void deleteAuth(String authToken) throws ResponseException;

    Collection<GameData> getGames() throws ResponseException;

    GameData createGame(String gameName) throws ResponseException;

    void saveGame(GameData game) throws ResponseException;

    void updateGame(GameData game) throws ResponseException;

    GameData getGame(int gameID) throws ResponseException;
}
