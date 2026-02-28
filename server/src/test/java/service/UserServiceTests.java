package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {

    private DataAccess dao;
    private UserService userService;

    @BeforeEach
    void setup() {
        dao=new MemoryDataAccess();
        userService=new UserService(dao);
    }

    @Test
    void registerPositive() throws Exception {
        var req=new RegisterRequest("cameron", "pass", "email@x.com");
        var res=userService.register(req);

        assertEquals("cameron",res.username());
        assertNotNull(res.authToken());
        assertNotNull(dao.getUser("cameron"));
        assertNotNull(dao.getAuth(res.authToken()));
    }

    @Test
    void registerNegative_usernameTaken() throws Exception {
        var req=new RegisterRequest("cameron","pass","email@x.com");
        userService.register(req);

        assertThrows(DataAccessException.class, () -> userService.register(req));
    }

    @Test
    void loginPositive() throws Exception {
        userService.register(new RegisterRequest("cameron","pass","e"));

        var res=userService.login(new LoginRequest("cameron","pass"));

        assertEquals("cameron",res.username());
        assertNotNull(res.authToken());
    }

    @Test
    void loginNegative_badPassword() throws Exception {
        userService.register(new RegisterRequest("cameron","pass","e"));

        assertThrows(UnauthorizedException.class, () -> userService.login(new LoginRequest("cameron", "wrong")));
    }

    @Test
    void logoutPositive() throws Exception {
        var reg=userService.register(new RegisterRequest("cameron","pass","e"));
        userService.logout(new LogoutRequest(reg.authToken()));
        assertNull(dao.getAuth(reg.authToken()));
    }

    @Test void logoutNegative_invalidToken() {
        assertThrows(UnauthorizedException.class,() -> userService.logout(new LogoutRequest("fake-token")) );
    }
}
