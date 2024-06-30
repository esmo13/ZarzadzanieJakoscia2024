package biz;

import db.dao.DAO;
import db.dao.impl.DAOImpl;
import model.Account;
import model.Operation;
import model.User;
import model.exceptions.OperationIsNotAllowedException;
import model.operations.PaymentIn;
import model.operations.Withdraw;
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
        target.history = mockHistory;
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
        accId = 12;
        User user = new User();
        Account a = mock(Account.class);

        String desc = "Wpłata";
        double amount = 123;
        when(mockDao.findAccountById(eq(accId))).thenReturn(a);
        when(mockDao.updateAccountState(eq(a))).thenReturn(true);
        when(a.income(amount)).thenReturn(true);
        //WHEN
        boolean result = target.paymentIn(user, amount, desc, accId);
        //THEN
        assertTrue(result);
        verify(a, times(1)).income(amount);

        verify(mockDao, atMostOnce()).findAccountById(eq(accId));
        verify(mockDao, atLeastOnce()).findAccountById(anyInt());
        verify(mockDao, times(1)).findAccountById(anyInt());
        verify(mockDao, atMostOnce()).updateAccountState(eq(a));
        verify(mockDao, atLeastOnce()).updateAccountState(any(Account.class));

        verify(mockHistory, atLeastOnce()).logOperation(any(Operation.class), eq(true));
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
        boolean result = target.paymentIn(user, amount, desc, accId);
        //THEN
        assertFalse(result);
    }

    @Test
    void amountLessThanZeroPaymentIn() throws SQLException {
        //GIVEN
        int accId = 13;
        User user = mock(User.class);
        Account a = mock(Account.class);
        double amount = -123;
        when(mockDao.findAccountById(anyInt())).thenReturn(a);
        when(a.income(amount)).thenReturn(false); //spodziewamy sie FALSE jeśli wartość dla metody INCOME jest ujemna
        String desc = "Wpłata";

        //WHEN
        boolean result = target.paymentIn(user, amount, desc, accId);
        //THEN
        assertFalse(result);
        // tu znaleziono pierwszy błąd, nie chcemy wykonywać updateAccountState jeśli metoda income() sie nie powiodła
        verify(mockDao, times(0)).updateAccountState(a);
        verify(mockHistory, atLeastOnce()).logOperation(any(Operation.class), eq(false));
    }

    @Test
    void dbFailPaymentIn() throws SQLException {
        //GIVEN
        int accId = 13;
        User user = mock(User.class);
        Account a = mock(Account.class);
        final double[] accDiff = {0};
        double amount = 123;
        when(mockDao.findAccountById(anyInt())).thenReturn(a);
        when(a.income(amount)).thenAnswer(invocation -> {
            accDiff[0] += amount;
            return true;
        });
        when(a.outcome(amount)).thenAnswer(invocation -> {
            accDiff[0] -= amount;
            return true;
        });
        when(mockDao.updateAccountState(a)).thenReturn(false); //założenie testowe - operacja na bazie danych sie nie powiodła.
        String desc = "Wpłata";

        //WHEN
        boolean result = target.paymentIn(user, amount, desc, accId);
        //THEN
        assertFalse(result);
        //operacja income się powiodła
        verify(a, atLeastOnce()).income(amount);
        //tu znaleziono błąd drugi, operacja powinna zostać cofnięta
        verify(a, atLeastOnce()).outcome(amount);
        verify(mockHistory, atLeastOnce()).logOperation(any(Operation.class), eq(false));
        assertEquals(accDiff[0], 0.0);
    }

    @Test
    void dbThrowPaymentIn() throws SQLException {
        //GIVEN
        int accId = 13;
        User user = mock(User.class);
        Account a = mock(Account.class);
        final double[] accDiff = {0};
        double amount = 123;
        when(mockDao.findAccountById(anyInt())).thenReturn(a);
        when(a.income(amount)).thenAnswer(invocation -> {
            accDiff[0] += amount;
            return true;
        });
        when(a.outcome(amount)).thenAnswer(invocation -> {
            accDiff[0] -= amount;
            return true;
        });
        //tu znaleziono błąd numer 3, metoda paymentInt nie handluje wyjątku SQLException rzuconego przez baze.
        when(mockDao.updateAccountState(a)).thenThrow(SQLException.class); //założenie testowe - operacja na bazie danych sie nie powiodła i rzuciła wyjątkiem.
        String desc = "Wpłata";

        //WHEN
        boolean result = target.paymentIn(user, amount, desc, accId);
        //THEN
        assertFalse(result);
        //operacja income się powiodła
        verify(a, atLeastOnce()).income(amount);
        //tu znaleziono błąd drugi, operacja powinna zostać cofnięta
        verify(a, atLeastOnce()).outcome(amount);
        verify(mockHistory, atLeastOnce()).logOperation(any(Operation.class), eq(false));
        assertEquals(accDiff[0], 0.0);
    }

    @Test
    void paymentOut() throws SQLException, OperationIsNotAllowedException {
        //GIVEN
        int accId = 13;
        User user = mock(User.class);
        Account a = mock(Account.class);
        String desc = "Wyplata";
        double amount = 123;
        when(mockDao.findAccountById(eq(accId))).thenReturn(a);
        when(mockAuthManager.canInvokeOperation(any(Withdraw.class), any(User.class))).thenReturn(true);
        when(mockDao.updateAccountState(eq(a))).thenReturn(true);
        when(a.outcome(amount)).thenReturn(true);
        //WHEN
        boolean result = target.paymentOut(user, amount, desc, accId);
        //THEN
        assertTrue(result);
        verify(a, times(1)).outcome(amount);

        verify(mockDao, times(1)).findAccountById(anyInt());
        verify(mockDao, atMostOnce()).updateAccountState(eq(a));
        verify(mockDao, atLeastOnce()).updateAccountState(any(Account.class));

        verify(mockHistory, atLeastOnce()).logOperation(any(Withdraw.class), eq(true));
    }

    @Test
    void paymentOutAuthFail() throws SQLException, OperationIsNotAllowedException {
        //GIVEN
        int accId = 13;
        User user = mock(User.class);
        Account a = mock(Account.class);
        String desc = "Wyplata";
        double amount = 123;
        when(mockDao.findAccountById(eq(accId))).thenReturn(a);
        when(mockAuthManager.canInvokeOperation(any(Withdraw.class), any(User.class))).thenReturn(false);
        //WHEN
        OperationIsNotAllowedException thrown = assertThrows(OperationIsNotAllowedException.class, () -> {
            target.paymentOut(user, amount, desc, accId);
        });
        //THEN
        assertEquals("Unauthorized operation", thrown.getMessage());
        verify(mockAuthManager, times(1)).canInvokeOperation(any(Withdraw.class), any(User.class));
        verify(mockDao, times(1)).findAccountById(anyInt());
        verify(mockHistory, atLeastOnce()).logUnauthorizedOperation(any(Withdraw.class), eq(false));
    }

    @Test
    void paymentOutAccountNull() throws SQLException, OperationIsNotAllowedException {
        //GIVEN
        int accId = 13;
        User user = mock(User.class);
        Account a = mock(Account.class);
        String desc = "Wyplata";
        double amount = 123;
        when(mockDao.findAccountById(eq(accId))).thenReturn(null);
        //WHEN
        boolean result = target.paymentOut(user, amount, desc, accId);

        //THEN
        //tu znaleziono bład 4, jeśli account==null operacja powinna się nie powieść.
        assertFalse(result);
        verify(mockDao, times(1)).findAccountById(anyInt());
    }

    @Test
    void paymentOutOutcomeFail() throws SQLException, OperationIsNotAllowedException {
        //GIVEN
        int accId = 13;
        User user = mock(User.class);
        Account a = mock(Account.class);
        String desc = "Wyplata";
        double amount = 123;
        when(mockDao.findAccountById(eq(accId))).thenReturn(a);
        when(mockAuthManager.canInvokeOperation(any(Withdraw.class), any(User.class))).thenReturn(true);
        when(a.outcome(amount)).thenReturn(false);
        //WHEN
        boolean result = target.paymentOut(user, amount, desc, accId);
        //THEN
        assertFalse(result);
        verify(a, times(1)).outcome(amount);

        verify(mockDao, times(1)).findAccountById(anyInt());
        //tu znaleziono błąd 5, nie chcemy wykonywać zapytania do bazy danych jeśli metoda outcome się nie powiedzie
        verify(mockDao, times(0)).updateAccountState(eq(a));

        verify(mockHistory, atLeastOnce()).logOperation(any(Withdraw.class), eq(false));
    }

    @Test
    void paymentOutDbFail() throws SQLException, OperationIsNotAllowedException {
        //GIVEN
        int accId = 13;
        User user = mock(User.class);
        Account a = mock(Account.class);
        String desc = "Wyplata";
        double amount = 123;
        when(mockDao.findAccountById(eq(accId))).thenReturn(a);
        when(mockAuthManager.canInvokeOperation(any(Withdraw.class), any(User.class))).thenReturn(true);
        when(mockDao.updateAccountState(eq(a))).thenReturn(false);
        when(a.outcome(amount)).thenReturn(true);
        //WHEN
        boolean result = target.paymentOut(user, amount, desc, accId);
        //THEN
        assertFalse(result);
        verify(a, times(1)).outcome(amount);

        verify(mockDao, times(1)).findAccountById(anyInt());
        verify(mockDao, times(1)).updateAccountState(eq(a));

        verify(mockHistory, atLeastOnce()).logOperation(any(Withdraw.class), eq(false));
    }

    @Test
    void paymentOutDbFailThrow() throws SQLException, OperationIsNotAllowedException {
        //GIVEN
        int accId = 13;
        User user = mock(User.class);
        Account a = mock(Account.class);
        String desc = "Wyplata";
        double amount = 123;
        when(mockDao.findAccountById(eq(accId))).thenReturn(a);
        when(mockAuthManager.canInvokeOperation(any(Withdraw.class), any(User.class))).thenReturn(true);
        when(mockDao.updateAccountState(eq(a))).thenThrow(SQLException.class);
        when(a.outcome(amount)).thenReturn(true);
        //WHEN
        boolean result = target.paymentOut(user, amount, desc, accId);
        //THEN
        assertFalse(result);
        verify(a, times(1)).outcome(amount);

        verify(mockDao, times(1)).findAccountById(anyInt());
        verify(mockDao, times(1)).updateAccountState(eq(a));

        verify(mockHistory, atLeastOnce()).logOperation(any(Withdraw.class), eq(false));
    }

    @Test
    void internalPayment() throws SQLException, OperationIsNotAllowedException {
        //GIVEN
        int srcId = 13;
        int destId = 14;
        User user = mock(User.class);
        Account srcAcc = mock(Account.class);
        Account destAcc = mock(Account.class);
        String desc = "Wyplata";
        double amount = 123;
        when(mockDao.findAccountById(eq(srcId))).thenReturn(srcAcc);
        when(mockDao.findAccountById(eq(destId))).thenReturn(destAcc);
        when(mockAuthManager.canInvokeOperation(any(Withdraw.class), any(User.class))).thenReturn(true);
        when(srcAcc.outcome(amount)).thenReturn(true);
        when(destAcc.income(amount)).thenReturn(true);
        when(mockDao.updateAccountState(srcAcc)).thenReturn(true);
        when(mockDao.updateAccountState(destAcc)).thenReturn(true);

        //WHEN
        boolean result = target.internalPayment(user, amount, desc, srcId, destId);
        //THEN
        assertTrue(result);
        verify(srcAcc, times(1)).outcome(amount);
        verify(destAcc, times(1)).income(amount);
        verify(mockDao, times(2)).findAccountById(anyInt());
        verify(mockDao, times(1)).updateAccountState(eq(srcAcc));
        verify(mockDao, times(1)).updateAccountState(eq(destAcc));
        verify(mockHistory, times(1)).logOperation(any(Withdraw.class), eq(true));
        verify(mockHistory, times(1)).logOperation(any(PaymentIn.class), eq(true));
    }

    @Test
    void internalPaymentIncomeFail() throws SQLException, OperationIsNotAllowedException {
        //GIVEN
        int srcId = 13;
        int destId = 14;
        User user = mock(User.class);
        Account srcAcc = mock(Account.class);
        Account destAcc = mock(Account.class);
        String desc = "Wyplata";
        double amount = 123;
        when(mockDao.findAccountById(eq(srcId))).thenReturn(srcAcc);
        when(mockDao.findAccountById(eq(destId))).thenReturn(destAcc);
        when(mockAuthManager.canInvokeOperation(any(Withdraw.class), any(User.class))).thenReturn(true);
        when(srcAcc.outcome(amount)).thenReturn(true);
        when(destAcc.income(amount)).thenReturn(false);


        //WHEN
        boolean result = target.internalPayment(user, amount, desc, srcId, destId);
        //THEN
        assertFalse(result);
        verify(srcAcc, times(1)).outcome(amount);
        verify(destAcc, times(1)).income(amount);
        verify(mockDao, times(2)).findAccountById(anyInt());
        verify(mockDao, times(0)).updateAccountState(eq(srcAcc));
        verify(mockDao, times(0)).updateAccountState(eq(destAcc));
        verify(mockHistory, times(1)).logOperation(any(Withdraw.class), eq(false));
        verify(mockHistory, times(1)).logOperation(any(PaymentIn.class), eq(false));
    }

    @Test
    void internalPaymentOutcomeFail() throws SQLException, OperationIsNotAllowedException {
        //GIVEN
        int srcId = 13;
        int destId = 14;
        User user = mock(User.class);
        Account srcAcc = mock(Account.class);
        Account destAcc = mock(Account.class);
        String desc = "Wyplata";
        double amount = 123;
        when(mockDao.findAccountById(eq(srcId))).thenReturn(srcAcc);
        when(mockDao.findAccountById(eq(destId))).thenReturn(destAcc);
        when(mockAuthManager.canInvokeOperation(any(Withdraw.class), any(User.class))).thenReturn(true);
        when(srcAcc.outcome(amount)).thenReturn(false);


        //WHEN
        boolean result = target.internalPayment(user, amount, desc, srcId, destId);
        //THEN
        assertFalse(result);
        verify(srcAcc, times(1)).outcome(amount);
        verify(destAcc, times(0)).income(amount);
        verify(mockDao, times(2)).findAccountById(anyInt());
        verify(mockDao, times(0)).updateAccountState(eq(srcAcc));
        verify(mockDao, times(0)).updateAccountState(eq(destAcc));
        verify(mockHistory, times(1)).logOperation(any(Withdraw.class), eq(false));
        verify(mockHistory, times(1)).logOperation(any(PaymentIn.class), eq(false));
    }

    @Test
    void internalPaymentDaoFailDest() throws SQLException, OperationIsNotAllowedException {
        //GIVEN
        int srcId = 13;
        int destId = 14;
        User user = mock(User.class);
        Account srcAcc = mock(Account.class);
        Account destAcc = mock(Account.class);
        String desc = "Wyplata";
        double amount = 123;
        when(mockDao.findAccountById(eq(srcId))).thenReturn(srcAcc);
        when(mockDao.findAccountById(eq(destId))).thenReturn(destAcc);
        when(mockAuthManager.canInvokeOperation(any(Withdraw.class), any(User.class))).thenReturn(true);
        when(srcAcc.outcome(amount)).thenReturn(true);
        when(destAcc.income(amount)).thenReturn(true);
        when(mockDao.updateAccountState(srcAcc)).thenReturn(true);
        when(mockDao.updateAccountState(destAcc)).thenReturn(false);


        //WHEN
        boolean result = target.internalPayment(user, amount, desc, srcId, destId);
        //THEN

        //tu znaleziono GIGANTYCZNY błąd 9, jeśli updateAccountState dla destAcc się powiedzie, wynik funkcji jest pozytywny!!
        assertFalse(result);
        verify(srcAcc, times(1)).outcome(amount);
        verify(destAcc, times(1)).income(amount);
        verify(mockDao, times(2)).findAccountById(anyInt());
        verify(mockDao, times(1)).updateAccountState(eq(srcAcc));
        verify(mockDao, times(1)).updateAccountState(eq(destAcc));
        verify(mockHistory, times(1)).logOperation(any(Withdraw.class), eq(false));
        verify(mockHistory, times(1)).logOperation(any(PaymentIn.class), eq(false));
    }

    @Test
    void internalPaymentAuthFail() throws SQLException, OperationIsNotAllowedException {
        //GIVEN
        int srcId = 13;
        int destId = 14;
        User user = mock(User.class);
        Account srcAcc = mock(Account.class);
        Account destAcc = mock(Account.class);
        String desc = "Wyplata";
        double amount = 123;
        when(mockDao.findAccountById(eq(srcId))).thenReturn(srcAcc);
        when(mockDao.findAccountById(eq(destId))).thenReturn(destAcc);
        when(mockAuthManager.canInvokeOperation(any(Withdraw.class), any(User.class))).thenReturn(false);


        //WHEN
        OperationIsNotAllowedException thrown = assertThrows(OperationIsNotAllowedException.class, () -> {
            target.internalPayment(user, amount, desc, srcId, destId);
        });

        //THEN
        assertEquals("Unauthorized operation", thrown.getMessage());
        verify(srcAcc, times(0)).outcome(amount);
        verify(destAcc, times(0)).income(amount);
        verify(mockDao, times(2)).findAccountById(anyInt());
        verify(mockDao, times(0)).updateAccountState(eq(srcAcc));
        verify(mockDao, times(0)).updateAccountState(eq(destAcc));
        verify(mockHistory, times(0)).logOperation(any(Withdraw.class), eq(false));
        verify(mockHistory, times(0)).logOperation(any(PaymentIn.class), eq(false));
    }

    @Test
    void internalPaymentDestAccNull() throws SQLException, OperationIsNotAllowedException {
        //GIVEN
        int srcId = 13;
        int destId = 14;
        User user = mock(User.class);
        Account srcAcc = mock(Account.class);
        Account destAcc = mock(Account.class);
        String desc = "Wyplata";
        double amount = 123;
        when(mockDao.findAccountById(eq(srcId))).thenReturn(srcAcc);
        when(mockDao.findAccountById(eq(destId))).thenReturn(null);

        //WHEN

        //tu wykryto błąd 8
        boolean result = target.internalPayment(user, amount, desc, srcId, destId);

        //THEN
        assertFalse(result);
        verify(srcAcc, times(0)).outcome(amount);
        verify(destAcc, times(0)).income(amount);
        verify(mockDao, times(2)).findAccountById(anyInt());
        verify(mockDao, times(0)).updateAccountState(eq(srcAcc));
        verify(mockDao, times(0)).updateAccountState(eq(destAcc));
        verify(mockHistory, times(1)).logOperation(any(Withdraw.class), eq(false));
        verify(mockHistory, times(1)).logOperation(any(PaymentIn.class), eq(false));
    }

    @Test
    void internalPaymentSrcAccNull() throws SQLException, OperationIsNotAllowedException {
        //GIVEN
        int srcId = 13;
        int destId = 14;
        User user = mock(User.class);
        Account srcAcc = mock(Account.class);
        Account destAcc = mock(Account.class);
        String desc = "Wyplata";
        double amount = 123;
        when(mockDao.findAccountById(eq(srcId))).thenReturn(null);
        when(mockDao.findAccountById(eq(destId))).thenReturn(destAcc);

        //WHEN

        boolean result = target.internalPayment(user, amount, desc, srcId, destId);

        //THEN
        assertFalse(result);
        verify(srcAcc, times(0)).outcome(amount);
        verify(destAcc, times(0)).income(amount);
        verify(mockDao, times(2)).findAccountById(anyInt());
        verify(mockDao, times(0)).updateAccountState(eq(srcAcc));
        verify(mockDao, times(0)).updateAccountState(eq(destAcc));
        verify(mockHistory, times(1)).logOperation(any(Withdraw.class), eq(false));
        verify(mockHistory, times(1)).logOperation(any(PaymentIn.class), eq(false));
    }
@Test
    void buildBank(){
        //GIVEN
    AccountManager.makeDao = DAOImpl::new;
        //WHEN
        AccountManager built =  AccountManager.buildBank();
        //THEN
        assertNotNull(built);
    }
    @Test
    void buildBankThrowSql() throws SQLException{
        //GIVEN
        //tu znaleziono blad 10
        AccountManager.makeDao = ()->{
            throw new SQLException("test");
        };
        //WHEN
        AccountManager built =  AccountManager.buildBank();
        //THEN
        assertNull(built);
    }

    @Test
    void buildBankThrowClassNotFound() throws SQLException{
        //GIVEN
        AccountManager.makeDao = ()->{
            throw new ClassNotFoundException("test");
        };
        //WHEN
        AccountManager built =  AccountManager.buildBank();
        //THEN
        assertNull(built);
    }
    @Test
    void buildBankThrowThrowable() throws SQLException{
        //GIVEN
        AccountManager.makeDao = ()->{
            throw new java.lang.Throwable("test");
        };
        //WHEN
        AccountManager built =  AccountManager.buildBank();
        //THEN
        assertNull(built);
    }
}



