package cucumbreTests;

import db.dao.DAO;
import io.cucumber.java.en.Given;
import model.User;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//@ExtendWith(MockitoExtension.class)
class MoneyTransferSteps {

    @Mock
    DAO daoMock = mock(DAO.class);

    List<User> users = new ArrayList<>();

    @Given("We have user {string} with id: {int}")
    public void setUpUserWithNameAndId(String name, int id) throws SQLException, SQLException {
        User user = new User();
        user.setName(name);
        user.setId(id);
        users.add(user);
        when(daoMock.findUserByName(name)).thenReturn(user);
    }
}
