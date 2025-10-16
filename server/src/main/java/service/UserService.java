package service;

import dataAccess.DataAccess;
import dataAccess.MemoryDataAccess;
import dataAccess.DataAccessException;
import datamodel.RegistrationResult;
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
        String authToken = generateToken();
        return new RegistrationResult(user.username(), authToken);
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
