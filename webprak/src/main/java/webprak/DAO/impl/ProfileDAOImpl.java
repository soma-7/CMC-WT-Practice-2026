package webprak.DAO.impl;

import org.springframework.stereotype.Repository;
import webprak.DAO.ProfileDAO;
import webprak.models.Profile;

@Repository
public class ProfileDAOImpl extends CommonDAOImpl<Profile, Long> implements ProfileDAO {
    public ProfileDAOImpl() {
        super(Profile.class);
    }
}