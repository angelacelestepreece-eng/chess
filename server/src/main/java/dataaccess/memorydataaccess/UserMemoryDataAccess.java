package dataaccess.memorydataaccess;

import dataaccess.UserDAO;
import model.UserData;
import exception.ResponseException;

import java.util.HashMap;

import org.mindrot.jbcrypt.BCrypt;

public class UserMemoryDataAccess implements UserDAO {
    private final HashMap<String, UserData> users = new HashMap<>();

    public void createUser(UserData user) throws ResponseException {
        if (users.containsKey(user.username())) {
            throw new ResponseException(ResponseException.Code.ServerError, "User already exists");
        }

        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        UserData securedUser = new UserData(user.username(), hashedPassword, user.email());
        users.put(user.username(), securedUser);
    }


    public UserData getUser(String username) {
        return users.get(username);
    }

    public void clear() {
        users.clear();
    }

}
