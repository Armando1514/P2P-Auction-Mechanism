package p2p.auction.mechanism.DAO;

import net.tomp2p.peers.PeerAddress;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

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
        participants = new HashSet<PeerAddress>();
    }




    public Auction updateElements(Auction newAuction)
    {

        this.setAuctionName(newAuction.getAuctionName());
        this.setFastPrice(newAuction.getFastPrice());

        if(!this.getParticipants().isEmpty())
        {
            if(!newAuction.getParticipants().isEmpty())
            {
                HashSet<PeerAddress> lastParticipants = this.getParticipants();
                HashSet<PeerAddress> newParticipants = newAuction.getParticipants();
                Iterator<PeerAddress> it = newParticipants.iterator();
                while (it.hasNext())
                {

                    PeerAddress address = it.next();
                    if(!lastParticipants.contains(address))
                        lastParticipants.add(address);

                }
            }
        }
        else {
            this.setParticipants(newAuction.getParticipants());
        }

        return this;
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

    public double getFastPrice() {
        return fastPrice;
    }

    public void setFastPrice(double fastPrice) {
        this.fastPrice = fastPrice;
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
