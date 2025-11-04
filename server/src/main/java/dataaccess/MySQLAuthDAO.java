package dataaccess;

import model.AuthData;
import model.UserData;

import java.sql.*;
import java.util.UUID;

import static dataaccess.SQLHelper.executeUpdate;
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
        var statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        executeUpdate(statement, authToken, user.username());
        return new AuthData(user.username(), authToken);
    }

    @Override
    public AuthData getAuth(String authToken) throws ResponseException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username FROM auth WHERE authToken=?";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, authToken);
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        String username = rs.getString("username");
                        return new AuthData(username, authToken);
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
        int updated = executeUpdate(statement, authToken);
        if (updated == 0) {
            throw new ResponseException(ResponseException.Code.BadRequest, "Auth token not found");
        }
    }

    @Override
    public void clear() throws ResponseException {
        var statement = "TRUNCATE auth";
        executeUpdate(statement);
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS auth (
                authToken VARCHAR(100) NOT NULL PRIMARY KEY,
                username VARCHAR(100) NOT NULL,
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
