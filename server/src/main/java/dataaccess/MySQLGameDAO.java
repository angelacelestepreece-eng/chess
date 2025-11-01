package dataaccess;

import com.google.gson.Gson;
import dataaccess.ResponseException;
import model.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

import static java.sql.Types.NULL;

public class MySQLGameDAO implements GameDAO {

    public MySQLGameDAO() throws ResponseException {
        configureGameTable();
    }

    @Override
    public GameData createGame(String gameName) throws ResponseException {
        var insertStatement = "INSERT INTO game (gameName, json) VALUES (?, ?)";
        int gameID = executeInsert(insertStatement, gameName, "{}");
        GameData finalGame = new GameData(gameID, null, null, gameName, null);
        String updatedJson = new Gson().toJson(finalGame);
        var updateStatement = "UPDATE game SET json=? WHERE gameID=?";
        int rows = executeUpdate(updateStatement, updatedJson, gameID);
        if (rows == 0) {
            throw new ResponseException(ResponseException.Code.ServerError, "Failed to update game JSON");
        }
        return finalGame;
    }


    @Override
    public GameData getGame(int gameID) throws ResponseException {
        String statement = "SELECT json FROM game WHERE gameID=?";
        try (Connection conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.setInt(1, gameID);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return new Gson().fromJson(rs.getString("json"), GameData.class);
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(ResponseException.Code.ServerError,
                    String.format("Unable to read game: %s", e.getMessage()));
        }
        return null;
    }


    @Override
    public Collection<GameData> getGames() throws ResponseException {
        var statement = "SELECT json FROM game";
        var result = new ArrayList<GameData>();
        try (Connection conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement);
             ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                GameData game = new Gson().fromJson(rs.getString("json"), GameData.class);
                result.add(game);
            }
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(ResponseException.Code.ServerError,
                    String.format("Unable to list games: %s", e.getMessage()));
        }
        return result;
    }

    @Override
    public void saveGame(GameData game) throws ResponseException {
        String json = new Gson().toJson(game);
        var statement = "UPDATE game SET json=? WHERE gameID=?";
        executeUpdate(statement, json, game.gameID());
    }

    @Override
    public void updateGame(GameData game) throws ResponseException {
        String json = new Gson().toJson(game);
        var statement = "UPDATE game SET json=? WHERE gameID=?";
        executeUpdate(statement, json, game.gameID());
    }

    @Override
    public void clear() throws ResponseException {
        var statement = "TRUNCATE game";
        executeUpdate(statement);
    }


    private int executeInsert(String statement, Object... params) throws ResponseException {
        try (Connection conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param instanceof String p) {
                    preparedStatement.setString(i + 1, p);
                } else if (param instanceof Integer p) {
                    preparedStatement.setInt(i + 1, p);
                } else if (param == null) {
                    preparedStatement.setNull(i + 1, NULL);
                }
            }
            preparedStatement.executeUpdate();
            try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            throw new ResponseException(ResponseException.Code.ServerError, "No gameID returned from insert");
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(ResponseException.Code.ServerError,
                    String.format("Unable to insert into game table: %s, %s", statement, e.getMessage()));
        }
    }


    private int executeUpdate(String statement, Object... params) throws ResponseException {
        try (Connection conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)) {
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param instanceof String p) {
                    preparedStatement.setString(i + 1, p);
                } else if (param instanceof Integer p) {
                    preparedStatement.setInt(i + 1, p);
                } else if (param == null) {
                    preparedStatement.setNull(i + 1, NULL);
                }
            }
            return preparedStatement.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(ResponseException.Code.ServerError,
                    String.format("Unable to update game table: %s, %s", statement, e.getMessage()));
        }
    }


    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS game (
                gameID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                gameName VARCHAR(45),
                json LONGTEXT
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private void configureGameTable() throws ResponseException {
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(ResponseException.Code.ServerError,
                    String.format("Unable to configure game table: %s", e.getMessage()));
        }
    }

}
