package cucumbreTests;

import biz.AccountManager;
import biz.AuthenticationManager;
import biz.BankHistory;
import biz.InterestOperator;
import db.dao.DAO;
import io.cucumber.java.Before;
import model.Account;
import model.User;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import model.exceptions.OperationIsNotAllowedException;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MoneyTransferSteps {
    DAO daoMock;
    AuthenticationManager authMock;
    BankHistory histMock;
    InterestOperator intrestMock;

    AccountManager aM;
    List<User> users = new ArrayList<User>();

    //@Given("SetUpTestEnv")
    @Before
    public void setUpTestEnv() throws NoSuchFieldException, IllegalAccessException {
        //... set up
        aM = new AccountManager();
        daoMock = mock(DAO.class);
        authMock = mock(AuthenticationManager.class);
        intrestMock = mock(InterestOperator.class);
        histMock = mock(BankHistory.class);
        Field field;
        try {
            field = AccountManager.class.getDeclaredField("dao");
            field.setAccessible(true);
            field.set(aM,daoMock); //aM.dao = dao;
            field.setAccessible(false);
            field = AccountManager.class.getDeclaredField("auth");
            field.setAccessible(true);
            field.set(aM,authMock); //aM.auth = authMock;
            field.setAccessible(false);
            field = AccountManager.class.getDeclaredField("interestOperator");
            field.setAccessible(true);
            field.set(aM,intrestMock); //aM.interestOperator = intrestMock;
            field.setAccessible(false);
            field = AccountManager.class.getDeclaredField("history");
            field.setAccessible(true);
            field.set(aM,histMock); //aM.history=histMock;
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Given("We have user {string} with id: {int}")
    public void setUpUserWithNameAndId(String name, int id) throws SQLException {
        User user = new User();
        user.setName(name);
        user.setId(id);
        users.add(user);
        when(daoMock.findUserByName(name)).thenReturn(user);
    }

    @Given("{string} have account: {int} with: {double} pln")
    public void setUpAccountWithNameIdandAmount(String name, int accId, double amount ) throws SQLException {
        User u = null;
        for (User _u: users ) {
            if (_u.getName().equals(name)){
                u = _u;
            }
        }
        if (u == null) throw new NullPointerException();
        Account acc = setupAccountWithIdandAmount(accId,amount);
        acc.setOwner(u);
    }

    @Given("There is an account:{int} with {double} pln")
    public Account setupAccountWithIdandAmount(int accId, double amount) throws SQLException {
        Account acc = new Account();
        acc.setId(accId);
        acc.setAmmount(amount);
        when(daoMock.findAccountById(accId)).thenReturn(acc);
        return acc;
    }

    @Given("Everything is authorised")
    public void authorizeEverything(){
        when(authMock.canInvokeOperation(any(), any())).thenReturn(true);
    }

    @When("{string} make transfer from acc: {int} to acc: {int} with amount: {double}")
    public void makeTransfer(String name, int srcId, int dstId, double amount) throws OperationIsNotAllowedException, SQLException {
        User u = null;
        for (User _u: users ) {
            if (_u.getName().equals(name)){
                u = _u;
            }
        }
        if (u == null) throw new NullPointerException();
        aM.internalPayment(u, amount, "Opis", srcId, dstId);
    }

    @Then("account:{int} value:{double} pln")
    public void checkAccountAmount(int accId, double value) throws SQLException {
        Account acc = daoMock.findAccountById(accId);
        assertEquals(acc.getAmmount(),value,0.001);
    }

}