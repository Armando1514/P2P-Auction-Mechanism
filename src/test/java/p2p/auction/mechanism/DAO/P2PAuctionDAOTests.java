package p2p.auction.mechanism.DAO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.*;
import p2p.auction.mechanism.*;


import static org.junit.jupiter.api.Assertions.*;


public class P2PAuctionDAOTests {

    private static final int NUMBER_OF_PEERS = 5;

    private static AuctionDAO[] peers;

    private static CountDownLatch cl  ;
    private static CountDownLatch c2  ;


    @BeforeAll
    static void initPeer() throws Exception
    {
        peers = new AuctionDAO[NUMBER_OF_PEERS];

        class MessageListenerImpl implements MessageListener {
            int peerid;
            public MessageListenerImpl(int peerid)
            {
                this.peerid=peerid;
            }
            public Object parseMessage(Object obj) {
                System.out.println(peerid+"] (Direct Message Received) "+obj);
                return "success";
            }

        }

        try {
            cl = new CountDownLatch(NUMBER_OF_PEERS) ;
            c2 = new CountDownLatch(NUMBER_OF_PEERS) ;

            int i = 0;

            while( i < NUMBER_OF_PEERS)
            {

                peers[i] = AuctionMechanismDAOFactory.instantiate(i, "127.0.0.1", new MessageListenerImpl(i), true).getAuctionDAO();
                i++;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        }

    //check if all the auction  created in parallel from different peers are in the hash map.
    @Test
    protected void testUpdateGetAll() throws Exception {

        int i = 0;
        while( i < NUMBER_OF_PEERS ) {

            int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {

                    Random random = new Random();

                    int rnd = random.ints(1,(NUMBER_OF_PEERS)).findFirst().getAsInt();
                    User user = new User("user"+finalI,"password", new Double(1), null, null);
                    Auction auctionTest = new Auction(user, "test-"+finalI, null, new Date());

                    try {
                        peers[rnd].create(auctionTest);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    c2.countDown();

                }
            }).start();

            i ++;
        }
        c2.await();

        // wait until all the threads are done


        i = 0;

        //check if all the bids are in dec order.
        HashMap<String, Auction> auctions = peers[0].readAll();

        assertEquals(NUMBER_OF_PEERS, auctions.size() );

    }

    //check if all the bids  created in parallel from different peers, are in the right order.
    @Test
    protected void testUpdate() throws Exception {

        User user = new User("userTestUpdate","password", new Double(1), null, null);
        Auction auctionTest = new Auction(user, "testUpdate", null, new Date());
        peers[0].create(auctionTest);
        int i = 0;
        while( i < NUMBER_OF_PEERS ) {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    Random random = new Random();

                    int rnd = random.ints(1,(NUMBER_OF_PEERS)).findFirst().getAsInt();
                    User user = new User("test"+rnd,"password", new Double(1), null, null);

                    AuctionBid bid = new AuctionBid(auctionTest, user, new Double(rnd));
                    try {
                        peers[rnd].update(auctionTest, bid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    cl.countDown();

                }
            }).start();

            i ++;
        }
        cl.await();

            // wait until all the threads are done


        i = 0;

        //check if all the bids are in dec order.
        ArrayList<AuctionBid> auctionSlot = null;
        try {
            auctionSlot = peers[0].read("testUpdate").getSlots();
        } catch (Exception e) {
            e.printStackTrace();
        }

        while( i < auctionSlot.size())
            {

                Double pivot =  auctionSlot.get(i).getBidValue();
                int j = i + 1;


                while(auctionSlot.size() > j)
                {
                    Double tmp = auctionSlot.get(j).getBidValue();


                    assertFalse(tmp < pivot, "The elements are not in the right order. P2PAuctionCRUDTests." );
                    j++;
                }
                i++;
            }



    }



    @Test
    protected void testReadLastBid () throws Exception {

        User user = new User("userReadLastBid","password", new Double(1), null, null);
        Auction auctionTest = new Auction(user, "testReadLastBid", null, new Date());
        peers[0].create(auctionTest);

        AuctionBid bid = new AuctionBid(auctionTest, user, new Double(3));
        peers[0].update(auctionTest, bid);
        peers[0].read("testReadLastBid");
        int size = peers[0].read("testReadLastBid").getSlots().size();
        AuctionBid bidLast = peers[0].read("testReadLastBid").getSlots().get(size - 1);
        assertEquals(bidLast.getBidValue(), new Double(3));

    }

    @Test
    protected void testReadEmptyAuction () throws Exception {

        assertNull(peers[0].read("Empty Test"));

    }
static int ia = 0;
    // we need to execute it at the end.
    @AfterEach
     void testDelete() throws Exception {
        HashMap<String, Auction> auctions = peers[0].readAll();
        ia++;
        for (String key: auctions.keySet()) {
            peers[0].delete(key);
        }
    }





}
