import dataAccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.ServiceException;
import service.UserService;

public class ServiceTest {

    @Test
    public void registerNormal() throws ServiceException {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService();

        var res = userService.register(new UserData("dog", "cat", "fish"));
        Assertions.assertNotNull(res);
    }

    @Test
    public void clearNormal() throws ServiceException {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService();

        var res = userService.register(new UserData("dog", "cat", "fish"));
        Assertions.assertNotNull(res);
    }
}
