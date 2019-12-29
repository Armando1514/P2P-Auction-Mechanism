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
import java.util.*;

public class P2PAuctionDAO implements AuctionDAO {

    final private PeerDHT peerDHT;
    private static P2PAuctionDAO p2PAuctionDAO = null;


    private P2PAuctionDAO(PeerDHT peerDHT) {

        this.peerDHT = peerDHT;
    }

    private static final Random RND = new Random(42L);



    /* Read operation */
    public Auction read(Integer auction_id) throws Exception {

        FutureGet futureGet = this.peerDHT.get(Number160.createHash(auction_id)).getLatest().start();
        futureGet.awaitUninterruptibly();
        if (futureGet.isSuccess()) {
            //auction not found
            if (futureGet.isEmpty())
                return null;
            return (Auction) futureGet.data().object();
        }
        return null;
    }

    /* read all operation */
    public HashMap<Integer, Auction> readAll() throws IOException, ClassNotFoundException {
        FutureGet futureGet = this.peerDHT.get(Number160.createHash("getAll")).getLatest().start();
        futureGet.awaitUninterruptibly();
        if (futureGet.isSuccess()) {
            //auction not found
            if (futureGet.isEmpty()) {
                return null;
            }
            return (HashMap<Integer, Auction>) futureGet.data().object();
        }
        return null;
    }


    /* Delete operation */
    public void delete(Integer auction_id) throws Exception {
        this.updateGetAll(this.read(auction_id), false);

        peerDHT.remove(Number160.createHash(auction_id)).all().start()
                .awaitUninterruptibly();
        updateVersioningNumber(auction_id, true);

    }

    public Auction create(Auction auction) throws Exception {

        FutureGet futureGet = this.peerDHT.get(Number160.createHash("auctionVersioningNumber")).getLatest().start();
        futureGet.awaitUninterruptibly();
        FuturePut p;
        int value = 0;
        boolean inserted = false;
        Iterator<Map.Entry<Integer, Integer>> iterator = null;
        HashMap<Integer, Integer> freeElements = new HashMap<>()  ;
        if (this.readAll() == null) {

            HashMap<Integer, Auction> x = new HashMap<>();
            peerDHT.put(Number160.createHash("getAll")).putIfAbsent()
                    .data(new Data(x)).start().awaitUninterruptibly();
        }

        if (futureGet.isSuccess()) {
            //auction not found
            if (futureGet.isEmpty()) {

                p = peerDHT.put(Number160.createHash("auctionVersioningNumber")).putIfAbsent()
                        .data(new Data(freeElements)).start().awaitUninterruptibly();
                if (p.isSuccess()) {
                    value = 0;
                    auction.setId(value);

                    p = peerDHT.put(Number160.createHash(value)).putIfAbsent()
                            .data(new Data(auction)).start().awaitUninterruptibly();
                    if (p.isSuccess()) {
                        inserted = true;

                        updateVersioningNumber(auction.getId(), false);
                        updateGetAll(auction, true);
                        System.out.println("INSERTED:" + value + " AUCTION " + auction.getAuctionName());

                    }
                }
            }
            else {
                freeElements = (HashMap<Integer, Integer>) futureGet.data().object();
                iterator = null;
                iterator = freeElements.entrySet().iterator();

            }
        }
                else
                    throw new DAOException("Retry, a not common error is happened during the FUTUREGET.");





        while(!inserted) {

            if (!iterator.hasNext()) {
                value = this.readAll().size() + RND.nextInt(500);
                futureGet = this.peerDHT.get(Number160.createHash("auctionVersioningNumber")).getLatest().start().awaitUninterruptibly();
                freeElements = (HashMap<Integer, Integer>) futureGet.data().object();
                iterator = freeElements.entrySet().iterator();

            } else
                value = iterator.next().getValue();

                auction.setId(value);
            System.out.println("TRY:" + value + " AUCTION " + auction.getAuctionName());

            p = peerDHT.put(Number160.createHash(value)).putIfAbsent()
                    .data(new Data(auction)).start().awaitUninterruptibly();
            if(p.isSuccess())
            {
                inserted = true;
                updateVersioningNumber(auction.getId(), false);
                updateGetAll(auction, true);

                System.out.println("INSERTED:" + value + " AUCTION " + auction.getAuctionName());

            }

        }




        return auction;
    }

    // if mode true, is for add a new integer available , if mode false, is for removing.
    private void updateVersioningNumber(Integer id, boolean mode) throws Exception {

        Pair<Number640, Byte> pair2 = null;
        Pair<Number160, Data> pair = null;
        for(int i=0; i < 50; i++)
        {
            pair = getAndUpdateVersioningNumber( id, mode);

            if (pair == null) {
                throw new DAOException("we cannot handle this kind of inconsistency. Wait and retry after several minutes");
            }
            FuturePut fp = peerDHT
                    .put(Number160.createHash("auctionVersioningNumber"))
                    .data( pair.element1().prepareFlag(),
                            pair.element0()).start().awaitUninterruptibly();

            pair2 = checkVersions(fp.rawResult());
            // 1 is PutStatus.OK_PREPARED
            if (pair2 != null && pair2.element1() == 1) {
                break;
            }

            // if not removed, a low ttl will eventually get rid of it
            peerDHT.remove(Number160.createHash("auctionVersioningNumber")).versionKey(pair.element0()).start()
                    .awaitUninterruptibly();
            Thread.sleep(RND.nextInt(500));
        }
        if (pair2 != null && pair2.element1() == 1) {

            FuturePut fp ;
            fp = peerDHT.put(Number160.createHash("auctionVersioningNumber"))
                    .versionKey(pair2.element0().versionKey()).putConfirm()
                    .data(new Data()).start().awaitUninterruptibly();
        } else {
            throw new DAOException("we cannot handle this kind of inconsistency. Wait and retry after several minutes");
        }

    }

    private  Pair<Number160, Data> getAndUpdateVersioningNumber(
                                                            Integer id, boolean mode) throws InterruptedException, IOException, ClassNotFoundException {
        Pair<Number640, Data> pair = null;
        for (int i = 0; i < 50; i++) {
            FutureGet fg = peerDHT.get(Number160.createHash("auctionVersioningNumber")).getLatest().start()
                    .awaitUninterruptibly();
            // check if all the peers agree on the same latest version, if not
            // wait a little and try again
            pair = checkVersions(fg.rawData());
            if (pair != null) {
                break;
            }
            Thread.sleep(RND.nextInt(500));
        }
        // we got the latest data
        if (pair != null) {
            // update operation is append
            HashMap<Integer, Integer> freeElements = (HashMap<Integer, Integer>) pair.element1().object();
                if (mode == true)
                    freeElements.put(id, id);
                else
                    //remove mode
                    freeElements.remove(id);

                Data newData = new Data(freeElements);
                Number160 v = pair.element0().versionKey();
                long version = v.timestamp() + 1;
                newData.addBasedOn(v);
                //since we create a new version, we can access old versions as well
                return new Pair<Number160, Data>(new Number160(version,
                        newData.hash()), newData);
            }

        return null;
    }

    // if mode true, is for create, if mode false, is for removing.
    private void updateGetAll(Auction auction, boolean mode) throws Exception {

        Pair<Number640, Byte> pair2 = null;
        Pair<Number160, Data> pair = null;
        for(int i=0; i < 50; i++)
        {
            pair = getAndUpdateGetAll(auction, mode);

            if (pair == null) {
                throw new DAOException("we cannot handle this kind of inconsistency. Wait and retry after several minutes");
            }
            FuturePut fp = peerDHT
                    .put(Number160.createHash("getAll"))
                    .data( pair.element1().prepareFlag(),
                            pair.element0()).start().awaitUninterruptibly();

            pair2 = checkVersions(fp.rawResult());
            // 1 is PutStatus.OK_PREPARED
            if (pair2 != null && pair2.element1() == 1) {
                break;
            }


            // if not removed, a low ttl will eventually get rid of it
            peerDHT.remove(Number160.createHash("getAll")).versionKey(pair.element0()).start()
                    .awaitUninterruptibly();
            Thread.sleep(RND.nextInt(500));
        }
        if (pair2 != null && pair2.element1() == 1) {

            FuturePut fp ;
            fp = peerDHT.put(Number160.createHash("getAll"))
                    .versionKey(pair2.element0().versionKey()).putConfirm()
                    .data(new Data()).start().awaitUninterruptibly();
        } else {
            throw new DAOException("we cannot handle this kind of inconsistency. Wait and retry after several minutes");
        }


    }

    private  Pair<Number160, Data> getAndUpdateGetAll(
                                                            Auction auction, boolean mode) throws InterruptedException, IOException, ClassNotFoundException {
        Pair<Number640, Data> pair = null;
        for (int i = 0; i < 50; i++) {
            FutureGet fg = peerDHT.get(Number160.createHash("getAll")).getLatest().start()
                    .awaitUninterruptibly();
            // check if all the peers agree on the same latest version, if not
            // wait a little and try again
            pair = checkVersions(fg.rawData());
            if (pair != null) {
                break;
            }
            Thread.sleep(RND.nextInt(500));
        }
        // we got the latest data
        if (pair != null) {
            // update operation is append

            HashMap<Integer, Auction> hash = (HashMap<Integer, Auction>) pair.element1().object();
            if(hash != null) {
    if (mode == true)
        hash.put(auction.getId(), auction);
        //remove mode
    else
        hash.remove(auction.getId());

    Data newData = new Data(hash);
    Number160 v = pair.element0().versionKey();
    long version = v.timestamp() + 1;
    newData.addBasedOn(v);
    //since we create a new version, we can access old versions as well
    return new Pair<Number160, Data>(new Number160(version,
            newData.hash()), newData);
        }
        }
        return null;
    }



    /* Update the auction's values in an async p2p system, maintaining the consistency */
    public void update(Auction auction, AuctionBid newBid) throws Exception {
        Pair<Number640, Byte> pair2 = null;
        Pair<Number160, Data> pair = null;
        for(int i=0; i < 5; i++)
        {
             pair = getAndUpdate( auction, newBid);

            if (pair == null) {
                throw new DAOException("we cannot handle this kind of inconsistency. Wait and retry after several minutes");
            }
            FuturePut fp = peerDHT
                    .put(Number160.createHash(auction.getId()))
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
                        peerDHT.remove(Number160.createHash(auction.getId())).versionKey(pair.element0()).start()
                                .awaitUninterruptibly();
                        throw new HigherBidExistenceException("Your bid: " + newBid.getBidValue() + " is lower than the last one: " + lastBid.getBidValue() + ", update the auction status.");
                    }
                }

                break;
            }



            // if not removed, a low ttl will eventually get rid of it
            peerDHT.remove(Number160.createHash(auction.getId())).versionKey(pair.element0()).start()
                    .awaitUninterruptibly();
            Thread.sleep(RND.nextInt(500));
            }
            if (pair2 != null && pair2.element1() == 1) {

                FuturePut fp ;
                         fp = peerDHT.put(Number160.createHash(auction.getId()))
                        .versionKey(pair2.element0().versionKey()).putConfirm()
                        .data(new Data()).start().awaitUninterruptibly();

            } else {
                throw new DAOException("we cannot handle this kind of inconsistency. Wait and retry after several minutes");
            }

    }


    /*
       get latest version, check if all replica peers have latest version,
       if not wait and try again, when you have the latest version do modification.
       In this case, write the new bind and assign the slot to the user.
       */
    private  Pair<Number160, Data> getAndUpdate(
                                                      Auction auction, AuctionBid newBid) throws HigherBidExistenceException, InterruptedException, ClassNotFoundException,
            IOException {
        Pair<Number640, Data> pair = null;

        for(int i = 0; i < 5; i++)
        {
            // get the latest version of the auction.
            FutureGet fg =  peerDHT.get(Number160.createHash(auction.getId())).getLatest().start().awaitUninterruptibly();
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
                throw new HigherBidExistenceException("Your bid: "+ newBid.getBidValue()+" is lower than the last one: "+ lastBid.getBidValue() + ", update the auction status.");
            }
        }
        return null;
    }


    private  <K> Pair<Number640, K> checkVersions(Map<PeerAddress, Map<Number640, K>> rawData)
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
