package dataAccess;

import model.UserData;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {
    private HashMap<String, UserData> users = new HashMap<>();


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

}
