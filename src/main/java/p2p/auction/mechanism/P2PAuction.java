package p2p.auction.mechanism;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashSet;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;

import java.util.Random;


public class P2PAuction  extends P2PAuctionCRUD implements P2PAuctionAPI {

    public P2PAuction(int id, String boot_peer, final MessageListener listener) throws Exception {
        super(id, boot_peer, listener);
    }

    private static final Random RND = new Random(42L);
    final private ArrayList<String> topics = new ArrayList<String>();


    /**
     * Creates a new auction for a good.
     *
     * @param auction_name   a String, the name identify the auction.
     * @param expirationDate a Date that is the end time of an auction.
     * @param reserved_price a double value that is the reserve minimum pricing selling.
     * @param description    a String describing the selling goods in the auction.
     * @return true if the auction is correctly created, false otherwise.
     */
    public boolean createAuction(String auction_name, AuctionUser user, Date expirationDate, double reserved_price, String description) {

        try {
            // The future object is used to create the topic
            FutureGet futureGet = super.getPeerDHT().
                    get(Number160.createHash(auction_name)).start();
            futureGet.awaitUninterruptibly();
            // if we get the topic, and is empty (has not created yet)
            if (futureGet.isSuccess() && futureGet.isEmpty()) {

                // creation of an auction object
                Auction auction = new Auction(user, auction_name,
                        new HashSet<PeerAddress>(), expirationDate);

                // we put in the dht the auction object, the key is the auction_name.
                super.getPeerDHT().put(Number160.createHash(auction_name)).
                        data(new Data(auction)).
                        start().awaitUninterruptibly();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public String checkAuction(String _auction_name) {
        return null;
    }



    /**
     * Places a bid for an auction if it is not already ended.
     *
     * @param auction_name a String, the name of the auction.
     * @param bid_amount   a double value, the bid for an auction.
     * @return Auction, object representing the status of the auction.
     */
    public Auction placeABid(String auction_name, double bid_amount) {
        try {
                Auction auction = super.read(auction_name);

                if(auction == null)
                    return null;

                //remember to change
                AuctionUser user = new AuctionUser("test","password", bid_amount, null, null);

                //let's create the bid
                AuctionBid newBid = new AuctionBid(auction, user, bid_amount);

                //put our peer address in the topic

                super.createAndUpdate(auction, newBid);

                return super.read(auction_name);
        }
         catch (Exception e) {
            e.printStackTrace();
             return null;

         }
    }

    public Auction getAuction(String auction_name) throws Exception
    {
    return super.read(auction_name);
    }
}
