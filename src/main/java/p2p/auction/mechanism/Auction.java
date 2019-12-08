package p2p.auction.mechanism;

import net.tomp2p.peers.PeerAddress;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

public class Auction implements Serializable {

    private AuctionUser owner;
    private String auctionName;
    private HashSet<PeerAddress> participants;
    private Date expirationDate;
    private ArrayList<AuctionBid> slots;

    public Auction(AuctionUser owner, String auctionName, HashSet<PeerAddress> participants, Date expirationDate)
    {
        this.owner = owner;
        this.auctionName = auctionName;
        this.setParticipants(participants);
        this.setExpirationDate(expirationDate);
        this.slots = new ArrayList<AuctionBid>();
    }

    public AuctionUser getOwner() {
        return this.owner;
    }

    public Auction setOwner(AuctionUser owner) {
        this.owner = owner;
        return this;
    }

    public void setNickname(AuctionUser owner) {
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


    public ArrayList<AuctionBid> getSlots() {
        return this.slots;
    }

    public void setSlot(AuctionBid bid) {
        this.slots.add(bid);
    }
}
