package p2p.auction.mechanism.DAO;

public interface DAOFactory {
        AuctionDAO getAuctionDAO();
        UserDAO getUserDAO();
}
