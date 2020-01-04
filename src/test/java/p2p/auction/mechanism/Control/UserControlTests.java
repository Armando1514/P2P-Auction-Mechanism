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

class UserControlTests {


    @BeforeAll
    static void initPeer() throws Exception {
        class MessageListenerImpl implements MessageListener {
            private int peerid;

            private MessageListenerImpl(int peerid) {
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
            e.printStackTrace();
        }
    }

    @Test
    void storeUserTest() {
        User x = new User();
        x.setNickname("storeUser");
        UserMechanism.storeUser(x);
        assertNotNull(UserMechanism.findUser("storeUser"));
    }

    @Test
    void updateUserTest() {
        User x = new User();
        x.setNickname("storeUser");
        x.setPassword("ola");
        UserMechanism.storeUser(x);
        x.setPassword("ina");
        UserMechanism.updateUser(x);
        assertEquals(UserMechanism.findUser("storeUser").getPassword(), "ina");
    }



}