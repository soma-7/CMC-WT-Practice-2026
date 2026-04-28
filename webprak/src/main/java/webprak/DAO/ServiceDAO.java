package webprak.DAO;

import webprak.models.Service;
import java.util.List;

public interface ServiceDAO extends CommonDAO<Service, Long> {
    Long createService(String name, String description, String includes, String other);
    void updateService(Long serviceId, String name, String description, String includes, String other);
    List<Object[]> getAllServices(int pageNum, int pageSize, String tagKey, String sortBy, boolean ascending, String search);
    long countServices(String tagKey, String search);
}