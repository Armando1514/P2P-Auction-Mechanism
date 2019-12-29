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

    private static final int NUMBER_OF_PEERS = 10;

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
                    Auction auctionTest = new Auction(user, "test-"+finalI, new Date(), new Double(1));

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
        HashMap<Integer, Auction> auctions = peers[0].readAll();

        assertEquals(NUMBER_OF_PEERS, auctions.size() );

    }

    //check if all the bids  created in parallel from different peers, are in the right order.
    @Test
    protected void testUpdate() throws Exception {

        User user = new User("userTestUpdate","password", new Double(1), null, null);
        Auction auctionTest = new Auction(user, "testUpdate", new Date(),new Double(1));
        auctionTest = peers[0].create(auctionTest);
        int i = 0;
        while( i < NUMBER_OF_PEERS ) {

            Auction finalAuctionTest = auctionTest;
            new Thread(new Runnable() {
                @Override
                public void run() {

                    Random random = new Random();

                    int rnd = random.ints(1,(NUMBER_OF_PEERS)).findFirst().getAsInt();
                    User user = new User("test"+rnd,"password", new Double(1), null, null);

                    AuctionBid bid = new AuctionBid(finalAuctionTest, user, new Double(rnd));
                    try {
                        peers[rnd].update(finalAuctionTest, bid);
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
            auctionSlot = peers[0].read(auctionTest.getId()).getSlots();
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
        Auction auctionTest = new Auction(user, "testReadLastBid", new Date(), new Double(1));
        auctionTest = peers[0].create(auctionTest);

        AuctionBid bid = new AuctionBid(auctionTest, user, new Double(3));
        peers[0].update(auctionTest, bid);
        peers[0].read(auctionTest.getId());
        int size = peers[0].read(auctionTest.getId()).getSlots().size();
        AuctionBid bidLast = peers[0].read(auctionTest.getId()).getSlots().get(size - 1);
        assertEquals(bidLast.getBidValue(), new Double(3));

    }

    @Test
    protected void testReadEmptyAuction () throws Exception {

        assertNull(peers[0].read(Integer.MAX_VALUE));

    }
static int ia = 0;
    // we need to execute it at the end.
    @AfterEach
     void testDelete() throws Exception {
        HashMap<Integer, Auction> auctions = peers[0].readAll();
        ia++;
        for (Integer key: auctions.keySet()) {
            System.out.println(key);
            peers[0].delete(key);
        }
    }





}
