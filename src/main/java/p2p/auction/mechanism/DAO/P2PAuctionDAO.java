package p2p.auction.mechanism.DAO;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;
import net.tomp2p.utils.Pair;

import java.io.IOException;
import java.util.*;

public class P2PAuctionDAO implements AuctionDAO {

    final private PeerDHT peerDHT;
    private static P2PAuctionDAO p2PAuctionDAO = null;


    private P2PAuctionDAO(PeerDHT peerDHT) {

        this.peerDHT = peerDHT;
    }

    private static final Random RND = new Random(42 L);



    /* Read operation */
    public Auction read(Integer auction_id) throws Exception {

        FutureGet futureGet = this.peerDHT.get(Number160.createHash(auction_id)).getLatest().start().awaitUninterruptibly();
        if (futureGet.isSuccess()) {
            //auction not found
            if (futureGet.isEmpty())
                return null;
            return (Auction) futureGet.data().object();
        }
        return null;
    }

    /* read all operation */
    public HashMap < Integer, Auction > readAll() throws IOException, ClassNotFoundException {
        FutureGet futureGet = this.peerDHT.get(Number160.createHash("getAll")).getLatest().start();
        futureGet.awaitUninterruptibly();
        if (futureGet.isSuccess()) {
            //auction not found
            if (futureGet.isEmpty()) {
                return null;
            }
            return (HashMap < Integer, Auction > ) futureGet.data().object();
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
        Iterator < Integer > iterator = null;
        HashSet < Integer > freeElements = new HashSet < Integer > ();
        if (this.readAll() == null) {

            HashMap < Integer, Auction > x = new HashMap < > ();
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


                    }
                }
            } else {
                freeElements = (HashSet < Integer > ) futureGet.data().object();
                iterator = freeElements.iterator();

            }
        } else
            throw new DAOException("Retry, a not common error is happened during the FUTUREGET.");





        while (!inserted) {

            if (!iterator.hasNext()) {
                value = this.readAll().size() + RND.nextInt(500);
                futureGet = this.peerDHT.get(Number160.createHash("auctionVersioningNumber")).getLatest().start().awaitUninterruptibly();
                freeElements = (HashSet < Integer > ) futureGet.data().object();
                iterator = freeElements.iterator();

            } else
                value = iterator.next();

            auction.setId(value);

            p = peerDHT.put(Number160.createHash(value)).putIfAbsent()
                    .data(new Data(auction)).start().awaitUninterruptibly();
            if (p.isSuccess()) {
                inserted = true;
                updateVersioningNumber(auction.getId(), false);
                updateGetAll(auction, true);


            }

        }




        return auction;
    }

    // if mode true, is for add a new integer available , if mode false, is for removing.
    private void updateVersioningNumber(Integer id, boolean mode) throws Exception {

        Pair < Number640, Byte > pair2 = null;
        Pair < Number160, Data > pair = null;
        for (int i = 0; i < 50; i++) {
            pair = getAndUpdateVersioningNumber(id, mode);

            if (pair == null) {
                throw new DAOException("we cannot handle this kind of inconsistency. Wait and retry after several minutes");
            }
            FuturePut fp = peerDHT
                    .put(Number160.createHash("auctionVersioningNumber"))
                    .data(pair.element1().prepareFlag(),
                            pair.element0()).start().awaitUninterruptibly();

            pair2 = DAOTools.checkVersions(fp.rawResult());
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

            FuturePut fp;
            fp = peerDHT.put(Number160.createHash("auctionVersioningNumber"))
                    .versionKey(pair2.element0().versionKey()).putConfirm()
                    .data(new Data()).start().awaitUninterruptibly();
        } else {
            throw new DAOException("we cannot handle this kind of inconsistency. Wait and retry after several minutes");
        }

    }

    private Pair < Number160, Data > getAndUpdateVersioningNumber(
            Integer id, boolean mode) throws InterruptedException, IOException, ClassNotFoundException {
        Pair < Number640, Data > pair = null;
        for (int i = 0; i < 50; i++) {
            FutureGet fg = peerDHT.get(Number160.createHash("auctionVersioningNumber")).getLatest().start()
                    .awaitUninterruptibly();
            // check if all the peers agree on the same latest version, if not
            // wait a little and try again
            pair = DAOTools.checkVersions(fg.rawData());
            if (pair != null) {
                break;
            }
            Thread.sleep(RND.nextInt(500));
        }
        // we got the latest data
        if (pair != null) {
            // update operation is append
            HashSet < Integer > freeElements = (HashSet < Integer > ) pair.element1().object();
            if (mode == true)
                freeElements.add(id);
            else
                //remove mode
                freeElements.remove(id);

            Data newData = new Data(freeElements);
            Number160 v = pair.element0().versionKey();
            long version = v.timestamp() + 1;
            newData.addBasedOn(v);
            //since we create a new version, we can access old versions as well
            return new Pair < Number160, Data > (new Number160(version,
                    newData.hash()), newData);
        }

        return null;
    }

    // if mode true, is for create, if mode false, is for removing.
    public void updateGetAll(Auction auction, boolean mode) throws Exception {

        Pair < Number640, Byte > pair2 = null;
        Pair < Number160, Data > pair = null;
        for (int i = 0; i < 50; i++) {
            pair = getAndUpdateGetAll(auction, mode);

            if (pair == null) {
                throw new DAOException("we cannot handle this kind of inconsistency. Wait and retry after several minutes");
            }
            FuturePut fp = peerDHT
                    .put(Number160.createHash("getAll"))
                    .data(pair.element1().prepareFlag(),
                            pair.element0()).start().awaitUninterruptibly();

            pair2 = DAOTools.checkVersions(fp.rawResult());
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

            HashMap < Integer, Auction > hash = (HashMap < Integer, Auction > ) pair.element1().object();

            peerDHT.put(Number160.createHash("getAll"))
                    .versionKey(pair2.element0().versionKey()).putConfirm()
                    .data(new Data()).start().awaitUninterruptibly();
        } else {
            throw new DAOException("we cannot handle this kind of inconsistency. Wait and retry after several minutes");
        }


    }

    private Pair < Number160, Data > getAndUpdateGetAll(
            Auction auction, boolean mode) throws InterruptedException, IOException, ClassNotFoundException {
        Pair < Number640, Data > pair = null;
        for (int i = 0; i < 50; i++) {
            FutureGet fg = peerDHT.get(Number160.createHash("getAll")).getLatest().start()
                    .awaitUninterruptibly();
            // check if all the peers agree on the same latest version, if not
            // wait a little and try again
            pair = DAOTools.checkVersions(fg.rawData());
            if (pair != null) {
                break;
            }
            Thread.sleep(RND.nextInt(500));
        }
        // we got the latest data
        if (pair != null) {
            // update operation is append

            HashMap < Integer, Auction > hash = (HashMap < Integer, Auction > ) pair.element1().object();
            if (hash != null) {
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
                return new Pair < Number160, Data > (new Number160(version,
                        newData.hash()), newData);
            }
        }
        return null;
    }



    /* Update the auction's values in an async p2p system, maintaining the consistency */
    public void update(Auction newAuction) throws Exception {
        Pair < Number640, Byte > pair2 = null;
        Pair < Number160, Data > pair = null;
        for (int i = 0; i < 5; i++) {
            pair = getAndUpdate(newAuction);

            if (pair == null) {
                throw new DAOException("we cannot handle this kind of inconsistency. Wait and retry after several minutes");
            }
            // id never changes
            FuturePut fp = peerDHT
                    .put(Number160.createHash(newAuction.getId()))
                    .data(pair.element1().prepareFlag(),
                            pair.element0()).start().awaitUninterruptibly();

            pair2 = DAOTools.checkVersions(fp.rawResult());
            // 1 is PutStatus.OK_PREPARED
            if (pair2 != null && pair2.element1() == 1) {

                break;
            }



            // if not removed, a low ttl will eventually get rid of it
            peerDHT.remove(Number160.createHash(newAuction.getId())).versionKey(pair.element0()).start()
                    .awaitUninterruptibly();
            Thread.sleep(RND.nextInt(500));
        }
        if (pair2 != null && pair2.element1() == 1) {

            FuturePut fp;
            fp = peerDHT.put(Number160.createHash(newAuction.getId()))
                    .versionKey(pair2.element0().versionKey()).putConfirm()
                    .data(new Data()).start().awaitUninterruptibly();
            if (fp.isSuccess()) {
                // update the global list
                updateGetAll((Auction) pair.element1().object(), true);
            }

        } else {
            throw new DAOException("we cannot handle this kind of inconsistency. Wait and retry after several minutes");
        }

    }


    /*
       get latest version, check if all replica peers have latest version,
       if not wait and try again, when you have the latest version do modification.
       In this case, write the new bind and assign the slot to the user.
       */
    private Pair < Number160, Data > getAndUpdate(Auction newAuction) throws InterruptedException, ClassNotFoundException,
            IOException {
        Pair < Number640, Data > pair = null;

        for (int i = 0; i < 5; i++) {
            // get the latest version of the auction.
            FutureGet fg = peerDHT.get(Number160.createHash(newAuction.getId())).getLatest().start().awaitUninterruptibly();
            // check if all the peers agree on the same latest version, if not, wait a little and try again
            pair = DAOTools.checkVersions(fg.rawData());
            if (pair != null) {
                break;
            }
            // wait 500 ms first to ask again the latest version.
            Thread.sleep(RND.nextInt(500));

        }

        //we got the latest data
        if (pair != null) {
            Auction last = (Auction) pair.element1().object();

            //the problem of collision is related only to the participants field, because is editable from all the peers.
            last = last.updateElements(newAuction);

            Data newData = new Data(last);

            Number160 v = pair.element0().versionKey();
            long version = v.timestamp() + 1;
            newData.addBasedOn(v);
            //since we create a new version, we can access old version as well
            //Creates a new key with a long for the first 64bits, and using the lower 96bits for the rest.

            return new Pair < Number160, Data > (new Number160(version,
                    newData.hash()), newData);
        }

        return null;
    }






    static AuctionDAO getInstance(PeerDHT peerDHT) {

        if (p2PAuctionDAO == null) {
            p2PAuctionDAO = new P2PAuctionDAO(peerDHT);
        }

        return p2PAuctionDAO;
    }

}