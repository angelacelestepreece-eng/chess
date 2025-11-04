package service;

import dataaccess.DataAccess;
import dataaccess.ResponseException;
import model.AuthData;

public class ServiceHelper {

    public static AuthData validateAuth(DataAccess dataAccess, String authToken) throws ServiceException {
        if (authToken == null || authToken.isBlank()) {
            throw new ServiceException(401, "Error: unauthorized");
        }

        try {
            AuthData authData = dataAccess.getAuth(authToken);
            if (authData == null) {
                throw new ServiceException(401, "Error: unauthorized");
            }
            return authData;
        } catch (ResponseException e) {
            throw wrap(e);
        }
    }

    public static void validateNotBlank(String value, int code, String message) throws ServiceException {
        if (value == null || value.isBlank()) {
            throw new ServiceException(code, message);
        }
    }

    public static ServiceException wrap(ResponseException e) {
        String message = e.getMessage() != null ? e.getMessage() : "Server Error";
        return new ServiceException(500, "Error: " + message);
    }
}
