package p2p.auction.mechanism.Control;

import p2p.auction.mechanism.DAO.Auction;
import p2p.auction.mechanism.DAO.AuctionDAO;
import p2p.auction.mechanism.DAO.AuctionMechanismDAOFactory;

public class AuctionControl implements AuctionMechanism{

    public boolean createAuction(Auction auction)
    {
        AuctionDAO auctionDAO = AuctionMechanismDAOFactory.getInstance().getAuctionDAO();
        try {
            auctionDAO.create(auction);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
