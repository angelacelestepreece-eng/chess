package service;

import dataaccess.DataAccess;
import dataaccess.ResponseException;
import datamodel.LoginResult;
import datamodel.RegistrationResult;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.awt.image.BandCombineOp;
import java.security.Provider;


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

        try {
            if (dataAccess.getUser(user.username()) != null) {
                throw new ServiceException(403, "Error: already taken");
            }

            dataAccess.createUser(user);
            AuthData auth = dataAccess.createAuth(user);
            return new RegistrationResult(auth.username(), auth.authToken());
        } catch (ResponseException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Internal Server Error";
            throw new ServiceException(500, "Error: " + msg);
        }
    }

    public LoginResult login(UserData user) throws ServiceException {
        if (user.username() == null || user.password() == null ||
                user.username().isBlank() || user.password().isBlank()) {
            throw new ServiceException(400, "Error: bad request");
        }

        try {
            UserData registeredUser = dataAccess.getUser(user.username());

            if (registeredUser == null ||
                    !BCrypt.checkpw(user.password(), registeredUser.password())) {
                throw new ServiceException(401, "Error: unauthorized");
            }

            AuthData auth = dataAccess.createAuth(registeredUser);
            return new LoginResult(auth.username(), auth.authToken());
        } catch (ResponseException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Internal Server Error";
            throw new ServiceException(500, "Error: " + msg);
        }
    }

    public void logout(String authToken) throws ServiceException {
        try {
            AuthData authData = dataAccess.getAuth(authToken);

            if (authData == null) {
                throw new ServiceException(401, "Error: unauthorized");
            }

            dataAccess.deleteAuth(authToken);
        } catch (ResponseException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Internal Server Error";
            throw new ServiceException(500, "Error: " + msg);
        }
    }

    public void clear() throws ServiceException {
        try {
            dataAccess.clear();
        } catch (ResponseException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Internal Server Error";
            throw new ServiceException(500, "Error: " + msg);
        }
    }
}
