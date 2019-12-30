package p2p.auction.mechanism.DAO;

import net.tomp2p.peers.PeerAddress;

public interface DAOFactory  {
         AuctionDAO getAuctionDAO();
         UserDAO getUserDAO();
         AuctionBidDAO getAuctionBidDAO();
         PeerAddress getPeerAddress();
}
