package p2p.auction.mechanism;

import net.tomp2p.peers.PeerAddress;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

public class Auction implements Serializable {

    private Integer id;
    private User owner;
    private String auctionName;
    private double fastPrice;
    private HashSet<PeerAddress> participants;
    private Date expirationDate;
    private ArrayList<AuctionBid> slots;
    private Date creationDate;

    public Auction(User owner, String auctionName, Date expirationDate, double fastPrice)
    {
        this.id = id;
        this.owner = owner;
        this.fastPrice = fastPrice;
        this.auctionName = auctionName;
        this.setExpirationDate(expirationDate);
        this.slots = new ArrayList<AuctionBid>();
        this.creationDate = new Date();
    }

    public User getOwner() {
        return this.owner;
    }

    public Auction setOwner(User owner) {
        this.owner = owner;
        return this;
    }

    public void setNickname(User owner) {
        this.owner = owner;
    }

    public String getAuctionName() {
        return this.auctionName;
    }

    public void setAuctionName(String auctionName) {
        this.auctionName = auctionName;
    }

    public HashSet<PeerAddress> getParticipants() {
        return this.participants;
    }

    public void setParticipants(HashSet<PeerAddress> participants) {
        this.participants = participants;
    }

    public Date getExpirationDate() {
        return this.expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<AuctionBid> getSlots() {
        return this.slots;
    }

    public void setSlot(AuctionBid bid) {
        this.slots.add(bid);
    }
}
