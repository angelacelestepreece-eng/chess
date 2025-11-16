package client;

import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;
import exception.ResponseException;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabase() throws Exception {
        facade.clear();
    }

    @Test
    void registerPositive() throws Exception {
        var result = facade.register("player1", "password", "p1@email.com");
        assertNotNull(result.authToken());
        assertTrue(result.authToken().length() > 10);
    }

    @Test
    void registerNegativeDuplicateUser() throws Exception {
        facade.register("player1", "password", "p1@email.com");
        assertThrows(ResponseException.class, () ->
                facade.register("player1", "password", "p1@email.com")
        );
    }

    @Test
    void loginPositive() throws Exception {
        facade.register("player2", "password", "p2@email.com");
        var result = facade.login("player2", "password");
        assertNotNull(result.authToken());
    }

    @Test
    void loginNegativeWrongPassword() throws Exception {
        facade.register("player3", "password", "p3@email.com");
        assertThrows(ResponseException.class, () ->
                facade.login("player3", "wrongpassword")
        );
    }

    @Test
    void loginNegativeNoSuchUser() {
        assertThrows(ResponseException.class, () ->
                facade.login("nosuchuser", "password")
        );
    }

    @Test
    void logoutPositive() throws Exception {
        facade.register("player4", "password", "p4@email.com");
        facade.logout();
    }

    @Test
    void logoutNegativeBadAuth() {
        assertThrows(ResponseException.class, () ->
                facade.logout()
        );
    }

    @Test
    void createGamePositive() throws Exception {
        facade.register("player5", "password", "p5@email.com");
        var result = facade.createGame("MyGame");
        assertTrue(result.gameID() > 0);
    }

    @Test
    void createGameNegativeNoAuth() {
        assertThrows(ResponseException.class, () ->
                facade.createGame("GameWithoutAuth")
        );
    }

    @Test
    void listGamesPositive() throws Exception {
        facade.register("player6", "password", "p6@email.com");
        facade.createGame("Game1");
        var result = facade.listGames();
        assertFalse(result.games().isEmpty());
    }

    @Test
    void listGamesNegativeNoAuth() {
        assertThrows(ResponseException.class, () ->
                facade.listGames()
        );
    }

    @Test
    void joinGamePositive() throws Exception {
        facade.register("player7", "password", "p7@email.com");
        var game = facade.createGame("Game2");
        facade.joinGame(game.gameID(), "WHITE");
    }

    @Test
    void joinGameNegativeBadColor() throws Exception {
        facade.register("player8", "password", "p8@email.com");
        var game = facade.createGame("Game3");
        assertThrows(ResponseException.class, () ->
                facade.joinGame(game.gameID(), "PURPLE")
        );
    }

    @Test
    void joinGameNegativeNoAuth() throws Exception {
        var auth = facade.register("playerX", "password", "px@email.com");
        var game = facade.createGame("GameX");
        facade.logout();
        assertThrows(ResponseException.class, () ->
                facade.joinGame(game.gameID(), "WHITE")
        );
    }

}
