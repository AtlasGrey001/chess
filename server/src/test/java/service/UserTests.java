package service;

import dataaccess.DataAccess;
import dataaccess.MySQLDataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserTests {
    private DataAccess dao;

    @BeforeEach
    void setup() throws Exception {
        dao = new MySQLDataAccess();
        dao.clear();
    }

    @Test
    void createUserPositive() throws Exception {
        dao.createUser(new UserData("cam", "p", "e"));
        assertNotNull(dao.getUser("cam"));
    }

    @Test
    void createUserNegativeDuplicate() throws Exception {
        dao.createUser(new UserData("cam", "p", "e"));
        assertThrows(dataaccess.DataAccessException.class,
                () -> dao.createUser(new UserData("cam", "p", "e")));
    }

    @Test
    void createAuthPositive() throws Exception {
        dao.createUser(new UserData("cam", "p", "e"));   // REQUIRED
        dao.createAuth(new AuthData("t1", "cam"));

        assertNotNull(dao.getAuth("t1"));
    }

    @Test
    void createAuthNegativeDuplicate() throws Exception {
        dao.createUser(new UserData("cam", "p", "e"));   // REQUIRED
        dao.createAuth(new AuthData("t1", "cam"));

        assertThrows(dataaccess.DataAccessException.class,
                () -> dao.createAuth(new AuthData("t1", "cam")));
    }

    @Test
    void getAuthPositive() throws Exception {
        dao.createUser(new UserData("cam", "p", "e"));   // REQUIRED
        dao.createAuth(new AuthData("t1", "cam"));

        assertEquals("cam", dao.getAuth("t1").username());
    }

    @Test
    void getAuthNegativeNotFound() throws Exception {
        assertNull(dao.getAuth("nope"));
    }

    @Test
    void deleteAuthPositive() throws Exception {
        dao.createUser(new UserData("cam", "p", "e"));   // REQUIRED
        dao.createAuth(new AuthData("t1", "cam"));

        dao.deleteAuth("t1");
        assertNull(dao.getAuth("t1"));
    }

    @Test
    void deleteAuthNegativeNotFound() throws Exception {
        dao.deleteAuth("missing"); // should not throw
    }

    @Test
    void clearPositive() throws Exception {
        dao.createUser(new UserData("cam", "p", "e"));   // REQUIRED
        dao.createAuth(new AuthData("t1", "cam"));

        dao.clear();

        assertNull(dao.getUser("cam"));
        assertNull(dao.getAuth("t1"));
    }

    @Test
    void clearNegativeDoubleClear() throws Exception {
        dao.clear();
        dao.clear(); // should not throw
    }
}
