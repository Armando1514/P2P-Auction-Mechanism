package p2p.auction.mechanism.Control;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import p2p.auction.mechanism.Control.AuctionMechanism;

import p2p.auction.mechanism.DAO.*;
import p2p.auction.mechanism.MessageListener;

import java.io.IOException;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class AuctionControlTests {
    @BeforeAll
    static void initPeer() throws Exception {

        class MessageListenerImpl implements MessageListener {
            int peerid;

            public MessageListenerImpl(int peerid) {
                this.peerid = peerid;
            }

            public Object parseMessage(Object obj) {
                System.out.println(peerid + "] (Direct Message Received) " + obj);
                return "success";
            }

        }
        try {

            AuctionMechanismDAOFactory.instantiate(0, "127.0.0.1", new MessageListenerImpl(0), true).getAuctionDAO();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    @Test
    public void createAuctionTest()
    {
        Auction x = new Auction();
        User user = new User();
        x.setOwner(user);
        x.setAuctionName("createAuction");
        x = AuctionMechanism.createAuction(x);
        assertNotNull(AuctionMechanism.findAuction(x.getId()));
    }

    @Test
    public void findAuctionTest()
    {
        Auction x = new Auction();
        x.setAuctionName("createAuction");
        User user = new User();
        x.setOwner(user);
        x = AuctionMechanism.createAuction(x);
        assertNotNull(AuctionMechanism.findAuction(x.getId()));
    }

    @Test
    public void placeABidForEndedAuctionTest()
    {

        Auction x = new Auction();
        x.setAuctionName("placeABidForEndedAuctionTest");
        x.setExpirationDate(new Date());
        User user = new User();
        x.setOwner(user);
        x = AuctionMechanism.createAuction(x);


        user.setNickname("placeABidForEndedAuctionUser");
        AuctionBid bid = new AuctionBid(x, user, 4d);
        Assertions.assertThrows(AuctionEndedException.class, () -> {
            AuctionMechanism.placeABid(bid);
        });
    }


    /* should also update the auction status */
    @Test
    public void updateAuctionTest()
    {
        Auction x = new Auction();
        x.setAuctionName("updateAuctionTest");
        User user = new User();
        x.setOwner(user);
        x.setExpirationDate(new Date());
        x = AuctionMechanism.createAuction(x);
        x = AuctionMechanism.findAuction(x.getId());
        assertEquals(x.getStatus(), Auction.AuctionStatus.ENDED);
    }



}
