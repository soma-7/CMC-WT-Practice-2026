package webprak.DAO;

import webprak.models.Client;
import java.time.LocalDateTime;
import java.util.List;

public interface ClientDAO extends CommonDAO<Client, Long> {
    Long createClient(String info);
    void updateClientInfo(Long clientId, String newInfo);
    List<Object[]> getAllClients(int pageNum, int pageSize, String sortBy, boolean ascending);
    List<Object[]> getClientsByRegistrationDateRange(int pageNum, int pageSize, LocalDateTime start, LocalDateTime end);
    List<Object[]> getClientWithProfiles(Long clientId);
}