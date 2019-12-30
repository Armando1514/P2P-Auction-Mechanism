package p2p.auction.mechanism.DAO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import p2p.auction.mechanism.Auction;
import p2p.auction.mechanism.AuctionBid;
import p2p.auction.mechanism.MessageListener;
import p2p.auction.mechanism.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class P2PAuctionBidDAOTests {

    private static final int NUMBER_OF_PEERS = 5;

    private static AuctionBidDAO[] peers;

    private static AuctionDAO auctionDAO;

    private static CountDownLatch c4  ;

    @BeforeAll
    static void initPeer() throws Exception {
        peers = new AuctionBidDAO[NUMBER_OF_PEERS];

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
            c4 = new CountDownLatch(NUMBER_OF_PEERS) ;


            auctionDAO = AuctionMechanismDAOFactory.instantiate(0, "127.0.0.1", new MessageListenerImpl(0), true).getAuctionDAO();

           int  i = 0;
            while( i  < NUMBER_OF_PEERS )
            {

                peers[i] = AuctionMechanismDAOFactory.instantiate(i + 1, "127.0.0.1", new MessageListenerImpl(i + 1), true).getAuctionBidDAO();
                i++;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //check if all the bids  created in parallel from different peers, are in the right order.
    @Test
    protected void testCreate() throws Exception {

        User user = new User("userTestUpdate","password", new Double(1), null, null);
        Auction auctionTest = new Auction(user, "testUpdate", new Date(),new Double(1));

        auctionTest = auctionDAO.create(auctionTest);
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
                        peers[rnd].create(bid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    c4.countDown();

                }
            }).start();

            i ++;
        }
        c4.await();

        // wait until all the threads are done


        i = 0;

        //check if all the bids are in dec order.
        ArrayList<AuctionBid> auctionSlot = null;
        try {
            auctionSlot = auctionDAO.read(auctionTest.getId()).getSlots();
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
        auctionTest = auctionDAO.create(auctionTest);

        AuctionBid bid = new AuctionBid(auctionTest, user, new Double(3));
        peers[0].create(bid);
        auctionDAO.read(auctionTest.getId());
        int size = auctionDAO.read(auctionTest.getId()).getSlots().size();
        AuctionBid bidLast = auctionDAO.read(auctionTest.getId()).getSlots().get(size - 1);
        assertEquals(bidLast.getBidValue(), new Double(3));

    }
}
