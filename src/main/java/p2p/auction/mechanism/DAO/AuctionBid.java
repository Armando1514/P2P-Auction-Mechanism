package p2p.auction.mechanism.DAO;

import java.io.Serializable;

public class AuctionBid implements Serializable {
    private static final long serialVersionUID = 1L;
    private Auction auction;
    private User user;
    private double bidValue;
    public AuctionBid(Auction auction, User user, double bidValue) {
        this.setAuction(auction);
        this.setUser(user);
        this.setBidValue(bidValue);
    }

    boolean isSmallerThan(AuctionBid auctionBid) {
        return this.getBidValue() < auctionBid.getBidValue();
    }


    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getBidValue() {
        return bidValue;
    }

    void setBidValue(double bidValue) {
        this.bidValue = bidValue;
    }
}