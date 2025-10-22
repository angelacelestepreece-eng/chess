package dataAccess;

import model.AuthData;
import model.UserData;

import java.util.HashMap;
import java.util.UUID;

public class AuthMemoryDataAccess implements AuthDAO {
    private HashMap<String, AuthData> auths = new HashMap<>();

    public AuthData createAuth(UserData user) {
        String authToken = generateToken();
        AuthData auth = new AuthData(user.username(), authToken);
        auths.put(authToken, auth);
        return auth;
    }

    public AuthData getAuth(String authToken) {
        return auths.get(authToken);
    }

    public void deleteAuth(String authToken) {
        auths.remove(authToken);
    }

    public void clear() {
        auths.clear();
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
