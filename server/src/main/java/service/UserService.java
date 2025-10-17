package service;

import dataAccess.DataAccess;
import dataAccess.MemoryDataAccess;
import dataAccess.DataAccessException;
import datamodel.RegistrationResult;
import model.AuthData;
import model.UserData;

import java.util.UUID;


public class UserService {
    private final DataAccess dataAccess = new MemoryDataAccess();

    public RegistrationResult register(UserData user) throws ServiceException {
        if (user.username() == null || user.password() == null || user.email() == null ||
                user.username().isBlank() || user.password().isBlank() || user.email().isBlank()) {
            throw new ServiceException(400, "Error: bad request");
        }

        if (dataAccess.getUser(user.username()) != null) {
            throw new ServiceException(403, "Error: already taken");
        }

        dataAccess.createUser(user);
        AuthData auth = dataAccess.createAuth(user);
        return new RegistrationResult(auth.username(), auth.authToken());
    }

    public AuthData login(UserData user) throws ServiceException {
        if (user.username() == null || user.password() == null ||
                user.username().isBlank() || user.password().isBlank()) {
            throw new ServiceException(400, "Error: bad request");
        }

        UserData registeredUser = dataAccess.getUser(user.username());

        if (registeredUser == null) {
            throw new ServiceException(401, "Error: unauthorized");
        }

        if (!user.password().equals(registeredUser.password())) {
            throw new ServiceException(401, "Error: unauthorized");
        }
        return dataAccess.createAuth(user);
    }

    public void logout(String authToken) throws ServiceException {
        AuthData authData = dataAccess.getAuth(authToken);

        if (authData == null) {
            throw new ServiceException(401, "Error: unauthorized");
        }

        dataAccess.deleteAuth(authToken);

    }

    public void clear() {
        dataAccess.clear();
    }
}
