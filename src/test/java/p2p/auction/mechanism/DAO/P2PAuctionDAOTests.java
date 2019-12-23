package p2p.auction.mechanism.DAO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import p2p.auction.mechanism.*;


import static org.junit.jupiter.api.Assertions.*;


public class P2PAuctionDAOTests {

    private static final int NUMBER_OF_PEERS = 5;

    private static AuctionDAO[] peers;

    private static Auction auctionTest;

    private static CountDownLatch cl  ;


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
            int i = 0;

            while( i < NUMBER_OF_PEERS)
            {

                peers[i] = AuctionMechanismDAOFactory.instantiate(i, "127.0.0.1", new MessageListenerImpl(i), true).getAuctionDAO();
                i++;
            }


            User user = new User("test","password", new Double(1), null, null);

            auctionTest = new Auction(user, "Iphone X", null, new Date());


            peers[0].create(auctionTest);


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        }

    //check if all the bids  created in parallel from different peers, are in the right order.
    @Test
    protected void testCreateAndUpdate() throws Exception {

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
            auctionSlot = peers[0].read("Iphone X").getSlots();
        } catch (Exception e) {
            e.printStackTrace();
        }

        while( i < auctionSlot.size())
            {

                Double pivot =  auctionSlot.get(i).getBidValue();
                int j = i + 1;

                System.out.println(auctionSlot.size());

                while(auctionSlot.size() > j)
                {
                    Double tmp = auctionSlot.get(j).getBidValue();

                    System.out.println("tmp: " + tmp + " >  pivot: "+ pivot);

                    assertFalse(tmp < pivot, "The elements are not in the right order. P2PAuctionCRUDTests." );
                    j++;
                }
                i++;
            }



    }


    @Test
    protected void testRead () throws Exception {

        assertNotNull(peers[0].read("Iphone X"));

    }

    @Test
    protected void testReadLastBid () throws Exception {

        User user = new User("test","password", new Double(1), null, null);

        AuctionBid bid = new AuctionBid(auctionTest, user, new Double(3));
        peers[0].update(auctionTest, bid);
        peers[0].read("Iphone X");
        int size = peers[0].read("Iphone X").getSlots().size();
        AuctionBid bidLast = peers[0].read("Iphone X").getSlots().get(size - 1);
        assertEquals(bidLast.getBidValue(), new Double(3));

    }

    @Test
    protected void testReadEmptyAuction () throws Exception {

        assertNull(peers[0].read("Empty Test"));

    }

    // we need to execute it at the end.
    @Test
    @Order(4)
    protected void testDelete() throws Exception {
        peers[0].delete("Iphone X");
        assertNull(peers[0].read("Iphone X"));
    }




}
