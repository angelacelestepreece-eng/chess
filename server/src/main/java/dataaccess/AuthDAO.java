package dataAccess;

import model.AuthData;
import model.UserData;

public interface AuthDAO {
    AuthData createAuth(UserData user);

    AuthData getAuth(String authToken);

    void deleteAuth(String authToken);

    void clear();
}
