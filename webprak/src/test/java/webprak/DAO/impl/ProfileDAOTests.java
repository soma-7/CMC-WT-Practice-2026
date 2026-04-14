package webprak.DAO.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import webprak.DAO.AbstractDAOTests;
import webprak.DAO.ClientDAO;
import webprak.DAO.ProfileDAO;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProfileDAOTest extends AbstractDAOTests {

    @Autowired
    private ProfileDAO profileDAO;
    @Autowired
    private ClientDAO clientDAO;

    private Long createTestClient() {
        return clientDAO.createClient("{\"test\":true}");
    }

    @Test
    void createProfile_ShouldRejectDuplicatePhone() {
        Long cid = createTestClient();
        profileDAO.createProfile(cid, "a", "111", null);
        Long result = profileDAO.createProfile(cid, "b", "111", null);
        assert(result == null);
    }

    @Test
    void updateProfile_ShouldChangeFields() {
        Long cid = createTestClient();
        Long pid = profileDAO.createProfile(cid, "oldName", "222", "{}");
        profileDAO.updateProfile(pid, "newName", "333", "{\"new\":true}");
        List<Object[]> rows = profileDAO.getAllProfiles(0, 10, "name", true, null);
        Object[] row = rows.get(0);
        assertThat(row[1]).isEqualTo("newName");
        assertThat(row[2]).isEqualTo("333");
    }

    @Test
    void getAllProfiles_ShouldSupportPaginationAndSearch() {
        Long cid = createTestClient();
        profileDAO.createProfile(cid, "Zorro", "999", null);
        profileDAO.createProfile(cid, "Alice", "100", null);
        // Сортировка по имени ASC
        List<Object[]> list = profileDAO.getAllProfiles(0, 10, "name", true, null);
        assertThat(list.get(0)[1]).isEqualTo("Alice");
        assertThat(list.get(1)[1]).isEqualTo("Zorro");
        // Поиск
        List<Object[]> search = profileDAO.getAllProfiles(0, 10, "name", true, "ali");
        assertThat(search).hasSize(1);
        assertThat(search.get(0)[1]).isEqualTo("Alice");
    }

    @Test
    void updateBalance_ShouldIncreaseAndRespectNonNegative() {
        Long cid = createTestClient();
        Long pid = profileDAO.createProfile(cid, "test", "444", null);
        Double newBal = profileDAO.updateBalance(pid, 100.0);
        assertThat(newBal).isEqualTo(100.0);
        assertThrows(Exception.class, () -> profileDAO.updateBalance(pid, -200.0));
    }

    @Test
    void verifyBalanceConsistency_ShouldCheckConsistency() {
        Long cid = createTestClient();
        Long pid = profileDAO.createProfile(cid, "v", "555", null);
        profileDAO.updateBalance(pid, 50.0); // balance changing with no operations
        boolean consistent = profileDAO.verifyBalanceConsistency(pid);
        assert(!consistent);
    }
}
