package service;

import dataaccess.DataAccess;
import datamodel.LoginResult;
import datamodel.RegistrationResult;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import exception.ResponseException;

import static service.ServiceHelper.*;


public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public RegistrationResult register(UserData user) throws ServiceException {
        validateNotBlank(user.username(), 400, "Error: bad request");
        validateNotBlank(user.password(), 400, "Error: bad request");
        validateNotBlank(user.email(), 400, "Error: bad request");

        try {
            if (dataAccess.getUser(user.username()) != null) {
                throw new ServiceException(403, "Error: already taken");
            }

            dataAccess.createUser(user);
            AuthData auth = dataAccess.createAuth(user);
            return new RegistrationResult(auth.username(), auth.authToken());
        } catch (ResponseException e) {
            throw wrap(e);
        }
    }

    public LoginResult login(UserData user) throws ServiceException {
        validateNotBlank(user.username(), 400, "Error: bad request");
        validateNotBlank(user.password(), 400, "Error: bad request");

        try {
            UserData registeredUser = dataAccess.getUser(user.username());

            if (registeredUser == null ||
                    !BCrypt.checkpw(user.password(), registeredUser.password())) {
                throw new ServiceException(401, "Error: unauthorized");
            }

            AuthData auth = dataAccess.createAuth(registeredUser);
            return new LoginResult(auth.username(), auth.authToken());
        } catch (ResponseException e) {
            throw wrap(e);
        }
    }

    public void logout(String authToken) throws ServiceException {
        validateAuth(dataAccess, authToken);
        try {
            dataAccess.deleteAuth(authToken);
        } catch (ResponseException e) {
            throw wrap(e);
        }
    }

    public void clear() throws ServiceException {
        try {
            dataAccess.clear();
        } catch (ResponseException e) {
            throw wrap(e);
        }
    }
}
