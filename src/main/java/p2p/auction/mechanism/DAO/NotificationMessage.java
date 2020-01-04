package p2p.auction.mechanism.DAO;

import java.io.Serializable;

public class NotificationMessage implements Serializable {

    private static final long serialVersionUID = 1L;


    private String message;
    private AuctionBid bid;
    private MessageType type;


    public enum MessageType {
        WIN,
        BID
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }


    public AuctionBid getBid() {
        return bid;
    }

    public void setBid(AuctionBid bid) {
        this.bid = bid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}