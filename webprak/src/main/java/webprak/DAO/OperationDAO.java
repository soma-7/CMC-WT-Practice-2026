package webprak.DAO;

import webprak.models.Operation;
import java.time.LocalDateTime;
import java.util.List;

public interface OperationDAO extends CommonDAO<Operation, Long> {
    Long createOperation(Long profileId, String type, Long serviceId, Double balanceChange, String description);
    List<Object[]> getAllOperations(int pageNum, int pageSize, Long profileId, Long clientId, String type,
                                    Long serviceId, LocalDateTime from, LocalDateTime to);
}