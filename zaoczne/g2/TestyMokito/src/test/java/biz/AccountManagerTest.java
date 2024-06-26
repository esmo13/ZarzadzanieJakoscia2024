package biz;

import db.dao.DAO;
import model.Account;
import model.Operation;
import model.User;
import model.exceptions.OperationIsNotAllowedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class AccountManagerTest {

    AccountManager target;
    @Mock
    DAO mockDao;
    @Mock
    BankHistory mockHistory; // = Mockito.mock(BankHistory.class);
    @Mock
    AuthenticationManager mockAuthManager;
    @Mock
    InterestOperator mockIntOperator;

    @BeforeEach
    void setUp() {
        target = new AccountManager();
        target.dao = mockDao;
        target.history=mockHistory;
        target.auth = mockAuthManager;
        target.interestOperator = mockIntOperator;
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void paymentIn() throws SQLException {
        //GIVEN
        int accId = 13;
        accId=12;
        User user = new User();
        Account a = mock(Account.class) ;

        String desc = "Wpłata";
        double amount = 123;
        when(mockDao.findAccountById(eq(accId))).thenReturn(a);
        when(mockDao.updateAccountState(eq(a))).thenReturn(true);
        when(a.income(amount)).thenReturn(true);
        //WHEN
        boolean result = target.paymentIn(user,amount,desc,accId);
        //THEN
        assertTrue(result);
        verify(a, times(1)).income(amount);

        verify(mockDao, atMostOnce() ).findAccountById(eq(accId));
        verify(mockDao, atLeastOnce() ).findAccountById(anyInt());
        verify(mockDao, times(1) ).findAccountById(anyInt());
        verify(mockDao, atMostOnce() ).updateAccountState(eq(a));
        verify(mockDao, atLeastOnce() ).updateAccountState(any(Account.class));

        verify(mockHistory, atLeastOnce()).logOperation(any(Operation.class),eq(true));
    }
    //PaymentIn przypadki testowe:
    // user == null, ammount <0, konto nie istnieje, nie udało się zupdatować bazy danych

    @Test
    void nullAccountpaymentIn() throws SQLException {
        //GIVEN
        when(mockDao.findAccountById(anyInt())).thenReturn(null);
        int accId = 13;
        User user = new User();
        String desc = "Wpłata";
        double amount = 123;
        //WHEN
        boolean result = target.paymentIn(user,amount,desc,accId);
        //THEN
        assertFalse(result);
    }
    @Test
    void amountLessThanZeroPaymentIn() throws SQLException {
        //GIVEN
        int accId = 13;
        User user = mock(User.class);
        Account a = mock(Account.class) ;
        double amount = -123;
        when(mockDao.findAccountById(anyInt())).thenReturn(a);
        when(a.income(amount)).thenReturn(false); //spodziewamy sie FALSE jeśli wartość dla metody INCOME jest ujemna
        String desc = "Wpłata";

        //WHEN
        boolean result = target.paymentIn(user,amount,desc,accId);
        //THEN
        assertFalse(result);
        // tu znaleziono pierwszy błąd, nie chcemy wykonywać updateAccountState jeśli metoda income() sie nie powiodła
        verify(mockDao, times(0)).updateAccountState(a);
        verify(mockHistory, atLeastOnce()).logOperation(any(Operation.class),eq(false));
    }

    @Test
    void dbFailPaymentIn() throws SQLException {
        //GIVEN
        int accId = 13;
        User user = mock(User.class);
        Account a = mock(Account.class) ;
        final double[] accDiff = {0};
        double amount = 123;
        when(mockDao.findAccountById(anyInt())).thenReturn(a);
        when(a.income(amount)).thenAnswer(invocation -> {
            accDiff[0]+= amount;
            return true;
        });
        when(a.outcome(amount)).thenAnswer(invocation ->{
            accDiff[0]-=amount;
            return true;
        });
        when(mockDao.updateAccountState(a)).thenReturn(false); //założenie testowe - operacja na bazie danych sie nie powiodła.
        String desc = "Wpłata";

        //WHEN
        boolean result = target.paymentIn(user,amount,desc,accId);
        //THEN
        assertFalse(result);
        //operacja income się powiodła
        verify(a, atLeastOnce()).income(amount);
        //tu znaleziono błąd drugi, operacja powinna zostać cofnięta
        verify(a, atLeastOnce()).outcome(amount);
        verify(mockHistory, atLeastOnce()).logOperation(any(Operation.class),eq(false));
    }

    @Test
    void dbThrowPaymentIn() throws SQLException {
        //GIVEN
        int accId = 13;
        User user = mock(User.class);
        Account a = mock(Account.class) ;
        final double[] accDiff = {0};
        double amount = 123;
        when(mockDao.findAccountById(anyInt())).thenReturn(a);
        when(a.income(amount)).thenAnswer(invocation -> {
            accDiff[0]+= amount;
            return true;
        });
        when(a.outcome(amount)).thenAnswer(invocation ->{
            accDiff[0]-=amount;
            return true;
        });
        //tu znaleziono błąd numer 3, metoda paymentInt nie handluje wyjątku SQLException rzuconego przez baze.
        when(mockDao.updateAccountState(a)).thenThrow(SQLException.class); //założenie testowe - operacja na bazie danych sie nie powiodła i rzuciła wyjątkiem.
        String desc = "Wpłata";

        //WHEN
        boolean result = target.paymentIn(user,amount,desc,accId);
        //THEN
        assertFalse(result);
        //operacja income się powiodła
        verify(a, atLeastOnce()).income(amount);
        //tu znaleziono błąd drugi, operacja powinna zostać cofnięta
        verify(a, atLeastOnce()).outcome(amount);
        verify(mockHistory, atLeastOnce()).logOperation(any(Operation.class),eq(false));
        assertEquals(accDiff[0], 0.0);
    }

    @Test
    void paymentOut() throws SQLException, OperationIsNotAllowedException {
        //GIVEN
        int accId = 13;
        User user = new User();
        Account a = mock(Account.class) ;

        String desc = "Wpłata";
        double amount = 123;
        when(mockDao.findAccountById(eq(accId))).thenReturn(a);
        when(mockDao.updateAccountState(eq(a))).thenReturn(true);
        when(a.income(amount)).thenReturn(true);
        //WHEN
        boolean result = target.paymentOut(user,amount,desc,accId);
        //THEN
        assertTrue(result);
        verify(a, times(1)).income(amount);

        verify(mockDao, times(1) ).findAccountById(anyInt());
        verify(mockDao, atMostOnce() ).updateAccountState(eq(a));
        verify(mockDao, atLeastOnce() ).updateAccountState(any(Account.class));

        verify(mockHistory, atLeastOnce()).logOperation(any(Operation.class),eq(true));
    }
}

