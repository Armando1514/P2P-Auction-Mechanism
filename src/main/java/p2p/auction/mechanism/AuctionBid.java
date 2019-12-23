package p2p.auction.mechanism;

import java.io.Serializable;

public class AuctionBid implements Serializable {

    private Auction auction;
    private User user;
    private double bidValue;
    public AuctionBid(Auction auction, User user, double bidValue)
    {
        this.setAuction(auction);
        this.setUser(user);
        this.setBidValue(bidValue);
    }

    public boolean isSmallerThan(AuctionBid auctionBid)
    {
        if(this.getBidValue() < auctionBid.getBidValue()) {
            System.out.println("true: "+ this.getBidValue()+ "  <  " + auctionBid.getBidValue());
            return true;
        }
        else {
            System.out.println("false: "+ this.getBidValue()+ "  <  " + auctionBid.getBidValue());

            return false;
        }
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

    public void setBidValue(double bidValue) {
        this.bidValue = bidValue;
    }
}
