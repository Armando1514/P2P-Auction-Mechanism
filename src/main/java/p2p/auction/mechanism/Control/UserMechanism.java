package p2p.auction.mechanism.Control;

import p2p.auction.mechanism.DAO.User;

public interface UserMechanism {

    public boolean updateUser(User user);
    public boolean storeUser(User user);
    public User findUser(String nickname);
}
