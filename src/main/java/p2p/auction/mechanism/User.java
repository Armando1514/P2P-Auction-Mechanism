package p2p.auction.mechanism;

import java.io.Serializable;

public class User implements Serializable {
    private String nickname;
    private String password;
    private Double money;
    private String latestAuctionsJoined;
    private String myAuctions;

    public User(String nickname, String password, Double money, String latestAuctionJoined, String myAuctions) {
        this.nickname = nickname;
        this.password = password;
        this.money = money;
        this.latestAuctionsJoined = latestAuctionJoined;
        this.myAuctions = myAuctions;
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

    public String getMyAuctions() {
        return myAuctions;
    }

    public void setMyAuctions(String myAuctions) {
        this.myAuctions = myAuctions;
    }
}
