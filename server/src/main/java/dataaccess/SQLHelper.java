package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;

import static java.sql.Types.NULL;

public class SQLHelper {
    public static int executeUpdate(String statement, Object... params) throws ResponseException {
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
}
