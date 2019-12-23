package p2p.auction.mechanism.DAO;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import net.tomp2p.utils.Pair;
import p2p.auction.mechanism.Auction;
import p2p.auction.mechanism.AuctionBid;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

public class P2PAuctionDAO implements AuctionDAO {

    final private PeerDHT peerDHT;
    private static P2PAuctionDAO p2PAuctionDAO = null;


    private P2PAuctionDAO(PeerDHT peerDHT) {

        this.peerDHT = peerDHT;
    }

    private static final Random RND = new Random(42L);


    /* Read operation */
    public Auction read(String auction_name) throws Exception {

        FutureGet futureGet = this.peerDHT.get(Number160.createHash(auction_name)).getLatest().start();
        futureGet.awaitUninterruptibly();
        if (futureGet.isSuccess()) {
            //auction not found
            if (futureGet.isEmpty())
                return null;
            return (Auction) futureGet.data().object();
        }
        return null;
    }

    /* Delete operation */
    public void delete(String auction_name) {
        peerDHT.remove(Number160.createHash(auction_name)).all().start()
                .awaitUninterruptibly();

    }

    public void create(Auction auction) throws Exception {
        FuturePut p = peerDHT.put(Number160.createHash(auction.getAuctionName())).putIfAbsent()
                .data(new Data(auction)).start().awaitUninterruptibly();
        if(!p.isSuccess())
            throw new Exception("The nickname is not available, change it.");

    }


    /* Update the auction's values in an async p2p system, maintaining the consistency */
    public void update(Auction auction, AuctionBid newBid) throws Exception {
        Pair<Number640, Byte> pair2 = null;
        Pair<Number160, Data> pair = null;
        for(int i=0; i < 5; i++)
        {
             pair = getAndUpdate(peerDHT, auction, newBid);

            if (pair == null) {
                throw new BidException("we cannot handle this kind of inconsistency. Wait and retry after several minutes");
            }
            FuturePut fp = peerDHT
                    .put(Number160.createHash(auction.getAuctionName()))
                    .data( pair.element1().prepareFlag(),
                            pair.element0()).start().awaitUninterruptibly();

            pair2 = checkVersions(fp.rawResult());
            // 1 is PutStatus.OK_PREPARED
            if (pair2 != null && pair2.element1() == 1) {
                Auction lastAuction = (Auction) pair.element1().object();

                //get the last Bid
                if((lastAuction.getSlots().size() - 1) > 0) {
                    int size;
                    size = lastAuction.getSlots().size() - 1;

                    // check if the bid that i'm going to put, is bigger that the last bid inserted.
                    AuctionBid lastBid = lastAuction.getSlots().get(size-1);
                    // if is lower, remove it.
                    if (!lastBid.isSmallerThan(newBid)) {
                        peerDHT.remove(Number160.createHash(auction.getAuctionName())).versionKey(pair.element0()).start()
                                .awaitUninterruptibly();
                        throw new BidException("Your bid: " + newBid.getBidValue() + " is lower than the last one: " + lastBid.getBidValue() + ", update the auction status.");
                    }
                }

                break;
            }


            // if not removed, a low ttl will eventually get rid of it
            peerDHT.remove(Number160.createHash(auction.getAuctionName())).versionKey(pair.element0()).start()
                    .awaitUninterruptibly();
            Thread.sleep(RND.nextInt(500));
            }
            if (pair2 != null && pair2.element1() == 1) {

                FuturePut fp ;
                         fp = peerDHT.put(Number160.createHash(auction.getAuctionName()))
                        .versionKey(pair2.element0().versionKey()).putConfirm()
                        .data(new Data()).start().awaitUninterruptibly();

            } else {
                throw new BidException("we cannot handle this kind of inconsistency. Wait and retry after several minutes");
            }

    }

    /*
       get latest version, check if all replica peers have latest version,
       if not wait and try again, when you have the latest version do modification.
       In this case, write the new bind and assign the slot to the user.
       */
    private static Pair<Number160, Data> getAndUpdate(PeerDHT peerDHT,
                                                      Auction auction, AuctionBid newBid) throws BidException, InterruptedException, ClassNotFoundException,
            IOException {
        Pair<Number640, Data> pair = null;

        for(int i = 0; i < 5; i++)
        {
            // get the latest version of the auction.
            FutureGet fg =  peerDHT.get(Number160.createHash(auction.getAuctionName())).getLatest().start().awaitUninterruptibly();
            // check if all the peers agree on the same latest version, if not, wait a little and try again
            pair = checkVersions(fg.rawData());
            if(pair != null)
            {
                break;
            }
            // wait 500 ms first to ask again the latest version.
            Thread.sleep(RND.nextInt(500));

        }

        boolean checkValue;
        //we got the latest data
        if(pair != null)
        {
            int size ;
            Auction lastAuction = (Auction) pair.element1().object();
            AuctionBid lastBid = null;
            //get the last Bid
            if((lastAuction.getSlots().size() - 1) > 0) {
                size = lastAuction.getSlots().size() - 1;
                lastBid = lastAuction.getSlots().get(size);
                //if the new bid's value, is less than the last one, the flag is false, and throw an exception.
                checkValue = lastBid.isSmallerThan(newBid);
            }

            else{
                // this is the first bid
                checkValue = true;
            }

            // does it means that we can add the slots, because the new bid is bigger than the last
            if(checkValue) {
                // we add the new bid to the Auction.
                lastAuction.getSlots().add(newBid);
                Data newData = new Data(lastAuction);
                Number160 v = pair.element0().versionKey();
                long version = v.timestamp() + 1;
                newData.addBasedOn(v);
                //since we create a new version, we can access old version as well
                //Creates a new key with a long for the first 64bits, and using the lower 96bits for the rest.

                return new Pair<Number160, Data>(new Number160(version,
                        newData.hash()), newData);
            }
            else
            {
                //does it means that our bid is lower than the last bid, so we need to make another bid.
                throw new BidException("Your bid: "+ newBid.getBidValue()+" is lower than the last one: "+ lastBid.getBidValue() + ", update the auction status.");
            }
        }
        return null;
    }


    private static <K> Pair<Number640, K> checkVersions(Map<PeerAddress, Map<Number640, K>> rawData)
    {
        Number640 latestKey = null;
        K latestData = null;
        for ( Map.Entry<PeerAddress, Map<Number640, K>> entry : rawData.entrySet())
        {
            if (latestData == null && latestKey == null)
            {
                latestData = entry.getValue().values().iterator().next();
                latestKey = entry.getValue().keySet().iterator().next();
            }
            else
            {
                if(!latestKey.equals(entry.getValue().keySet().iterator().next())
                        || !latestData.equals(entry.getValue().values()
                        .iterator().next()))
                {
                    return null;
                }
            }

        }
        return new Pair<Number640, K>(latestKey, latestData);
    }


    public static AuctionDAO getInstance(PeerDHT peerDHT){

        if(p2PAuctionDAO == null) {
            p2PAuctionDAO = new P2PAuctionDAO(peerDHT);
        }

        return p2PAuctionDAO;
    }

}
