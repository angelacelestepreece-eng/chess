package dataAccess;

import model.AuthData;
import model.UserData;

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
    public void createAuth(UserData user) {
        String authToken = generateToken();
        AuthData auth = new AuthData(user.username(), authToken);
        auths.put(user.username(), auth);
    }

    @Override
    public AuthData getAuth(String username) {
        return auths.get(username);
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

}
