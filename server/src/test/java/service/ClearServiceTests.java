package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTests {
    private DataAccess dao;
    private UserService userService;
    private ClearService clearService;

    @BeforeEach
    void setup() {
        dao=new MemoryDataAccess();
        userService=new UserService(dao);
        clearService=new ClearService(dao);
    }

    @Test
    void clearPositive() throws Exception {
        userService.register(new RegisterRequest("cameron","pass","e"));
        clearService.clear();

        assertNull(dao.getUser("cameron"));
        assertTrue(dao.listGames().isEmpty());
    }
}
