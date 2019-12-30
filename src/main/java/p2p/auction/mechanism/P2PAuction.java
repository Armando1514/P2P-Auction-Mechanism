package p2p.auction.mechanism;

import java.util.Date;
import java.util.ArrayList;

import p2p.auction.mechanism.DAO.Auction;
import p2p.auction.mechanism.DAO.User;

import java.util.Random;


public class P2PAuction  {


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
    public boolean createAuction(String auction_name, User user, Date expirationDate, double reserved_price, String description) {
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
  return null;
    }

    public Auction getAuction(String auction_name) throws Exception
    {
    return null;
    }
}
