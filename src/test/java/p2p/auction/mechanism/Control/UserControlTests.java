package p2p.auction.mechanism.Control;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import p2p.auction.mechanism.Control.UserMechanism;
import p2p.auction.mechanism.DAO.AuctionMechanismDAOFactory;
import p2p.auction.mechanism.DAO.User;
import p2p.auction.mechanism.MessageListener;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class UserControlTests {


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

            AuctionMechanismDAOFactory.instantiate(0, "127.0.0.1", new MessageListenerImpl(0), true);


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void storeUserTest()
    {
        User x = new User();
        x.setNickname("storeUser");
        UserMechanism.storeUser(x);
        assertNotNull(UserMechanism.findUser("storeUser"));
    }

    @Test
    public void updateUserTest()
    {
        User x = new User();
        x.setNickname("storeUser");
        x.setMoney(new Double(4));
        UserMechanism.storeUser(x);
        x.setMoney(new Double(5));
        UserMechanism.updateUser(x);
        assertEquals(UserMechanism.findUser("storeUser").getMoney(),new Double(5));
    }



}
