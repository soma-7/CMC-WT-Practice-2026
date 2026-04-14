package webprak.DAO.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import webprak.DAO.*;
import webprak.models.Profile;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OperationDAOTests extends AbstractDAOTests {

    @Autowired
    private OperationDAO operationDAO;
    @Autowired
    private ClientDAO clientDAO;
    @Autowired
    private ProfileDAO profileDAO;
    @Autowired
    private ServiceDAO serviceDAO;

    private Long createTestProfile() {
        Long cid = clientDAO.createClient("{}");
        return profileDAO.createProfile(cid, "testProfile", "12345", "{}");
    }

    private Long createTestService() {
        return serviceDAO.createService("TestSvc", "desc", "{}", "{}");
    }

    @Test
    void createOperation_Deposit_ShouldIncreaseBalance() {
        Long pid = createTestProfile();
        Profile profile = profileDAO.getById(pid);
        Long opId = operationDAO.createOperation(pid, "deposit", null, 50.0, "test deposit");
        assertThat(opId).isPositive();
        Double balance = profile.getBalance();
        assertThat(balance).isEqualTo(50.0);
    }

    @Test
    void createOperation_Withdrawal_ShouldDecreaseBalance() {
        Long pid = createTestProfile();
        Profile profile = profileDAO.getById(pid);
        operationDAO.createOperation(pid, "deposit", null, 100.0, null);
        Long opId = operationDAO.createOperation(pid, "withdrawal", null, -30.0, "cash");
        assertThat(opId).isPositive();
        Double balance = profile.getBalance();
        assertThat(balance).isEqualTo(70.0);
    }

    @Test
    void createOperation_WithService_ShouldRequireServiceId() {
        Long pid = createTestProfile();
        Long sid = createTestService();
        operationDAO.createOperation(pid, "deposit", null, 100.0, null);
        Long opId = operationDAO.createOperation(pid, "service_purchase", sid, -20.0, "buy");
        assertThat(opId).isPositive();
        // check that service id was stored
        List<Object[]> ops = operationDAO.getAllOperations(0, 10, pid, null, null, null, null, null);
        Object[] op = ops.get(0);
        assertThat(op[8]).isEqualTo(sid.intValue()); // service_id
    }

    @Test
    void createOperation_ShouldFailIfInsufficientBalance() {
        Long pid = createTestProfile();
        assertThrows(InvalidDataAccessApiUsageException.class,
                () -> operationDAO.createOperation(pid, "withdrawal", null, -10.0, "fail"));
    }

    @Test
    void getAllOperations_ShouldFilterByClientAndDate() {
        Long cid = clientDAO.createClient("{}");
        Long pid1 = profileDAO.createProfile(cid, "p1", "111", "{}");
        Long pid2 = profileDAO.createProfile(cid, "p2", "222", "{}");
        operationDAO.createOperation(pid1, "deposit", null, 10.0, null);
        operationDAO.createOperation(pid2, "deposit", null, 20.0, null);

        LocalDateTime now = LocalDateTime.now();
        List<Object[]> byClient = operationDAO.getAllOperations(0, 10, null, cid, null, null, now.minusMinutes(1), now.plusMinutes(1));
        assertThat(byClient).hasSize(2);

        List<Object[]> byType = operationDAO.getAllOperations(0, 10, null, null, "deposit", null, null, null);
        assertThat(byType).hasSize(2);
    }
}