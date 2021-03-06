package p2p.auction.mechanism.DAO;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;


import net.tomp2p.peers.PeerAddress;
import org.junit.jupiter.api.*;
import p2p.auction.mechanism.*;


import static org.junit.jupiter.api.Assertions.*;


class P2PAuctionDAOTests {

    private static final int NUMBER_OF_PEERS = 5;

    private static AuctionDAO[] peers;
    private static PeerAddress[] peerAddresses;


    private static CountDownLatch c2;
    private static CountDownLatch c3;


    @BeforeAll
    static void initPeer() throws Exception {
        peers = new AuctionDAO[NUMBER_OF_PEERS];
        peerAddresses = new PeerAddress[NUMBER_OF_PEERS];
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
            c2 = new CountDownLatch(NUMBER_OF_PEERS);
            c3 = new CountDownLatch(NUMBER_OF_PEERS);

            int i = 0;

            while (i < NUMBER_OF_PEERS) {

                AuctionMechanismDAOFactory instantiate = AuctionMechanismDAOFactory.instantiate(i, "127.0.0.1", new MessageListenerImpl(i), true);
                peers[i] = instantiate.getAuctionDAO();
                peerAddresses[i] = instantiate.getPeerAddress();
                i++;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //check if all the auction  created in parallel from different peers are in the hash map.
    @Test
    void testUpdateGetAll() throws InterruptedException, IOException, ClassNotFoundException {

        int i = 0;
        final int[] peersUpdated = {
                NUMBER_OF_PEERS
        };

        while (i < NUMBER_OF_PEERS) {

            int finalI = i;
            new Thread(() -> {

                Random random = new Random();

                int rnd = random.ints(1, (NUMBER_OF_PEERS)).findFirst().getAsInt();
                User user = new User("user" + finalI, "password");
                Auction auctionTest = new Auction(user, "test-" + finalI, new Date(), 1d);

                try {
                    peers[rnd].create(auctionTest);



                } catch (Exception e) {
                    e.printStackTrace();
                    peersUpdated[0]--;
                }

                c2.countDown();

            }).start();

            i++;
        }
        c2.await();

        // wait until all the threads are done

        //check if all the bids are in dec order.
        HashMap < Integer, Auction > auctions = peers[0].readAll();

        assertEquals(peersUpdated[0], auctions.size());

    }

    @Test
    void testUpdate() throws Exception {

        User user = new User("user1", "password");
        Auction auctionTest = new Auction(user, "test-0", new Date(), 1d);
        peers[0].create(auctionTest);
        auctionTest.setAuctionName("test-new");
        peers[0].update(auctionTest);
        Auction newAuction = peers[0].read(auctionTest.getId());

        assertEquals(newAuction.getAuctionName(), "test-new");

    }



    @Test
    void testUpdateParticipants() throws Exception {

        int i = 0;
        final int[] peersAvailable = {
                NUMBER_OF_PEERS
        };
        User user = new User("usdsaer", "password");
        Auction auctionTest = new Auction(user, "testsdwe", new Date(), 1d);
        Auction auctionTestCreated = peers[0].create(auctionTest);

        while (i < NUMBER_OF_PEERS) {

            int finalI = i;

            new Thread(() -> {

                try {

                    auctionTestCreated.getParticipants().put(String.valueOf(finalI), peerAddresses[finalI]);
                    peers[0].update(auctionTestCreated);

                } catch (Exception e) {
                    e.printStackTrace();
                    peersAvailable[0]--;
                }

                c3.countDown();

            }).start();

            i++;
        }
        c3.await();


        // wait until all the threads are done

        HashMap < String, PeerAddress > participants = peers[0].read(auctionTest.getId()).getParticipants();
        assertEquals(peersAvailable[0], participants.size());
    }


    @Test
    void testReadEmptyAuction() throws Exception {

        assertNull(peers[0].read(Integer.MAX_VALUE));

    }




    // we need to execute it at the end.
    @AfterEach
    void testDelete() throws Exception {
        HashMap < Integer, Auction > auctions = peers[0].readAll();
        for (Integer key: auctions.keySet()) {
            if (key != null)
                peers[0].delete(key);
        }
    }






}