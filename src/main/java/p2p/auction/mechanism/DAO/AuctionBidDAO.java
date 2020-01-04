package p2p.auction.mechanism.DAO;

public interface AuctionBidDAO extends DAOTools {
    void create(AuctionBid newBid) throws Exception;
}