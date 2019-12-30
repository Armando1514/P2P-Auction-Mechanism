package p2p.auction.mechanism.DAO;

import p2p.auction.mechanism.AuctionBid;

public interface AuctionBidDAO extends  DAOTools {
    void create(AuctionBid newBid) throws Exception;
}
