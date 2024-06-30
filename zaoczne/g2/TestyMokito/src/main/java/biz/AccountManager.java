package biz;

import db.dao.DAO;
import db.dao.impl.DAOImpl;
import db.dao.impl.SQLiteDB;
import model.Account;
import model.Operation;
import model.User;
import model.exceptions.OperationIsNotAllowedException;
import model.exceptions.UserUnnkownOrBadPasswordException;
import model.operations.PaymentIn;
import model.operations.Withdraw;
import org.junit.jupiter.api.function.ThrowingSupplier;

import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * Created by Krzysztof Podlaski on 04.03.2018.
 */
public class AccountManager {
    DAO dao;
    static ThrowingSupplier<DAO> makeDao = DAOImpl::new;
    BankHistory history;
    AuthenticationManager auth;
    InterestOperator interestOperator;
    User loggedUser=null;

    public boolean paymentIn(User user, double ammount, String description, int accountId) throws SQLException {
        Account account = dao.findAccountById(accountId);
        Operation operation = new PaymentIn(user, ammount,description, account);
        boolean success = false;
        if (account != null) {
            success = account.income(ammount);
            // pierwszy znaleziony błąd, nie musimy nadpisywać zmiennej success, jeśl account.income się nie powiedzie.
            //szczególnie że zapytania do bazy danych są kosztowne.
            if (success){
                //trzeci błąd, brak obsługi wyjątku rzucanego przez dao
                // NIEKONIECZNIE JEST TO BŁĄD, KWESTIA UMOWNA
                //chociaż wtedy w pamięci mamy stan konta inny niż w bazie danych
                //więc może nie kwestia umowna
                try{
            success = dao.updateAccountState(account);
            }catch (SQLException e){
                    success=false;
                }
            // drugi znaleziony błąd, jeśli operacja na bazie danych się nie powiedzie, powinniśmy cofnąć operacje
                // NIEKONIECZNIE JEST TO BŁĄD, KWESTIA UMOWNA, jeśli uznamy błąd 3 za błąd, to wtedy na pewno ten błąd też jest błędem
            if (!success){
                account.outcome(ammount);
            }
            }


        }
        history.logOperation(operation, success);
        return success;
    }

    public boolean paymentOut(User user, double ammount, String description, int accountId) throws OperationIsNotAllowedException, SQLException {
        Account account = dao.findAccountById(accountId);
        //błąd 4, nie sprawdzamy czy account istnieje
        boolean success = false;
        Operation operation = new Withdraw(user, ammount, description, account);
        if (account != null) {
            success = auth.canInvokeOperation(operation, user);
            if (!success) {
                history.logUnauthorizedOperation(operation, success);
                throw new OperationIsNotAllowedException("Unauthorized operation");
            }
            success = account.outcome(ammount);
            // blad 5, taki sam jak w paymentIn
            if (success) {
                //błąd 6, taki sam jak w paymentIn
                try{
                success = dao.updateAccountState(account);}
                catch (SQLException e){
                    success=false;
                }
                //błąd 7, taki sam jak w paymentIn
                if (!success){
                    account.income(ammount);
                }
            }
        }
            history.logOperation(operation, success);
            return success;

    }

    public boolean internalPayment(User user, double ammount, String description, int sourceAccountId, int destAccountId) throws OperationIsNotAllowedException, SQLException {
        //bardziej zły practice niż jawny błąd, ale uważam całą aktualną implementację funkcji za bezsensowną duplikację kodu (i tym samym błędów!)
        //cała ta funkcja mogłaby wywoływać paymentIn i paymentOut
        // ale rozumiem że na zaliczeniu sprawdzamy umiejętność pisania testów jednostkowych
        Account sourceAccount = dao.findAccountById(sourceAccountId);
        Account destAccount = dao.findAccountById(destAccountId);
        boolean success = false;
        Operation withdraw = new Withdraw(user, ammount, description, sourceAccount);
        Operation payment = new PaymentIn(user, ammount, description, destAccount);
        //błąd 8, brak null checka
        if (sourceAccount != null && destAccount != null) {
            success = auth.canInvokeOperation(withdraw, user);
            if (!success) {
                history.logUnauthorizedOperation(withdraw, success);
                throw new OperationIsNotAllowedException("Unauthorized operation");
            }
            success = sourceAccount.outcome(ammount);
            success = success && destAccount.income(ammount);
            if (success) {
                success = dao.updateAccountState(sourceAccount);
                //błąd 9, nie aktualizujemy success jeśli następna linijka się nie powiedzie
                if (success) success=dao.updateAccountState(destAccount);
            }
        }
        history.logOperation(withdraw, success);
        history.logOperation(payment, success);
        return success;
    }

    public static AccountManager buildBank() {
        try {
            //błąd(?) 10, nietestowalna architektura
            DAO dao = makeDao.get();
            BankHistory history = new BankHistory(dao);
            AuthenticationManager am = new AuthenticationManager(dao, history);
            AccountManager aManager = new AccountManager();
            InterestOperator io = new InterestOperator(dao, aManager,history);
            aManager.dao = dao;
            aManager.auth = am;
            aManager.history = history;
            aManager.interestOperator = io;
            return aManager;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (java.lang.Throwable e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean logIn(String userName, char[] password) throws UserUnnkownOrBadPasswordException, SQLException {
        loggedUser =  auth.logIn(userName, password);
        return loggedUser!=null;
    }

    public boolean logOut(User user) throws SQLException {
        if (auth.logOut(user)) {
            loggedUser = null;
            return true;
        }
        return false;
    }

    public User getLoggedUser() {
        return loggedUser;
    }
}
