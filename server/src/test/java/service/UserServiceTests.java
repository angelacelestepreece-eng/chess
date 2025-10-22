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
    void register_validUser_returnsAuthData() throws ServiceException {
        UserData user = new UserData("kiley", "password", "email");
        var result = service.register(user);

        assertEquals("kiley", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    void register_duplicateUsername_throwsAlreadyTaken() throws ServiceException {
        UserData user = new UserData("natalie", "password", "email");
        service.register(user);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.register(user));
        assertEquals("Error: already taken", ex.getMessage());
    }

    @Test
    void login_valid_returnsAuthData() throws ServiceException {
        UserData user = new UserData("sarah", "password", "email");
        service.register(user);

        var result = service.login(new UserData("sarah", "password", null));
        assertEquals("sarah", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    void login_withWrongPassword_throwsUnauthorized() throws ServiceException {
        UserData user = new UserData("molly", "password", "email");
        service.register(user);

        UserData invalidLogin = new UserData("molly", "wrongpassword", null);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.login(invalidLogin));
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void logout_valid_deletesAuth() throws ServiceException {
        UserData user = new UserData("jayson", "password", "email");
        var result = service.register(user);
        service.logout(result.authToken());
        assertNull(dao.getAuth(result.authToken()));
    }

    @Test
    void logout_invalidToken_throwsUnauthorized() {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.logout("invalid-token"));
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void clear_allData() throws ServiceException {
        UserData user = new UserData("jennifer", "password", "email");
        var result = service.register(user);
        service.clear();
        assertNull(dao.getUser("jennifer"));
        assertNull(dao.getAuth(result.authToken()));
    }
}
