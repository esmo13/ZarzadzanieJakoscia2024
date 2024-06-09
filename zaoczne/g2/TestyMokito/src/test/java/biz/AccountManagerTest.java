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
//        = new Account();
//        a.setOwner(user);
//        a.setAmmount(100);
//        a.setId(accId);
        String desc = "Wpłata";
        double amount = 123;
        when(mockDao.findAccountById(eq(accId))).thenReturn(a);
        when(mockDao.updateAccountState(eq(a))).thenReturn(true);
        //WHEN
        boolean result = target.paymentIn(user,amount,desc,accId);
        //THEN
        //System.out.println(target.dao.findAccountById(13));
        //System.out.println(target.dao.findAccountById(1));
        assertTrue(result);
        //Możemy sprawdzić stan konta po operacji
        //assertEquals(100+amount,a.getAmmount());
        verify(a, times(1)).income(amount);
        //Sprawzamy operacje na dao, find account by ID
        verify(mockDao, atMostOnce() ).findAccountById(eq(accId));
        verify(mockDao, atLeastOnce() ).findAccountById(anyInt());
        verify(mockDao, times(1) ).findAccountById(anyInt());
        verify(mockDao, atMostOnce() ).updateAccountState(eq(a));
        verify(mockDao, atLeastOnce() ).updateAccountState(any(Account.class));
        //Sprawdzamy czy zalogowano odpowiednie operacje
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
}