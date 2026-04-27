package webprak.DAO.impl;

import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import webprak.DAO.ProfileDAO;
import webprak.models.Client;
import webprak.models.Profile;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ProfileDAOImpl extends CommonDAOImpl<Profile, Long> implements ProfileDAO {

    public ProfileDAOImpl() {
        super(Profile.class);
    }

    @Override
    @Transactional
    public Long createProfile(Long clientId, String name, String phone, String other) {
        Long count = (Long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM profiles WHERE phone = :phone")
                .setParameter("phone", phone)
                .getSingleResult();
        if (count > 0) return null;

        Client client = entityManager.find(Client.class, clientId);

        Profile profile = new Profile(null, client, name, phone, 0.0, other, LocalDateTime.now());
        save(profile);
        return profile.getId();
    }

    @Override
    @Transactional
    public void updateProfile(Long profileId, String name, String phone, String other) {
        Profile profile = getById(profileId);
        if (phone != null && !phone.equals(profile.getPhone())) {
            Long count = (Long) entityManager.createNativeQuery(
                            "SELECT COUNT(*) FROM profiles WHERE phone = :phone AND profile_id != :id")
                    .setParameter("phone", phone)
                    .setParameter("id", profileId)
                    .getSingleResult();
            if (count > 0) throw new IllegalArgumentException("Phone already used");
            profile.setPhone(phone);
        }
        if (name != null) profile.setName(name);
        if (other != null) profile.setOther(other);
        update(profile);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> getAllProfiles(int pageNum, int pageSize, String sortBy, boolean ascending, String searchTerm) {
        String orderDirection = ascending ? "ASC" : "DESC";
        String orderField = switch (sortBy) {
            case "balance" -> " p.balance";
            case "name" -> " p.name";
            case "created_at" -> " p.created_at";
            case "phone" -> " p.phone";
            case "client_id" -> " c.client_id";
            default -> " p.profile_id";
        };
        String whereClause = "";
        if (searchTerm != null && !searchTerm.isEmpty()) {
            whereClause = "WHERE p.name ILIKE :search OR p.phone ILIKE :search ";
        }
        String sql = """
            SELECT p.profile_id, p.name, p.phone, p.balance, p.other, p.created_at,
                   c.client_id
            FROM profiles p
            JOIN clients c ON p.client_id = c.client_id
            """ + whereClause + """
            ORDER BY""" + orderField + " " + orderDirection;

        Query query = entityManager.createNativeQuery(sql);
        if (searchTerm != null && !searchTerm.isEmpty()) {
            query.setParameter("search", "%" + searchTerm + "%");
        }
        query.setFirstResult(pageNum * pageSize);
        query.setMaxResults(pageSize);
        return query.getResultList();
    }

    @Override
    @Transactional
    public Double updateBalance(Long profileId, Double delta) {
        Profile profile = getById(profileId);
        if (profile.getBalance() + delta < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        profile.setBalance(profile.getBalance() + delta);
        return profile.getBalance();
    }

    @Override
    public boolean verifyBalanceConsistency(Long profileId) {
        Profile profile = getById(profileId);
        Double sumOps = (Double) entityManager.createNativeQuery(
                        "SELECT COALESCE(SUM(balance_change), 0) FROM operations WHERE profile_id = :pid")
                .setParameter("pid", profileId)
                .getSingleResult();
        double diff = profile.getBalance() - sumOps;
        return Math.abs(diff) < 1e-9;
    }

    @Override
    public long countProfiles(String searchTerm) {
        String sql = "SELECT COUNT(*) FROM profiles p JOIN clients c ON p.client_id = c.client_id ";
        if (searchTerm != null && !searchTerm.isEmpty()) {
            sql += "WHERE p.name ILIKE :search OR p.phone ILIKE :search";
        }
        Query query = entityManager.createNativeQuery(sql);
        if (searchTerm != null && !searchTerm.isEmpty()) {
            query.setParameter("search", "%" + searchTerm + "%");
        }
        return ((Number) query.getSingleResult()).longValue();
    }
}