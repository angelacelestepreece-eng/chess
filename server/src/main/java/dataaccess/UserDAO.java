package dataaccess;

import exception.ResponseException;

import model.UserData;

public interface UserDAO {
    void createUser(UserData user) throws ResponseException;

    UserData getUser(String username) throws ResponseException;

    void clear() throws ResponseException;
}
