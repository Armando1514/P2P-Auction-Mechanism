package p2p.auction.mechanism.DAO;

import p2p.auction.mechanism.Auction;
import p2p.auction.mechanism.AuctionBid;

import java.io.IOException;
import java.util.HashMap;

public interface AuctionDAO {
    Auction read(String auction_name) throws Exception;
    HashMap<String, Auction> readAll() throws IOException, ClassNotFoundException;
    void delete(String auction_name) throws Exception;
    void create(Auction auction) throws Exception;
    void update(Auction auction, AuctionBid newBid) throws Exception;
}
