package dataaccess;

import chess.*;
import dataaccess.MemoryDataAccess.MemoryDataAccess;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DataAccessTests {
    private UserService service;
    private MemoryDataAccess dao;

    @BeforeEach
    void setup() {
        dao = new MemoryDataAccess();
        service = new UserService(dao);
    }

    @Test
    void createUserSuccess() throws Exception {
        UserData user = new UserData("angela", "pass", "email");
        dao.createUser(user);
        assertEquals("angela", dao.getUser("angela").username());
    }

    @Test
    void createUserDuplicate() throws Exception {
        UserData user = new UserData("sarah", "pass", "email");
        dao.createUser(user);
        assertThrows(ResponseException.class, () -> dao.createUser(user));
    }

    @Test
    void getUserSuccess() throws Exception {
        UserData user = new UserData("natalie", "pass", "email");
        dao.createUser(user);
        assertNotNull(dao.getUser("natalie"));
    }

    @Test
    void notFoundUser() throws Exception {
        assertNull(dao.getUser("none"));
    }

    @Test
    void createAuthSuccess() throws Exception {
        UserData user = new UserData("tori", "pass", "email");
        dao.createUser(user);
        var auth = dao.createAuth(user);
        assertEquals("tori", dao.getAuth(auth.authToken()).username());
    }

    @Test
    void getInvalidAuthToken() throws Exception {
        assertNull(dao.getAuth("invalidToken"));
    }

    @Test
    void createGameSuccess() throws Exception {
        GameData game = dao.createGame("AngelaGame");
        assertEquals("AngelaGame", dao.getGame(game.gameID()).gameName());
    }

    @Test
    void createGameDuplicate() throws Exception {
        var game = dao.createGame("FunGame");
        var duplicate = new model.GameData(game.gameID(), null, null, "FunGame", new chess.ChessGame());
        assertThrows(ResponseException.class, () -> dao.saveGame(duplicate));
    }

    @Test
    void getGameSuccess() throws Exception {
        var game = dao.createGame("CoolGame");
        assertNotNull(dao.getGame(game.gameID()));
    }

    @Test
    void getEmptyGames() throws Exception {
        assertTrue(dao.getGames().isEmpty());
    }

    @Test
    void getGameBadID() throws Exception {
        assertNull(dao.getGame(124));
    }

    @Test
    void updatesGame() throws Exception {
        var original = dao.createGame("NiceGame");
        var board = original.game();
        board.setTeamTurn(ChessGame.TeamColor.WHITE);
        var move = new ChessMove(
                new ChessPosition(2, 5),
                new ChessPosition(4, 5),
                null
        );
        board.makeMove(move);
        var updated = new GameData(original.gameID(), "white", "black", "NiceGame", board);
        dao.updateGame(updated);
        var result = dao.getGame(original.gameID());
        assertEquals(
                board.getBoard().getPiece(new ChessPosition(4, 5)),
                result.game().getBoard().getPiece(new ChessPosition(4, 5))
        );
    }

    @Test
    void updateNonexistentGame() {
        var nonexistent = new GameData(456, "whitePlayer", "blackPlayer", "InvalidGame", new ChessGame());
        assertThrows(ResponseException.class, () -> dao.updateGame(nonexistent));
    }

    @Test
    void savesGame() throws Exception {
        var game = new ChessGame();
        var gameData = new GameData(123, "whitePlayer", "blackPlayer", "FunGame", new ChessGame());
        dao.saveGame(gameData);
        var result = dao.getGame(123);
        assertNotNull(result);
        assertEquals("FunGame", result.gameName());
        assertEquals("whitePlayer", result.whiteUsername());
        assertEquals("blackPlayer", result.blackUsername());
        assertEquals(game.getBoard(), result.game().getBoard());
    }

    @Test
    void saveDuplicateGame() throws Exception {
        var gameData = new GameData(456, "whitePlayer", "blackPlayer", "First", new ChessGame());
        dao.saveGame(gameData);
        var duplicateGameData = new GameData(456, "whitePlayer", "blackPlayer", "Second", new ChessGame());
        assertThrows(ResponseException.class, () -> dao.saveGame(duplicateGameData));
    }


    @Test
    void getsGames() throws Exception {
        dao.createGame("AwesomeGame");
        dao.createGame("AmazingGame");
        assertTrue(dao.getGames().size() >= 2);
    }

    @Test
    void deleteAuthSuccess() throws Exception {
        UserData user = new UserData("janie", "password", "email");
        dao.createUser(user);
        var auth = dao.createAuth(user);
        dao.deleteAuth(auth.authToken());
        assertNull(dao.getAuth(auth.authToken()));
    }

    @Test
    void deleteInvalidAuthToken() {
        assertThrows(ResponseException.class, () -> dao.deleteAuth("invalidToken"));
    }

    @Test
    void clearSuccess() throws Exception {
        dao.createUser(new UserData("angela", "password", "email"));
        dao.clear();
        assertNull(dao.getUser("angela"));
        assertTrue(dao.getGames().isEmpty());
    }
}
