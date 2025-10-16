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
        dataAccess.createAuth(user);
        AuthData auth = dataAccess.getAuth(user.username());
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
        dataAccess.createAuth(user);
        return dataAccess.getAuth(user.username());
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
