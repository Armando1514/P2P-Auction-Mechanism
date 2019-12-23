package p2p.auction.mechanism.DAO;

import p2p.auction.mechanism.Auction;
import p2p.auction.mechanism.AuctionBid;
import p2p.auction.mechanism.User;

public interface UserDAO {
    User read(String nickname) throws Exception;
    void create(User user) throws Exception;
}
