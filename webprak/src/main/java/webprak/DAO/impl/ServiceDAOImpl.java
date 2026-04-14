package webprak.DAO.impl;

import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import webprak.DAO.ServiceDAO;
import webprak.models.Service;

import java.util.List;

@Repository
public class ServiceDAOImpl extends CommonDAOImpl<Service, Long> implements ServiceDAO {

    public ServiceDAOImpl() {
        super(Service.class);
    }

    @Override
    @Transactional
    public Long createService(String name, String description, String includes, String other) {
        Service service = new Service(null, name, description, includes, other);
        save(service);
        return service.getId();
    }

    @Override
    @Transactional
    public void updateService(Long serviceId, String name, String description, String includes, String other) {
        Service service = getById(serviceId);
        if (service == null) return;
        if (name != null) service.setName(name);
        if (description != null) service.setDescription(description);
        if (includes != null) service.setIncludes(includes);
        if (other != null) service.setOther(other);
        update(service);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> getAllServices(int pageNum, int pageSize, String tagKey, String sortBy, boolean ascending, String search) {
        String orderDirection = ascending ? "ASC" : "DESC";
        String orderField;
        if (sortBy != null && !sortBy.isEmpty()) {
            orderField = "includes->>'" + sortBy.replace("'", "''") + "'";
        } else {
            orderField = "name";
        }

        String sql =
                """        
                    SELECT service_id, name, description, includes, other
                    FROM services
                    WHERE 1=1
                """ +
                        (tagKey != null && !tagKey.isEmpty() ?
                                (tagKey.contains("=") ?
                                 " AND includes->> :tagKeyEqualKey = :tagKeyEqualValue" :
                                 " AND jsonb_exists(includes, :tagKey)") : "") +
                        (search != null && !search.isEmpty() ?
                                " AND (name ILIKE :search OR description ILIKE :search OR CAST(includes AS text) ILIKE :search OR CAST(other AS text) ILIKE :search)" : "") +
                        " ORDER BY " + orderField + " " + orderDirection;

        Query nativeQuery = entityManager.createNativeQuery(sql);

        if (tagKey != null && !tagKey.isEmpty()) {
            if (tagKey.contains("=")) {
                String[] parts = tagKey.split("=", 2);
                nativeQuery.setParameter("tagKeyEqualKey", parts[0]);
                nativeQuery.setParameter("tagKeyEqualValue", parts[1]);
            } else {
                nativeQuery.setParameter("tagKey", tagKey);
            }
        }
        if (search != null && !search.isEmpty()) {
            nativeQuery.setParameter("search", "%" + search + "%");
        }

        nativeQuery.setFirstResult(pageNum * pageSize);
        nativeQuery.setMaxResults(pageSize);
        return nativeQuery.getResultList();
    }
}