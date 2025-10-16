package dataAccess;

import model.AuthData;
import model.UserData;

public interface DataAccess {
    void clear();

    void createUser(UserData user);

    UserData getUser(String username);

    void createAuth(UserData user);

    AuthData getAuth(String auth);
}
