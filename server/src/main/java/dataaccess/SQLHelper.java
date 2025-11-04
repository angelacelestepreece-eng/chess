package dataaccess;

import java.sql.*;

import static java.sql.Types.NULL;

public class SQLHelper {

    private static void params(PreparedStatement preparedStatement, Object... params) throws SQLException {
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
    }

    public static int executeUpdate(String statement, Object... params) throws ResponseException {
        try (Connection conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)) {
            params(preparedStatement, params);
            return preparedStatement.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(ResponseException.Code.ServerError,
                    String.format("Unable to update game table: %s, %s", statement, e.getMessage()));
        }


    }

    public static int executeInsert(String statement, Object... params) throws ResponseException {
        try (Connection conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
            params(preparedStatement, params);
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
}
