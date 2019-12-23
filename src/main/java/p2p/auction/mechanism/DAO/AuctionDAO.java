package p2p.auction.mechanism.DAO;

import p2p.auction.mechanism.Auction;
import p2p.auction.mechanism.AuctionBid;

public interface AuctionDAO {
    Auction read(String auction_name) throws Exception;
    void delete(String auction_name);
    void create(Auction auction) throws Exception;
    void update(Auction auction, AuctionBid newBid) throws Exception;
}
