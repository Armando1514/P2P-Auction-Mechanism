package p2p.auction.mechanism.Control;

import p2p.auction.mechanism.DAO.*;

public interface UserMechanism {

    static boolean storeUser(User user)
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

    static void changeUserAddress(User user)
    {
        int i = 0;
        while(i < user.getAuctionsJoined().size())
        {
            Auction auction = user.getAuctionsJoined().get(i);
            if(!(auction.getStatus() == Auction.AuctionStatus.ENDED))
            {
                auction.getParticipants().put(user.getNickname(), AuctionMechanismDAOFactory.getInstance().getPeerAddress());
            }
        }
        i = 0;
        while(i < user.getMyAuctions().size())
        {
            Auction auction = user.getMyAuctions().get(i);
            if(!(auction.getStatus() == Auction.AuctionStatus.ENDED))
            {
                auction.getParticipants().put(user.getNickname(), AuctionMechanismDAOFactory.getInstance().getPeerAddress());
            }
        }
    }

    static boolean updateUser(User user)
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

    static User findUser(String nickname)
    {
        UserDAO userDAO = AuctionMechanismDAOFactory.getInstance().getUserDAO();
        try {
            return userDAO.read(nickname);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }}
