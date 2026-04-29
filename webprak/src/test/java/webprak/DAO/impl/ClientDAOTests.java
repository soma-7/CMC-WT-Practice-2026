package webprak.DAO.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import webprak.DAO.AbstractDAOTests;
import webprak.DAO.ClientDAO;
import webprak.DAO.ProfileDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClientDAOTest extends AbstractDAOTests {

    @Autowired
    private ClientDAO clientDAO;
    @Autowired
    private ProfileDAO profileDAO;
    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void createClient_ShouldCreateWithCorrectFields() {
        Long id = clientDAO.createClient("{\"email\": \"a@b.com\"}");
        assertThat(id).isPositive();
        Object[] client = clientDAO.getClientWithProfiles(id).get(0);
        assertThat(client[1]).isNotNull(); // registration_date
        assertThat(client[2]).isEqualTo("{\"email\": \"a@b.com\"}");
    }

    @Test
    void updateClientInfo_ShouldChangeInfo() {
        Long id = clientDAO.createClient("{\"old\": true}");
        clientDAO.updateClientInfo(id, "{\"new\": true}");
        List<Object[]> clients = clientDAO.getClientWithProfiles(id);
        assertThat(clients.get(0)[2]).isEqualTo("{\"new\": true}");
    }

    @Test
    void getAllClients_ShouldSortByAggregatedData() {
        Long cid1 = clientDAO.createClient("{\"name\": \"ivan\"}");
        Long pid11 = profileDAO.createProfile(cid1, "p1", "111", "{}");
        Long pid12 = profileDAO.createProfile(cid1, "p2", "222", "{}");
        Long cid2 = clientDAO.createClient("{\"name\": \"maria\"}");
        Long pid21 = profileDAO.createProfile(cid2, "p3", "333", "{}");
        profileDAO.updateBalance(pid11, 100.0);
        List<Object[]> list = clientDAO.getAllClients(0, 10, "totalBalance", false, "");
        assertThat(list).hasSize(2);
        Object[] row = list.get(0);
        assertThat(row[2]).isEqualTo(2L); // profile_count
    }

    @Test
    void getClientsByRegistrationDateRange_ShouldFilterByDateRange() throws InterruptedException {
        LocalDateTime now = LocalDateTime.now();
        Long cid1 = clientDAO.createClient("{}");
        Long cid2 = clientDAO.createClient("{}");
        Thread.sleep(Duration.ofSeconds(2).toMillis());
        Long cid3 = clientDAO.createClient("{}");
        List<Object[]> range = clientDAO.getClientsByRegistrationDateRange(0, 10, now.minusSeconds(1), now.plusSeconds(1));
        assertThat(range).hasSize(2);
    }

    @Test
    void getClientWithProfiles_ShouldReturnClientAndProfiles() {
        Long cid = clientDAO.createClient("{\"test\":true}");
        Long pid = profileDAO.createProfile(cid, "nick", "123456", "{\"card\":\"xxx\"}");
        List<Object[]> rows = clientDAO.getClientWithProfiles(cid);
        assertThat(rows).hasSize(1);
        Object[] row = rows.get(0);
        assertThat(((Number) row[0]).longValue()).isEqualTo(cid); // client_id
        assertThat(((Number) row[3]).longValue()).isEqualTo(pid); // profile_id
        assertThat(row[4]).isEqualTo("nick");
        assertThat(row[5]).isEqualTo("123456");
    }

    @Test
    void deleteClient_ProfileCascadeDelete() {
        Long cid = clientDAO.createClient("{}");
        Long pid = profileDAO.createProfile(cid, "x", "123", "{}");
        profileDAO.updateBalance(pid, 50.0);
        clientDAO.delete(clientDAO.getById(cid));

        Number count = (Number) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM profiles WHERE profile_id = :pid")
                .setParameter("pid", pid)
                .getSingleResult();
        assertThat(count.intValue()).isZero();
    }
}