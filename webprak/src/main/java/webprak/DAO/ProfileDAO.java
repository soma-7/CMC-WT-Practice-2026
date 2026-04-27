package webprak.DAO;

import webprak.models.Profile;
import java.util.List;

public interface ProfileDAO extends CommonDAO<Profile, Long> {
    Long createProfile(Long clientId, String name, String phone, String other);
    void updateProfile(Long profileId, String name, String phone, String other);
    List<Object[]> getAllProfiles(int pageNum, int pageSize, String sortBy, boolean ascending, String searchTerm);
    Double updateBalance(Long profileId, Double delta);
    boolean verifyBalanceConsistency(Long profileId);
    long countProfiles(String searchTerm);
}