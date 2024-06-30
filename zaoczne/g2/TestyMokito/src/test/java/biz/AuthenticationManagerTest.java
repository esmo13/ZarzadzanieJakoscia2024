package biz;

import db.dao.DAO;
import model.*;
import model.exceptions.UserUnnkownOrBadPasswordException;
import model.operations.OperationType;
import model.operations.PaymentIn;
import model.operations.Withdraw;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
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
        target.digestAlgo="SHA-256";
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

    @Test
    void logOutOk() throws SQLException {
        //GIVEN
        User user = mock(User.class);

        //WHEN
        boolean result = target.logOut(user);

        //THEN
        verify(mockHistory, times(1)).logLogOut(user);
        assertTrue(result);

    }
    @Test
    void logOutThrows() throws SQLException {
        //GIVEN
        User user = mock(User.class);
        doThrow(new SQLException("")).when(mockHistory).logLogOut(user);

        //WHEN
        assertThrows(SQLException.class,()->target.logOut(user));

        //THEN
        verify(mockHistory, times(1)).logLogOut(user);

    }

    @Test
    void hashPasswordNoSuchAlgo()  {
        //GIVEN
        char[] password = {'a','d','m','i','n'};
        //tu znaleziono nie błąd 12, zhardcodowany algorytm
        target.digestAlgo="arbitraryNotExistentAlgo";
        //WHEN
        String result = target.hashPassword(password);

        //THEN
        assertNull(result);

    }
    @Test
    void canIvokeOperationAdmin(){
        //GIVEN
        User user = new User();
        Role role = new Role();
        role.setName("Admin");
        user.setRole(role);
        Withdraw withdraw = mock(Withdraw.class);
        //WHEN
        boolean result = target.canInvokeOperation(withdraw,user);

        //THEN
        assertTrue(result);
    }

    @Test
    void canIvokeOperationNonAdminPaymentIn(){
        //GIVEN
        User user = new User();
        Role role = new Role();
        user.setId(1);
        role.setName("non-admin");
        user.setRole(role);
        PaymentIn op = mock(PaymentIn.class);
        when(op.getType()).thenReturn(OperationType.PAYMENT_IN);
        //WHEN
        boolean result = target.canInvokeOperation(op,user);

        //THEN
        assertTrue(result);
    }

    @Test
    void canIvokeOperationNonAdminWithdrawOk(){
        //GIVEN
        User user = new User();
        user.setId(1);
        Role role = new Role();
        role.setName("non-admin");
        user.setRole(role);
        Withdraw op =  new Withdraw(user,1,"dsc",new Account());
        //WHEN
        boolean result = target.canInvokeOperation(op,user);

        //THEN
        assertTrue(result);
    }
@Test
    void canIvokeOperationNonAdminWithdrawInvalidUser(){
        //GIVEN
        User user = new User();
        user.setId(1);
        User user2 = new User();
        user2.setId(3);
        Role role = new Role();
        role.setName("non-admin");
        user2.setRole(role);
        Withdraw op =  new Withdraw(user,1,"dsc",new Account());
        //WHEN
        boolean result = target.canInvokeOperation(op,user2);

        //THEN
        assertFalse(result);
    }
    @Test
    void canIvokeOperationOtherOperation(){
        //GIVEN
        User user = new User();
        user.setId(1);
        User user2 = new User();
        user2.setId(3);
        Role role = new Role();
        role.setName("non-admin");
        user2.setRole(role);
        Operation op = mock(Operation.class);
        when(op.getType()).thenReturn(OperationType.INTEREST);
        //WHEN
        boolean result = target.canInvokeOperation(op,user2);

        //THEN
        assertFalse(result);
    }

}
