package webprak.DAO.impl;

import org.springframework.stereotype.Repository;
import webprak.DAO.OperationDAO;
import webprak.models.Operation;

@Repository
public class OperationDAOImpl extends CommonDAOImpl<Operation, Long> implements OperationDAO {
    public OperationDAOImpl() {
        super(Operation.class);
    }
}