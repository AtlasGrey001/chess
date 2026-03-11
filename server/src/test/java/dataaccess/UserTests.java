package dataaccess;

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
    void getUserPositive() throws Exception {
        dao.createUser(new UserData("cam", "p", "e"));

        UserData user = dao.getUser("cam");

        assertNotNull(user);
        assertEquals("cam", user.username());
    }

    @Test
    void getUserNegativeNotFound() throws Exception {
        assertNull(dao.getUser("missing"));
    }

    @Test
    void createUserNegativeDuplicate() throws Exception {
        dao.createUser(new UserData("cam", "p", "e"));
        assertThrows(dataaccess.DataAccessException.class,
                () -> dao.createUser(new UserData("cam", "p", "e")));
    }

    @Test
    void createUserNegativeNullUsername() {
        assertThrows(DataAccessException.class,
                () -> dao.createUser(new UserData(null, "p", "e")));
    }


    @Test
    void createAuthPositive() throws Exception {
        dao.createUser(new UserData("cam", "p", "e"));
        dao.createAuth(new AuthData("t1", "cam"));

        assertNotNull(dao.getAuth("t1"));
    }

    @Test
    void createAuthNegativeDuplicate() throws Exception {
        dao.createUser(new UserData("cam", "p", "e"));
        dao.createAuth(new AuthData("t1", "cam"));

        assertThrows(dataaccess.DataAccessException.class,
                () -> dao.createAuth(new AuthData("t1", "cam")));
    }

    @Test
    void createAuthNegativeUserDoesNotExist() {
        assertThrows(DataAccessException.class,
                () -> dao.createAuth(new AuthData("t1", "ghost")));
    }


    @Test
    void getAuthPositive() throws Exception {
        dao.createUser(new UserData("cam", "p", "e"));
        dao.createAuth(new AuthData("t1", "cam"));

        assertEquals("cam", dao.getAuth("t1").username());
    }

    @Test
    void getAuthNegativeNotFound() throws Exception {
        assertNull(dao.getAuth("nope"));
    }

    @Test
    void getAuthPositiveTokenMatches() throws Exception {
        dao.createUser(new UserData("cam", "p", "e"));
        dao.createAuth(new AuthData("t1", "cam"));

        AuthData auth = dao.getAuth("t1");

        assertEquals("t1", auth.authToken());
    }


    @Test
    void deleteAuthPositive() throws Exception {
        dao.createUser(new UserData("cam", "p", "e"));
        dao.createAuth(new AuthData("t1", "cam"));

        dao.deleteAuth("t1");
        assertNull(dao.getAuth("t1"));
    }

    @Test
    void deleteAuthNegativeNotFound() throws Exception {
        dao.deleteAuth("missing");
    }

    @Test
    void deleteAuthNegativeNullToken() throws Exception {
        dao.deleteAuth(null);
    }

    @Test
    void clearPositive() throws Exception {
        dao.createUser(new UserData("cam", "p", "e"));
        dao.createAuth(new AuthData("t1", "cam"));

        dao.clear();

        assertNull(dao.getUser("cam"));
        assertNull(dao.getAuth("t1"));
    }

    @Test
    void clearPositiveAllTables() throws Exception {
        dao.createUser(new UserData("cam", "p", "e"));
        dao.createAuth(new AuthData("t1", "cam"));
        dao.createGame("A");
        dao.clear();

        assertNull(dao.getUser("cam"));
        assertNull(dao.getAuth("t1"));
        assertTrue(dao.listGames().isEmpty());
    }


    @Test
    void clearNegativeDoubleClear() throws Exception {
        dao.clear();
        dao.clear();
    }
}
