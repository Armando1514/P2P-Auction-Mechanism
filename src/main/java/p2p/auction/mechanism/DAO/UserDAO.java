package p2p.auction.mechanism.DAO;

public interface UserDAO extends DAOTools {
    User read(String nickname) throws Exception;
    void create(User user) throws Exception;
    void update(User user) throws Exception;

}