package p2p.auction.mechanism.DAO;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;
import net.tomp2p.utils.Pair;
import p2p.auction.mechanism.Control.AuctionMechanism;

import java.io.IOException;
import java.util.Random;

public class P2PAuctionBidDAO implements AuctionBidDAO {

    final private PeerDHT peerDHT;
    private static P2PAuctionBidDAO p2PAuctionBidDAO = null;


    private P2PAuctionBidDAO(PeerDHT peerDHT) {

        this.peerDHT = peerDHT;
    }

    private static final Random RND = new Random(42L);
    /* Update the auction's values in an async p2p system, maintaining the consistency */
    public void create(AuctionBid newBid) throws Exception {
        Pair<Number640, Byte> pair2 = null;
        Pair<Number160, Data> pair = null;
        for(int i=0; i < 5; i++)
        {
            pair = getAndUpdate(newBid);

            if (pair == null) {
                throw new DAOException("we cannot handle this kind of inconsistency. Wait and retry after several minutes");
            }
            FuturePut fp = peerDHT
                    .put(Number160.createHash(newBid.getAuction().getId()))
                    .data( pair.element1().prepareFlag(),
                            pair.element0()).start().awaitUninterruptibly();

            pair2 = DAOTools.checkVersions(fp.rawResult());
            // 1 is PutStatus.OK_PREPARED
            if (pair2 != null && pair2.element1() == 1) {
                Auction lastAuction = (Auction) pair.element1().object();


                    //get the last Bid
                    if ((lastAuction.getSlots().size() - 1) > 0) {
                        int size;
                        size = lastAuction.getSlots().size() - 1;

                        // check if the bid that i'm going to put, is bigger that the last bid inserted.
                        AuctionBid lastBid = lastAuction.getSlots().get(size - 1);
                        // if is lower, remove it.
                        if (!lastBid.isSmallerThan(newBid)) {
                            peerDHT.remove(Number160.createHash(newBid.getAuction().getId())).versionKey(pair.element0()).start()
                                    .awaitUninterruptibly();
                            throw new HigherBidExistenceException("Your bid: " + newBid.getBidValue() + " is lower than the last one: " + lastBid.getBidValue() + ", update the auction status.");
                        }


                }

                break;
            }



            // if not removed, a low ttl will eventually get rid of it
            peerDHT.remove(Number160.createHash(newBid.getAuction().getId())).versionKey(pair.element0()).start()
                    .awaitUninterruptibly();
            Thread.sleep(RND.nextInt(500));
        }
        if (pair2 != null && pair2.element1() == 1) {

         peerDHT.put(Number160.createHash(newBid.getAuction().getId()))
                    .versionKey(pair2.element0().versionKey()).putConfirm()
                    .data(new Data()).start().awaitUninterruptibly();
         AuctionMechanismDAOFactory.getInstance().getAuctionDAO().updateGetAll((Auction) pair.element1().object(),true);

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
            AuctionBid newBid) throws HigherBidExistenceException, InterruptedException, ClassNotFoundException,
            IOException, AuctionEndedException {
        Pair<Number640, Data> pair = null;

        for(int i = 0; i < 5; i++)
        {
            // get the latest version of the auction.
            FutureGet fg =  peerDHT.get(Number160.createHash(newBid.getAuction().getId())).getLatest().start().awaitUninterruptibly();
            // check if all the peers agree on the same latest version, if not, wait a little and try again
            pair = DAOTools.checkVersions(fg.rawData());
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

                if(lastAuction.getStatus() != Auction.AuctionStatus.ENDED) {

                    // we add the new bid to the Auction.
                    lastAuction.getSlots().add(newBid);
                    if(lastAuction.getFastPrice() != null) {
                        if (newBid.getBidValue() >= lastAuction.getFastPrice())
                            lastAuction.setStatus(Auction.AuctionStatus.ENDED);
                        String message = "The auction: "+lastAuction.getAuctionName()+"(id: "+lastAuction.getId()+"), is over.";
                            AuctionMechanism.noticePeers(lastAuction,message);
                    }

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
                    throw new AuctionEndedException("The Auction is ended, is not possible to place a bid");

                }
            }
            else
            {
                //does it means that our bid is lower than the last bid, so we need to make another bid.
                throw new HigherBidExistenceException("Your bid: "+ newBid.getBidValue()+" is lower than the last one: "+ lastBid.getBidValue() + ", update the auction status.");
            }
        }
        return null;
    }




    public static AuctionBidDAO getInstance(PeerDHT peerDHT){

        if(p2PAuctionBidDAO == null) {
            p2PAuctionBidDAO = new P2PAuctionBidDAO(peerDHT);
        }

        return p2PAuctionBidDAO;
    }
}
