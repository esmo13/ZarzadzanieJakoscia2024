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

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterestOperatorTest {

    InterestOperator target;
    @Mock
    DAO mockDao;
    @Mock
    AccountManager mockAccountManager; // = Mockito.mock(BankHistory.class);
    @Mock
    BankHistory mockBankHistory;

    @BeforeEach
    void setUp() {
        target = new InterestOperator(mockDao,mockAccountManager,mockBankHistory);
    }

    @AfterEach
    void tearDown() {
    }
    @Test
    void testConstruction(){
        //GIVEN
        //WHEN
        //THEN
       assertEquals(target.dao, mockDao);
       assertEquals(target.accountManager, mockAccountManager);
       assertEquals(target.bankHistory, mockBankHistory);
    }
    @Test
    void countInterestForAccount() throws SQLException {
        //GIVEN
        User user = mock(User.class);
        Account acc = mock(Account.class);
        Double am = 256.;
        Double expectedInterest = am*0.2;
        when(acc.getAmmount()).thenReturn(am);
        when(mockDao.findUserByName("InterestOperator")).thenReturn(user);
        when(mockAccountManager.paymentIn(any(User.class),any(Double.class),any(String.class),any(Integer.class))).thenReturn(true);
        //WHEN
        target.countInterestForAccount(acc);
        //THEN
        verify(mockDao, times(1)).findUserByName(anyString());
        verify(mockAccountManager, times(1)).paymentIn(any(User.class),eq(expectedInterest),any(String.class),any(Integer.class));
        verify(mockBankHistory,times(1)).logOperation(any(Operation.class),eq(true));

    }
    @Test
    void countInterestForAccountFail() throws SQLException {
        //GIVEN
        User user = mock(User.class);
        Account acc = mock(Account.class);
        Double am = 256.;
        Double expectedInterest = am*0.2;
        when(acc.getAmmount()).thenReturn(am);
        when(mockDao.findUserByName("InterestOperator")).thenReturn(user);
        when(mockAccountManager.paymentIn(any(User.class),any(Double.class),any(String.class),any(Integer.class))).thenReturn(false);
        //WHEN
        target.countInterestForAccount(acc);
        //THEN
        verify(mockDao, times(1)).findUserByName(anyString());
        verify(mockAccountManager, times(1)).paymentIn(any(User.class),eq(expectedInterest),any(String.class),any(Integer.class));
        verify(mockBankHistory,times(1)).logOperation(any(Operation.class),eq(false));

    }
}

