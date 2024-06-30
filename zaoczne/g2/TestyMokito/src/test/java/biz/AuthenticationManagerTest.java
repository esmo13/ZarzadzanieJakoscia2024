package biz;

import db.dao.DAO;
import model.Password;
import model.User;
import model.exceptions.UserUnnkownOrBadPasswordException;
import model.operations.PaymentIn;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationManagerTest {

    AuthenticationManager target;
    @Mock
    DAO mockDao;
    @Mock
    BankHistory mockHistory;

    @BeforeEach
    void setUp() {
        target = new AuthenticationManager(mockDao, mockHistory);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testConstruction() throws NoSuchFieldException, IllegalAccessException {
        //GIVEN
        Field daoField = AuthenticationManager.class.getDeclaredField("dao");
        daoField.setAccessible(true);
        Field historyField = AuthenticationManager.class.getDeclaredField("history");
        historyField.setAccessible(true);

        //WHEN
        //zrealizowane w metodzie setUp()
        //THEN

        assertEquals(mockDao, daoField.get(target));
        assertEquals(mockHistory, historyField.get(target));
    }

    @Test
    void logIn() throws UserUnnkownOrBadPasswordException, SQLException {
        //GIVEN
        int userId = 13;
        User user = mock(User.class);
        Password passwd = new Password();
        passwd.setUserId(userId);
        String login = "admin";
        char[] password = {'a','d','m','i','n'};
        //konieczna kopia bo metoda hashPassword "zeruje" oryginalny array.
        char[] passwordCopy = Arrays.copyOf(password, password.length);
        passwd.setPasswd(target.hashPassword(password));

        when(mockDao.findUserByName(login)).thenReturn(user);
        when(mockDao.findPasswordForUser(user)).thenReturn(passwd);
        //WHEN
        target.logIn(login,passwordCopy);
        //THEN
        verify(mockHistory, times(1)).logLoginSuccess(user);

    }

    @Test
    void logInWrongPassword() throws UserUnnkownOrBadPasswordException, SQLException {
        //GIVEN
        int userId = 13;
        User user = mock(User.class);
        Password passwd = new Password();
        passwd.setUserId(userId);
        String login = "admin";
        char[] password = {'a','d','m','i','n'};
        char[] wrong_password = {'a'};
        passwd.setPasswd(target.hashPassword(password));

        when(mockDao.findUserByName(login)).thenReturn(user);
        when(mockDao.findPasswordForUser(user)).thenReturn(passwd);
        //WHEN
        UserUnnkownOrBadPasswordException thrown = assertThrows(UserUnnkownOrBadPasswordException.class, () -> target.logIn(login,wrong_password));

        //THEN
        assertEquals(thrown.getMessage(),"Bad Password");
        verify(mockHistory, times(1)).logLoginFailure(user,"Bad Password");

    }

    @Test
    void logInUserNull() throws UserUnnkownOrBadPasswordException, SQLException {
        //GIVEN

        String login = "admin";
        char[] password = {'a','d','m','i','n'};

        when(mockDao.findUserByName(login)).thenReturn(null);
        //WHEN
        UserUnnkownOrBadPasswordException thrown = assertThrows(UserUnnkownOrBadPasswordException.class, () -> target.logIn(login,password));

        //THEN
        //tu znaleziono błąd 11, mimo że nie znaleziono użytkownika, wyjątek ma wiadomość "Bad Password"
        assertEquals(thrown.getMessage(),"Zła nazwa użytkownika "+login);
        verify(mockHistory, times(1)).logLoginFailure(null,"Zła nazwa użytkownika "+login);

    }
}