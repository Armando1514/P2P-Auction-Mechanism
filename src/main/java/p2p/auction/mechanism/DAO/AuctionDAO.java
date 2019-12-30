package p2p.auction.mechanism.DAO;

import p2p.auction.mechanism.Auction;
import p2p.auction.mechanism.AuctionBid;
import p2p.auction.mechanism.User;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

public interface AuctionDAO extends  DAOTools {
    Auction read(Integer auction_id) throws Exception;
    HashMap<Integer, Auction> readAll() throws IOException, ClassNotFoundException;
    void delete(Integer auction_id) throws Exception;
    Auction create(Auction auction) throws Exception;
    void update(Auction newAuction) throws Exception;
}
