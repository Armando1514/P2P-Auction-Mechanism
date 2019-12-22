package p2p.auction.mechanism;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;


public class P2PAuctionCRUDTests {

    private final int NUMBER_OF_PEERS = 20;

    private P2PAuction[] peers;

    private CountDownLatch cl  ;


    @BeforeEach
    protected void initPeer() throws Exception
    {
        peers = new P2PAuction[NUMBER_OF_PEERS];

        class MessageListenerImpl implements MessageListener{
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

                peers[i] = new P2PAuction(i, "127.0.0.1", new MessageListenerImpl(i));
                i++;
            }


            Double x = new Double(1);

            AuctionUser user = new AuctionUser("test","password", x, null, null);

            peers[0].createAuction("Iphone X",user, new Date(),324, "Apple phone, good condition.");


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        }


    @Test
    protected void testCreateAndUpdate() throws Exception {


        int i = 0;
        while( i < NUMBER_OF_PEERS ) {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    Random random = new Random();

                    int rnd = random.ints(1,(NUMBER_OF_PEERS)).findFirst().getAsInt();

                    peers[rnd].placeABid("Iphone X", new Double(rnd));
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
            auctionSlot = peers[0].getAuction("Iphone X").getSlots();
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


}
