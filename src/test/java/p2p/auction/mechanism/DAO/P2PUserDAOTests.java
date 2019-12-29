package p2p.auction.mechanism.DAO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import p2p.auction.mechanism.Auction;
import p2p.auction.mechanism.MessageListener;
import p2p.auction.mechanism.User;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class P2PUserDAOTests {
    private static final int NUMBER_OF_PEERS = 5;

    private static UserDAO[] peers;

    private static CountDownLatch c3 = new CountDownLatch(NUMBER_OF_PEERS);


    @BeforeAll
    static void initPeer() throws Exception {
        peers = new UserDAO[NUMBER_OF_PEERS];

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

            int i = 0;

            while (i < NUMBER_OF_PEERS) {

                peers[i] = AuctionMechanismDAOFactory.instantiate(i, "127.0.0.1", new MessageListenerImpl(i), true).getUserDAO();
                i++;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //check if all the user  created in parallel from different peers are without duplication.
    @Test
    protected void testUpdateGetAll() throws Exception {

        String userSaved[] = new String[NUMBER_OF_PEERS];

        int i = 0;
        while (i < NUMBER_OF_PEERS) {

            int duplicateN = i % (NUMBER_OF_PEERS / 2);
            userSaved[i] = "user" + duplicateN;

            new Thread(new Runnable() {
                @Override
                public void run() {

                    Random random = new Random();

                    int rnd = random.ints(1, (NUMBER_OF_PEERS)).findFirst().getAsInt();
                    User user = new User("user" + duplicateN, "password", new Double(1), null, null);
                    try {
                        peers[rnd].create(user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    c3.countDown();

                }
            }).start();

            i++;
        }
        c3.await();

        // wait until all the threads are done


        //check if all the bids are in dec order.
        for (i = 0; i < NUMBER_OF_PEERS; i++) {
            System.out.println(userSaved[i]);
            assertNotNull(peers[0].read(userSaved[i]));

        }

    }
}
