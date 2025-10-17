package dataAccess;

import model.AuthData;
import model.UserData;
import service.ServiceException;

import java.util.HashMap;
import java.util.UUID;

public class MemoryDataAccess implements DataAccess {
    private HashMap<String, UserData> users = new HashMap<>();
    private HashMap<String, AuthData> auths = new HashMap<>();


    public void saveUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public void clear() {
        users.clear();
        auths.clear();
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

}
