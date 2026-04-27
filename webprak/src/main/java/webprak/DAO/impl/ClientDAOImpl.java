package webprak.DAO.impl;

import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import webprak.DAO.ClientDAO;
import webprak.models.Client;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ClientDAOImpl extends CommonDAOImpl<Client, Long> implements ClientDAO {

    public ClientDAOImpl() {
        super(Client.class);
    }

    @Override
    @Transactional
    public Long createClient(String info) {
        Client client = new Client(null, info, LocalDateTime.now());
        save(client);
        return client.getId();
    }

    @Override
    @Transactional
    public void updateClientInfo(Long clientId, String newInfo) {
        Client client = getById(clientId);
        if (client != null) {
            client.setInfo(newInfo);
            update(client);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> getAllClients(int pageNum, int pageSize, String sortBy, boolean ascending, String search) {
        String orderDirection = ascending ? "ASC" : "DESC";
        String orderField;
        switch (sortBy) {
            case "totalBalance":     orderField = " total_balance"; break;
            case "registrationDate": orderField = " c.registration_date"; break;
            default:                 orderField = " c.client_id";
        }

        String whereSearch = "";
        if (search != null && !search.isEmpty()) {
            whereSearch = " WHERE CAST(c.client_id AS TEXT) ILIKE :search ";
        }

        String sql = """
                SELECT c.client_id as client_id,
                       c.registration_date as registration_date,
                       COUNT(p.profile_id) as profile_count,
                       COALESCE(SUM(p.balance), 0) as total_balance
                FROM clients c
                LEFT JOIN profiles p ON c.client_id = p.client_id
                """ + whereSearch +
                " GROUP BY c.client_id ORDER BY " + orderField + " " + orderDirection;

        Query nativeQuery = entityManager.createNativeQuery(sql);
        if (search != null && !search.isEmpty()) {
            nativeQuery.setParameter("search", "%" + search + "%");
        }
        nativeQuery.setFirstResult(pageNum * pageSize);
        nativeQuery.setMaxResults(pageSize);
        return nativeQuery.getResultList();
    }

    @Override
    public long countClients(String search) {
        String whereSearch = "";
        if (search != null && !search.isEmpty()) {
            whereSearch = " WHERE CAST(c.client_id AS TEXT) ILIKE :search ";
        }
        String sql = """
                SELECT COUNT(DISTINCT c.client_id)
                FROM clients c
                LEFT JOIN profiles p ON c.client_id = p.client_id
                """ + whereSearch;
        Query nativeQuery = entityManager.createNativeQuery(sql);
        if (search != null && !search.isEmpty()) {
            nativeQuery.setParameter("search", "%" + search + "%");
        }
        return ((Number) nativeQuery.getSingleResult()).longValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> getClientsByRegistrationDateRange(int pageNum, int pageSize, LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT c.client_id as client_id,
                   c.registration_date as registration_date,
                   COUNT(p.profile_id) as profile_count,
                   COALESCE(SUM(p.balance), 0) as total_balance
            FROM clients c
            LEFT JOIN profiles p ON c.client_id = p.client_id
            WHERE c.registration_date BETWEEN :start AND :end
            GROUP BY c.registration_date, c.client_id
            ORDER BY c.registration_date
        """;
        Query nativeQuery = entityManager.createNativeQuery(sql);
        nativeQuery.setParameter("start", start);
        nativeQuery.setParameter("end", end);
        nativeQuery.setFirstResult(pageNum * pageSize);
        nativeQuery.setMaxResults(pageSize);
        return nativeQuery.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> getClientWithProfiles(Long clientId) {
        String sql = """
            SELECT c.client_id,
                   c.registration_date,
                   c.info,
                   p.profile_id,
                   p.name,
                   p.phone,
                   p.balance,
                   p.other,
                   p.created_at
            FROM clients c
            LEFT JOIN profiles p ON c.client_id = p.client_id
            WHERE c.client_id = :clientId
            ORDER BY p.profile_id
        """;
        Query nativeQuery = entityManager.createNativeQuery(sql);
        nativeQuery.setParameter("clientId", clientId);
        return nativeQuery.getResultList();
    }
}