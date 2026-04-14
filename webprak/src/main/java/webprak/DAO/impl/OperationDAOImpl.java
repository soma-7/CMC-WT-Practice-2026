package webprak.DAO.impl;

import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import webprak.DAO.OperationDAO;
import webprak.models.Service;
import webprak.models.Profile;
import webprak.models.Operation;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class OperationDAOImpl extends CommonDAOImpl<Operation, Long> implements OperationDAO {

    public OperationDAOImpl() {
        super(Operation.class);
    }

    @Override
    @Transactional
    public Long createOperation(Long profileId, String type, Long serviceId, Double balanceChange, String description) {
        boolean requiresService = !("deposit".equals(type) || "withdrawal".equals(type));
        if (requiresService && serviceId == null) {
            return null;
        }
        String updateSql = "UPDATE profiles SET balance = balance + :delta WHERE profile_id = :pid AND balance + :delta >= 0";
        int updated = entityManager.createNativeQuery(updateSql)
                .setParameter("delta", balanceChange)
                .setParameter("pid", profileId)
                .executeUpdate();
        if (updated == 0) throw new IllegalStateException("Insufficient balance or profile not found");

        Operation.OperationType opType;
        switch (type) {
            case "deposit": opType = Operation.OperationType.deposit; break;
            case "withdrawal": opType = Operation.OperationType.withdrawal; break;
            case "service_payment": opType = Operation.OperationType.service_payment; break;
            case "service_purchase": opType = Operation.OperationType.service_purchase; break;
            case "service_cancellation": opType = Operation.OperationType.service_cancellation; break;
            default: return null;
        }
        Profile profile = entityManager.find(Profile.class, profileId);
        Service service = null;
        if (requiresService) service = entityManager.find(Service.class, serviceId);
        Operation operation = new Operation(null, profile, opType, LocalDateTime.now(), service, balanceChange, description);
        save(operation);
        profile.setBalance(profile.getBalance() + balanceChange);
        return operation.getId();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> getAllOperations(int pageNum, int pageSize, Long profileId, Long clientId, String type,
                                           Long serviceId, LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder("""
        SELECT o.operation_id, o.type, o.timestamp, o.balance_change, o.description,
               p.profile_id, p.name, p.phone,
               s.service_id, s.name
        FROM operations o
        JOIN profiles p ON o.profile_id = p.profile_id
        LEFT JOIN services s ON o.service_id = s.service_id
        WHERE 1=1
    """);
        if (profileId != null) sql.append(" AND o.profile_id = :profileId");
        if (clientId != null) sql.append(" AND p.client_id = :clientId");
        if (type != null) sql.append(" AND o.type = CAST(:type AS operation_type)");
        if (serviceId != null) sql.append(" AND o.service_id = :serviceId");
        if (from != null) sql.append(" AND o.timestamp >= :from");
        if (to != null) sql.append(" AND o.timestamp <= :to");
        sql.append(" ORDER BY o.timestamp DESC");

        Query query = entityManager.createNativeQuery(sql.toString());
        if (profileId != null) query.setParameter("profileId", profileId);
        if (clientId != null) query.setParameter("clientId", clientId);
        if (type != null) query.setParameter("type", type);
        if (serviceId != null) query.setParameter("serviceId", serviceId);
        if (from != null) query.setParameter("from", from);
        if (to != null) query.setParameter("to", to);
        query.setFirstResult(pageNum * pageSize);
        query.setMaxResults(pageSize);
        return query.getResultList();
    }
}