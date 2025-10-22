package service;

import dataaccess.DataAccess;
import datamodel.LoginResult;
import datamodel.RegistrationResult;
import model.AuthData;
import model.UserData;


public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

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

    public LoginResult login(UserData user) throws ServiceException {
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
        AuthData auth = dataAccess.createAuth(user);
        return new LoginResult(auth.username(), auth.authToken());
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
