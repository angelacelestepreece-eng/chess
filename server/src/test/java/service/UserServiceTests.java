package service;

import dataaccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {
    private UserService service;
    private MemoryDataAccess dao;

    @BeforeEach
    void setup() {
        dao = new MemoryDataAccess();
        service = new UserService(dao);
    }

    @Test
    void registerValidUser() throws ServiceException {
        UserData user = new UserData("kiley", "password", "email");
        var result = service.register(user);

        assertEquals("kiley", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    void registerDuplicateUsername() throws ServiceException {
        UserData user = new UserData("natalie", "password", "email");
        service.register(user);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.register(user));
        assertEquals("Error: already taken", ex.getMessage());
    }

    @Test
    void loginValid() throws ServiceException {
        UserData user = new UserData("sarah", "password", "email");
        service.register(user);

        var result = service.login(new UserData("sarah", "password", null));
        assertEquals("sarah", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    void loginWithWrongPassword() throws ServiceException {
        UserData user = new UserData("molly", "password", "email");
        service.register(user);

        UserData invalidLogin = new UserData("molly", "wrongpassword", null);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.login(invalidLogin));
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void logoutValid() throws ServiceException {
        UserData user = new UserData("jayson", "password", "email");
        var result = service.register(user);
        service.logout(result.authToken());
        assertNull(dao.getAuth(result.authToken()));
    }

    @Test
    void logoutInvalidToken() {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.logout("invalid-token"));
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void clearAllData() throws Exception {
        UserData user = new UserData("jennifer", "password", "email");
        var result = service.register(user);
        service.clear();
        assertNull(dao.getUser("jennifer"));
        assertNull(dao.getAuth(result.authToken()));
    }
}
