package biz;

import db.dao.DAO;
import model.Account;
import model.Operation;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankHistoryTest {

    BankHistory target;
    @Mock
    DAO mockDao;


    @BeforeEach
    void setUp() {
        target = new BankHistory(mockDao);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testConstruction() throws NoSuchFieldException, IllegalAccessException {
        //GIVEN
        Field daoField = BankHistory.class.getDeclaredField("dao");
        daoField.setAccessible(true);

        //WHEN
        //zrealizowane w metodzie setUp()
        //THEN

        assertEquals(mockDao, daoField.get(target));
    }
    @Test
    void logLoginSuccess() throws SQLException {
        //GIVEN
        User user = mock(User.class);
        //WHEN
        target.logLoginSuccess(user);
        //THEN
        verify(mockDao, times(1)).logOperation(any(Operation.class),eq(true));

    }

    @Test
    void logLoginFailure() throws SQLException {
        //GIVEN
        User user = mock(User.class);
        String info = "arbitrary string";
        //WHEN
        target.logLoginFailure(user,info);
        //THEN
        verify(mockDao, times(1)).logOperation(any(Operation.class),eq(false));

    }

    @Test
    void logLogOut() throws SQLException {
        //GIVEN
        User user = mock(User.class);
        //WHEN
        target.logLogOut(user);
        //THEN
        verify(mockDao, times(1)).logOperation(any(Operation.class),eq(true));

    }
    @Test
    void logPaymentIn() throws SQLException {
        //GIVEN
        Account acc = mock(Account.class);
        //WHEN
        RuntimeException ex = assertThrows(RuntimeException.class,()->target.logPaymentIn(acc,1,true));
        //THEN
        verify(mockDao, times(0)).logOperation(any(Operation.class),eq(true));
        assertEquals(ex.getMessage(),"Not implemented");
    }
    @Test
    void logPaymentOut() throws SQLException {
        //GIVEN
        Account acc = mock(Account.class);
        //WHEN
        RuntimeException ex = assertThrows(RuntimeException.class,()->target.logPaymentOut(acc,1,true));
        //THEN
        verify(mockDao, times(0)).logOperation(any(Operation.class),eq(true));
        assertEquals(ex.getMessage(),"Not implemented");
    }
    @Test
    void logUnauthorizedOperation() throws SQLException {
        //GIVEN
        Operation op = mock(Operation.class);
        //WHEN
        RuntimeException ex = assertThrows(RuntimeException.class,()->target.logUnauthorizedOperation(op,true));
        //THEN
        verify(mockDao, times(0)).logOperation(any(Operation.class),eq(true));
        assertEquals(ex.getMessage(),"Not implemented");
    }

    @Test
    void logOperationDaoThrow() throws SQLException {
        //GIVEN
        Operation op = mock(Operation.class);
        doThrow(new SQLException("")).when(mockDao).logOperation(op,true);
        //WHEN
        assertThrows(SQLException.class,()->target.logOperation(op,true));
        //THEN
        verify(mockDao, times(1)).logOperation(any(Operation.class),eq(true));

    }
    @Test
    void logOperation() throws SQLException {
        //GIVEN
        Operation op = mock(Operation.class);
        //WHEN
        target.logOperation(op,true);
        //THEN
        verify(mockDao, times(1)).logOperation(any(Operation.class),eq(true));

    }
}