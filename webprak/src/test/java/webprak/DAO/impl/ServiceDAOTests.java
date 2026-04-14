package webprak.DAO.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import webprak.DAO.AbstractDAOTests;
import webprak.DAO.ServiceDAO;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceDAOTest extends AbstractDAOTests {

    @Autowired
    private ServiceDAO serviceDAO;

    @Test
    void createService_ShouldReturnId() {
        Long sid = serviceDAO.createService("Internet", "Fast", "{\"speed\":\"100Mbps\"}", "{\"price\":10}");
        assertThat(sid).isPositive();
    }

    @Test
    void updateService_ShouldModifyFields() {
        Long sid = serviceDAO.createService("Old", "Desc", "{}", "{}");
        serviceDAO.updateService(sid, "New", "NewDesc", "{\"new\":true}", "{\"cost\":20}");
        List<Object[]> services = serviceDAO.getAllServices(0, 10, null, null, true, null);
        Object[] row = services.get(0);
        assertThat(row[1]).isEqualTo("New");
        assertThat(row[2]).isEqualTo("NewDesc");
    }

    @Test
    void getAllServices_ShouldFilterByTagKeyAndSearch() {
        serviceDAO.createService("A", "", "{\"sms\":\"100\"}", "{}");
        serviceDAO.createService("B", "", "{\"internet\":\"10GB\"}", "{}");
        serviceDAO.createService("C", "", "{\"sms\":\"50\"}", "{}");

        // key filter
        List<Object[]> withSms = serviceDAO.getAllServices(0, 10, "sms", null, true, null);
        assertThat(withSms).hasSize(2);

        // key=value filter
        List<Object[]> withSms100 = serviceDAO.getAllServices(0, 10, "sms=100", null, true, null);
        assertThat(withSms100).hasSize(1);
        assertThat(withSms100.get(0)[1]).isEqualTo("A");

        // value sort
        List<Object[]> sorted = serviceDAO.getAllServices(0, 10, null, "sms", true, null);
        assertThat(sorted.get(0)[1]).isEqualTo("A"); // "50" > "100" (as strings)

        // search
        List<Object[]> search = serviceDAO.getAllServices(0, 10, null, null, true, "internet");
        assertThat(search).hasSize(1);
        assertThat(search.get(0)[1]).isEqualTo("B");
    }
}