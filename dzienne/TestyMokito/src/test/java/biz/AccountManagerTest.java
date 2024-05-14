package biz;

import db.dao.DAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AccountManagerTest {

    AccountManager target;
    @Mock
    DAO mockDao = Mockito.mock(DAO.class);
    @Mock
    BankHistory mockHistory = Mockito.mock(BankHistory.class);
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
    void paymentIn() {
        System.out.println(target.auth);
    }}