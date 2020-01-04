package p2p.auction.mechanism.DAO;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nickname;
    private String password;
    private ArrayList < Auction > auctionsJoined;
    private ArrayList < Auction > myAuctions;
    private ArrayList < String > unreadedMessages;
    private ArrayList < Auction > winnedBid;

    public User() {
        this.unreadedMessages = new ArrayList < > ();
        this.auctionsJoined = new ArrayList < > ();
        this.myAuctions = new ArrayList < > ();
        this.winnedBid = new ArrayList < > ();


    }

    public User(String nickname, String password) {
        this.nickname = nickname;
        this.password = password;
        this.auctionsJoined = new ArrayList < > ();
        this.myAuctions = new ArrayList < > ();
        this.winnedBid = new ArrayList < > ();
        this.unreadedMessages = new ArrayList < > ();
    }



    public ArrayList < Auction > getAuctionsJoined() {
        return auctionsJoined;
    }

    public void setAuctionsJoined(Auction latestAuctionsJoined) {
        this.auctionsJoined.add(latestAuctionsJoined);
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public ArrayList < String > getUnreadedMessages() {
        return this.unreadedMessages;
    }
    public ArrayList < Auction > getWinnedBid() {
        return this.winnedBid;
    }

    public void setUnreadedMessages(ArrayList < String > unreadedMessages) {
        this.unreadedMessages = unreadedMessages;
    }
    public void setUnreadedMessages(String unreadedMessages) {
        this.unreadedMessages.add(unreadedMessages);
    }

    private void setWinnedBid(ArrayList < Auction > auctions) {
        this.winnedBid = auctions;
    }
    public void setWinnedBid(Auction auction) {
        this.winnedBid.add(auction);
    }

    public ArrayList < Auction > getMyAuctions() {
        return myAuctions;
    }

    public void setMyAuctions(Auction myAuction) {

        this.myAuctions.add(myAuction);
    }

    User updateElements(User newUser) {
        this.password = newUser.getPassword();

        this.auctionsJoined = newUser.getAuctionsJoined();

        this.myAuctions = newUser.getMyAuctions();

        if (!this.unreadedMessages.isEmpty()) {
            if (!newUser.unreadedMessages.isEmpty()) {

                ArrayList < String > unreaded = newUser.getUnreadedMessages();
                int i = 0;
                while (i < unreaded.size()) {
                    String message = unreaded.get(i);
                    if (!this.unreadedMessages.contains(message))
                        this.unreadedMessages.add(message);
                    i++;

                }
            } else {
                this.setUnreadedMessages(newUser.getUnreadedMessages());

            }
        } else {
            if (!newUser.getUnreadedMessages().isEmpty())
                this.setUnreadedMessages(newUser.getUnreadedMessages());
        }

        if (!this.winnedBid.isEmpty()) {
            if (!newUser.winnedBid.isEmpty()) {

                ArrayList < Auction > win = newUser.getWinnedBid();
                int i = 0;
                while (i < win.size()) {
                    Auction auction = win.get(i);
                    if (!this.winnedBid.contains(auction))
                        this.winnedBid.add(auction);
                    i++;

                }
            }
        } else {
            if (!newUser.getWinnedBid().isEmpty())
                this.setWinnedBid(newUser.getWinnedBid());
        }


        return this;
    }

}