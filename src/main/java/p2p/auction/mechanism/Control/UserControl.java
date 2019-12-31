package p2p.auction.mechanism.Control;

import p2p.auction.mechanism.DAO.AuctionMechanismDAOFactory;
import p2p.auction.mechanism.DAO.User;
import p2p.auction.mechanism.DAO.UserDAO;
import p2p.auction.mechanism.DAO.UsernameExistsException;

public class UserControl implements UserMechanism {

    public boolean storeUser(User user)
    {
        UserDAO userDAO = AuctionMechanismDAOFactory.getInstance().getUserDAO();
        try {
            userDAO.create(user);
        } catch (UsernameExistsException e) {
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean updateUser(User user)
    {
        UserDAO userDAO = AuctionMechanismDAOFactory.getInstance().getUserDAO();

        try {
            userDAO.update(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public User findUser(String nickname)
    {
        UserDAO userDAO = AuctionMechanismDAOFactory.getInstance().getUserDAO();
        try {
            return userDAO.read(nickname);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
