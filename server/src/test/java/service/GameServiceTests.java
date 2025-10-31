package service;

import dataaccess.MemoryDataAccess;
import dataaccess.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTests {
    private GameService service;
    private MemoryDataAccess dao;
    private String validToken;
    private int gameID;

    @BeforeEach
    void setup() throws Exception {
        dao = new MemoryDataAccess();
        service = new GameService(dao);

        UserData user = new UserData("john", "password", "email");
        dao.createUser(user);
        AuthData auth = dao.createAuth(user);

        validToken = auth.authToken();

        GameData game = dao.createGame("TestGame");
        gameID = game.gameID();
    }

    @Test
    void listGamesValidToken() throws Exception {
        var result = service.listGames(validToken);
        assertNotNull(result.games());
        assertFalse(result.games().isEmpty());
    }

    @Test
    void listGamesInvalidToken() {
        assertThrows(ServiceException.class, () -> service.listGames("invalid-token"));
    }

    @Test
    void createGameValidInput() throws Exception {
        var result = service.createGame("New Game", validToken);
        assertTrue(result.gameID() > 0);
    }

    @Test
    void createGameBlankName() {
        assertThrows(ServiceException.class, () -> service.createGame(" ", validToken));
    }

    @Test
    void joinGameValidWhiteJoin() throws Exception {
        service.joinGame(validToken, "WHITE", gameID);
        var updated = dao.getGame(gameID);
        assertEquals("john", updated.whiteUsername());
    }

    @Test
    void joinGameAlreadyTakenColor() throws Exception {
        service.joinGame(validToken, "white", gameID);
        UserData otherUser = new UserData("will", "pass", "email");
        dao.createUser(otherUser);
        AuthData otherAuth = dao.createAuth(otherUser);

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.joinGame(otherAuth.authToken(), "WHITE", gameID));
        assertEquals("Error: already taken", ex.getMessage());
    }

    @Test
    void clearAllData() throws Exception {
        service.clear();
        assertTrue(dao.getGames().isEmpty());
        assertNull(dao.getAuth(validToken));
    }
}
