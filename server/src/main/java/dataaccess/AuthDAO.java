package dataaccess;

import model.AuthData;
import model.UserData;
import exception.ResponseException;

public interface AuthDAO {
    AuthData createAuth(UserData user) throws ResponseException;

    AuthData getAuth(String authToken) throws ResponseException;

    void deleteAuth(String authToken) throws ResponseException;

    void clear() throws ResponseException;
}
