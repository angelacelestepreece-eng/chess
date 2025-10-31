package dataaccess;

import com.google.gson.Gson;
import dataaccess.ResponseException;
import model.AuthData;
import model.UserData;

import java.sql.*;
import java.util.UUID;

import static java.sql.Types.NULL;

public class MySQLAuthDAO implements AuthDAO {

    public MySQLAuthDAO() throws ResponseException {
        configureAuthTable();
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public AuthData createAuth(UserData user) throws ResponseException {
        String authToken = generateToken();
        var statement = "INSERT INTO auth (authToken, username, json) VALUES (?, ?, ?)";
        String json = new Gson().toJson(new AuthData(user.username(), authToken));
        executeUpdate(statement, authToken, user.username(), json);
        return new AuthData(user.username(), authToken);
    }

    @Override
    public AuthData getAuth(String authToken) throws ResponseException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT json FROM auth WHERE authToken=?";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, authToken);
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        return new Gson().fromJson(rs.getString("json"), AuthData.class);
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(ResponseException.Code.ServerError,
                    String.format("Unable to read auth: %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws ResponseException {
        var statement = "DELETE FROM auth WHERE authToken=?";
        executeUpdate(statement, authToken);
    }

    @Override
    public void clear() throws ResponseException {
        var statement = "TRUNICATE auth";
        executeUpdate(statement);
    }

    private int executeUpdate(String statement, Object... params) throws ResponseException {
        try (Connection conn = DatabaseManager.getConnection();
             var prepparedStatement = conn.prepareStatement(statement)) {
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param instanceof String p) {
                    prepparedStatement.setString(i + 1, p);
                } else if (param instanceof Integer p) {
                    prepparedStatement.setInt(i + 1, p);
                } else if (param == null) {
                    prepparedStatement.setNull(i + 1, NULL);
                }
            }
            return prepparedStatement.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(ResponseException.Code.ServerError,
                    String.format("Unable to Update auth table: %s, %s", statement, e.getMessage()));
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS auth (
                authToken VARCHAR(100) NOT NULL PRIMARY KEY,
                username VARCHAR(100) NOT NULL,
                json TEXT DEFAULT NULL,
                INDEX(username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci    
            """
    };

    private void configureAuthTable() throws ResponseException {
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(ResponseException.Code.ServerError,
                    String.format("Unable to configure auth table: %s", e.getMessage()));
        }
    }

}
