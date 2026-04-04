package webprak.DAO.impl;

import org.springframework.stereotype.Repository;
import webprak.DAO.ClientDAO;
import webprak.models.Client;

@Repository
public class ClientDAOImpl extends CommonDAOImpl<Client, Long> implements ClientDAO {
    public ClientDAOImpl() {
        super(Client.class);
    }
}