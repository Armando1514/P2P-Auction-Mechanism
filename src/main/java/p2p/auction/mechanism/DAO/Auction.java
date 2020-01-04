package p2p.auction.mechanism.DAO;

import net.tomp2p.peers.PeerAddress;
import p2p.auction.mechanism.AuctionApp;
import p2p.auction.mechanism.Control.AuctionMechanism;
import p2p.auction.mechanism.Control.UserMechanism;

import java.io.Serializable;
import java.util.*;

public class Auction implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private User owner;
    private String auctionName;
    private Double fastPrice;
    private HashMap < String, PeerAddress > participants;
    private ArrayList < AuctionBid > slots;
    private Calendar creationDate;
    private Calendar expirationDate;
    private AuctionStatus status;




    public enum AuctionStatus {
        ENDED,
        ONGOING
    }


    public Auction() {
        participants = new HashMap < > ();
        this.slots = new ArrayList < > ();
        TimeZone tz = TimeZone.getTimeZone(AuctionApp.getTimezone());
        Calendar cal = Calendar.getInstance(tz);
        cal.setTime(new Date());
        this.creationDate = cal;
        this.status = AuctionStatus.ONGOING;

    }

    public Auction(User owner, String auctionName, Date expirationDate, double fastPrice) {
        this.owner = owner;
        this.fastPrice = fastPrice;
        this.auctionName = auctionName;
        this.setExpirationDate(expirationDate);
        this.slots = new ArrayList < > ();
        TimeZone tz = TimeZone.getTimeZone(AuctionApp.getTimezone());
        Calendar cal = Calendar.getInstance(tz);
        cal.setTime(expirationDate);
        this.creationDate = cal;
        participants = new HashMap < > ();
    }

    public Calendar getCreationDate() {
        TimeZone tz = TimeZone.getTimeZone(AuctionApp.getTimezone());
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(tz);
        cal.setTime(this.creationDate.getTime());
        return cal;
    }


    Auction updateElements(Auction newAuction) {

        this.setAuctionName(newAuction.getAuctionName());
        this.setFastPrice(newAuction.getFastPrice());
        if (newAuction.getStatus() == AuctionStatus.ENDED)
            this.setStatus(newAuction.getStatus());

        if (!this.getParticipants().isEmpty()) {
            if (!newAuction.getParticipants().isEmpty()) {
                HashMap < String, PeerAddress > lastParticipants = this.getParticipants();
                HashMap < String, PeerAddress > newParticipants = newAuction.getParticipants();
                for (String usersId: newParticipants.keySet()) {

                    lastParticipants.put(usersId, newParticipants.get(usersId));

                }
            }
        } else {
            this.setParticipants(newAuction.getParticipants());
        }

        return this;
    }



    public User getOwner() {
        return this.owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }




    public String getAuctionName() {
        return this.auctionName;
    }

    public void setAuctionName(String auctionName) {
        this.auctionName = auctionName;
    }

    public HashMap < String, PeerAddress > getParticipants() {
        return this.participants;
    }

    public void setParticipants(String userId, PeerAddress participants) {
        this.participants.put(userId, participants);
    }
    private void setParticipants(HashMap < String, PeerAddress > participants) {
        this.participants = participants;
    }
    public Calendar getExpirationDate() {

        TimeZone tz = TimeZone.getTimeZone(AuctionApp.getTimezone());
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(tz);
        cal.setTime(this.expirationDate.getTime());
        return cal;
    }

    public Double getFastPrice() {
        return fastPrice;
    }

    public void setFastPrice(Double fastPrice) {
        if (fastPrice == null || fastPrice == -1)
            this.fastPrice = null;
        else
            this.fastPrice = fastPrice;
    }

    public void setExpirationDate(Date date) {

        TimeZone tz = TimeZone.getTimeZone("UTC");
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(tz);
        cal.setTime(date);
        this.expirationDate = cal;

        checkStatus();

    }
    public boolean checkStatus() {
        if (this.status == AuctionStatus.ENDED) {
            return false;
        } else {
            if (this.expirationDate != null) {
                Date currentDate = new Date();
                TimeZone tz = TimeZone.getTimeZone(AuctionApp.getTimezone());
                Calendar current = Calendar.getInstance(tz);
                current.setTime(currentDate);
                if (current.after(this.getExpirationDate())) {

                    this.status = AuctionStatus.ENDED;
                    if (this.getId() != null) {
                        if (!this.getSlots().isEmpty()) {
                            String message;

                            AuctionBid winnerBid = this.getSlots().get(this.getSlots().size() - 1);
                            User user = UserMechanism.findUser(winnerBid.getUser().getNickname());
                            double bidValue;
                            if (this.getSlots().size() > 1)
                                bidValue = this.getSlots().get(this.getSlots().size() - 2).getBidValue();
                            else
                                bidValue = winnerBid.getBidValue();

                            assert user != null;
                            message = "The auction: " + this.getAuctionName() + "(id: " + this.getId() + "), is over. The winner is: " + user.getNickname() + " , that has payed: " + bidValue + " $.";

                            user.setWinnedBid(this);
                            UserMechanism.updateUser(user);

                            NotificationMessage not = new NotificationMessage();
                            not.setBid(new AuctionBid(this, user, bidValue));
                            not.setMessage(message);
                            not.setType(NotificationMessage.MessageType.WIN);



                            AuctionMechanism.noticePeers(not);
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

    void setId(Integer id) {
        this.id = id;
    }


    public ArrayList < AuctionBid > getSlots() {
        return this.slots;
    }


    public AuctionStatus getStatus() {
        return status;
    }

    void setStatus(AuctionStatus status) {
        this.status = status;
    }
}