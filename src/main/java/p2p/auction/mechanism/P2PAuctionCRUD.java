package p2p.auction.mechanism;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;
import net.tomp2p.utils.Pair;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.Random;

public class P2PAuctionCRUD {

    final private Peer peer;
    final private PeerDHT peerDHT;
    final private int DEFAULT_MASTER_PORT = 4000;



    public P2PAuctionCRUD(int id, String boot_peer, final MessageListener listener) throws Exception {
                 /*
           1. Creates a peer builder with the provided peer ID
           2. Sets the UDP and TCP ports to the specified value (DEFAULT_MASTER_PORT + id).
           3. Creates a peer with our parameters and starts to listen for incoming connections.
         */
        peer = new PeerBuilder(Number160.createHash(id)).
                ports(DEFAULT_MASTER_PORT + id).start();
        // we start a peer DHT
        peerDHT = new PeerBuilderDHT(peer).start();

        /*
         The bootstrapping i.d. finding an existing peer in the overlay for the first
         connection is address with a well known peer listener (boot_peer).
         The peers need to know the ip address where to connect the first time.
         */
        FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(boot_peer)).
                ports(DEFAULT_MASTER_PORT).start();

        // Wait for because fb is an asynchronous operation.
        fb.awaitUninterruptibly();

        if (fb.isSuccess()) {

            // The routing is initiated to the peers specified in "bootstrapTo".
            peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).
                    start().awaitUninterruptibly();
        }
        else {

            throw new Exception("Error in master peer bootstrap.");
        }
        /*
         * Replies to a direct message from a peer. This reply is based on objects.
         *
         * @param sender
         *            The sender of this message
         * @param request
         *            The request that the sender sent.
         * @return A new object that is the reply.
         * @throws Exception
         */
        peer.objectDataReply(new ObjectDataReply() {

            public Object reply(PeerAddress sender, Object request) throws Exception {

                return listener.parseMessage(request);
            }
        });
    }

    private static final Random RND = new Random(42L);


    protected void store(Auction auction, AuctionBid newBid) throws Exception {
        Pair<Number640, Byte> pair2 = null;

        for(int i=0; i < 5; i++)
        {
            Pair<Number160, Data> pair = getAndUpdate(peerDHT, auction, newBid);
            if (pair == null) {
                throw new BidException("Failure, wait some time, first to make another bid.");
            }

            /*
            put prepared with data and short TTL,
            if status is OK on all replica peers, go ahead,
            otherwise, remove the data and go to step
             */
            FuturePut fp = peerDHT.put(Number160.createHash(auction.getAuctionName())).data(Number160.createHash(auction.getAuctionName()), pair.element1().prepareFlag(),
                    pair.element0()).start().awaitUninterruptibly();
            pair2 = checkVersions(fp.rawResult());
            if(pair2 != null && pair2.element1() == 1)
            {
                FutureGet futureGet = peerDHT.get(Number160.createHash("Nino")).start();
                futureGet.awaitUninterruptibly();
                if (futureGet.isSuccess()) {

                    System.out.println(((Auction) futureGet.data().object()).getSlots().get(0).getBidValue());
                }
                break;
            }
            // if not remove.
            peerDHT.remove(Number160.createHash(auction.getAuctionName())).versionKey(pair.element0()).start().awaitUninterruptibly();
            Thread.sleep(RND.nextInt(500));
        }
        if(pair2 != null && pair2.element1()==1)
        {
            // put confirm, send the data.
            FuturePut fp = peerDHT.put(Number160.createHash(auction.getAuctionName())).versionKey(pair2.element0().versionKey()).putConfirm().data(new Data())
                    .start().awaitUninterruptibly();
            FutureGet futureGet = peerDHT.get(Number160.createHash("Nino")).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {

                System.out.println(((Auction) futureGet.data().object()).getSlots().get(0).getBidValue());
            }

        } else {
            throw new BidException("Failure, wait some time, first to make another bid.");
        }

    }


    /*
     Step 1.
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
            AuctionBid lastBid;
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
                throw new BidException("Your bid is lower than the last one, update the auction status.");
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

    protected PeerDHT getPeerDHT() {
        return peerDHT;
    }
}
