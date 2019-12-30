package p2p.auction.mechanism;

import net.tomp2p.peers.PeerAddress;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class User implements Serializable {
    private String nickname;
    private String password;
    private Double money;
    private String latestAuctionsJoined;
    private String myAuctions;
    private ArrayList<String> unreadedMessages;

    public User(String nickname, String password, Double money, String latestAuctionJoined, String myAuctions) {
        this.nickname = nickname;
        this.password = password;
        this.money = money;
        this.latestAuctionsJoined = latestAuctionJoined;
        this.myAuctions = myAuctions;
        this.unreadedMessages = new ArrayList<>();
    }


    public String getLatestAuctionsJoined() {
        return latestAuctionsJoined;
    }

    public void setLatestAuctionsJoined(String latestAuctionsJoined) {
        this.latestAuctionsJoined = latestAuctionsJoined;
    }

    public Double getMoney() {
        return money;
    }

    public void setMoney(Double money) {
        this.money = money;
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

    public User setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public ArrayList<String> getUnreadedMessages() {
        return this.unreadedMessages;
    }

    public void setUnreadedMessages(ArrayList<String> unreadedMessages) {
        this.unreadedMessages = unreadedMessages;
    }
    public void setUnreadedMessages(String unreadedMessages) {
        this.unreadedMessages.add(unreadedMessages);
    }

    public String getMyAuctions() {
        return myAuctions;
    }

    public void setMyAuctions(String myAuctions) {
        this.myAuctions = myAuctions;
    }

    public User updateElements(User newUser)
    {
        this.password = newUser.password;
        this.money = newUser.money;
        this.latestAuctionsJoined = newUser.latestAuctionsJoined;
        this.myAuctions = newUser.myAuctions;

        if(!this.unreadedMessages.isEmpty())
        {
            if(!newUser.unreadedMessages.isEmpty())
            {

                ArrayList<String> unreaded = newUser.getUnreadedMessages();
                int i = 0;
                while (i < unreaded.size())
                {
                    String message = unreaded.get(i);
                    if(!this.unreadedMessages.contains(message))
                        this.unreadedMessages.add(message);
                    i++;

                }
            }
        }
        else {
            this.setUnreadedMessages(newUser.getUnreadedMessages());

        }

        return this;
    }

}
