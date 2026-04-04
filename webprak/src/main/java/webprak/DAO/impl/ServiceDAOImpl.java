package webprak.DAO.impl;

import org.springframework.stereotype.Repository;
import webprak.DAO.ServiceDAO;
import webprak.models.Service;

@Repository
public class ServiceDAOImpl extends CommonDAOImpl<Service, Long> implements ServiceDAO {
    public ServiceDAOImpl() {
        super(Service.class);
    }
}