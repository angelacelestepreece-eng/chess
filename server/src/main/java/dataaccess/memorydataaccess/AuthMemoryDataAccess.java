package dataaccess.memorydataaccess;

import dataaccess.AuthDAO;
import model.AuthData;
import model.UserData;

import java.util.HashMap;
import java.util.UUID;

import exception.ResponseException;

public class AuthMemoryDataAccess implements AuthDAO {
    private final HashMap<String, AuthData> auths = new HashMap<>();

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public AuthData createAuth(UserData user) throws ResponseException {
        String authToken = generateToken();
        AuthData auth = new AuthData(user.username(), authToken);
        auths.put(authToken, auth);
        return auth;
    }

    public AuthData getAuth(String authToken) throws ResponseException {
        return auths.get(authToken);
    }

    public void deleteAuth(String authToken) throws ResponseException {
        if (!auths.containsKey(authToken)) {
            throw new ResponseException(ResponseException.Code.ServerError, "Auth token not found");
        }
        auths.remove(authToken);
    }

    public void clear() throws ResponseException {
        auths.clear();
    }
}
