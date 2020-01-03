package p2p.auction.mechanism.DAO;

import net.tomp2p.peers.PeerAddress;
import p2p.auction.mechanism.Control.AuctionMechanism;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Auction implements Serializable {

    private Integer id;
    private User owner;
    private String auctionName;
    private Double fastPrice;
    private HashMap<String,PeerAddress> participants;
    private Date expirationDate;
    private ArrayList<AuctionBid> slots;
    private Date creationDate;
    private AuctionStatus status;




    public enum AuctionStatus {
        ENDED,
        ONGOING;
    }


    public Auction()
    {
        participants = new HashMap<String, PeerAddress>();
        this.slots = new ArrayList<AuctionBid>();
        this.creationDate = new Date();
        this.status=AuctionStatus.ONGOING;

    }

    public Auction(User owner, String auctionName, Date expirationDate, double fastPrice)
    {
        this.id = id;
        this.owner = owner;
        this.fastPrice = fastPrice;
        this.auctionName = auctionName;
        this.setExpirationDate(expirationDate);
        this.slots = new ArrayList<AuctionBid>();
        this.creationDate = new Date();
        Date currentDate = new Date();
        participants = new HashMap<String,PeerAddress>();
    }

    public Date getCreationDate() {
        return creationDate;
    }


    public Auction updateElements(Auction newAuction)
    {

        this.setAuctionName(newAuction.getAuctionName());
        this.setFastPrice(newAuction.getFastPrice());
        if(newAuction.getStatus() == AuctionStatus.ENDED)
        this.setStatus(newAuction.getStatus());

        if(!this.getParticipants().isEmpty())
        {
            if(!newAuction.getParticipants().isEmpty())
            {
                HashMap<String, PeerAddress> lastParticipants = this.getParticipants();
                HashMap<String, PeerAddress> newParticipants = newAuction.getParticipants();
                Iterator<String> it = newParticipants.keySet().iterator();
                while (it.hasNext())
                {

                    String usersId = it.next();
                    if(!lastParticipants.containsKey(usersId))
                        lastParticipants.put(usersId, newParticipants.get(usersId));

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




    public String getAuctionName() {
        return this.auctionName;
    }

    public void setAuctionName(String auctionName) {
        this.auctionName = auctionName;
    }

    public HashMap<String, PeerAddress> getParticipants() {
        return this.participants;
    }

    public void setParticipants(String userId,PeerAddress participants) {
        this.participants.put(userId,participants);
    }
    private void setParticipants(HashMap<String, PeerAddress> participants) {
        this.participants = participants;
    }
    public Date getExpirationDate() {
        return this.expirationDate;
    }

    public Double getFastPrice() {
        return fastPrice;
    }

    public void setFastPrice(Double fastPrice) {
        if(fastPrice == null || fastPrice == -1)
            this.fastPrice = null;
        else
        this.fastPrice = fastPrice;
    }

    public void setExpirationDate(Date expirationDate) {

        this.expirationDate = expirationDate;
        checkStatus();

    }
    public boolean checkStatus()
    {
        if(this.status == AuctionStatus.ENDED) {
            return false;
        }
        else {
            if (this.expirationDate != null) {
                Date currentDate = new Date();
                if (currentDate.after(expirationDate)) {

                    this.status = AuctionStatus.ENDED;
                    if(this.getId() != null) {
                        String message = "- The auction: " + this.getAuctionName() + "(id: " + this.getId() + "), is over.";

                        try {
                            AuctionMechanism.noticePeers(this, message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    return false;
                } else
                    this.status = AuctionStatus.ONGOING;

                return true;
            }
        }
        return true;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public ArrayList<AuctionBid> getSlots() {
        return this.slots;
    }


    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }
}
