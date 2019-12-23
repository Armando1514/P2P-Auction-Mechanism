package p2p.auction.mechanism.DAO;

import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import p2p.auction.mechanism.MessageListener;

import java.net.InetAddress;

public class AuctionMechanismDAOFactory implements DAOFactory{

    // static variable single_instance of type Singleton
    private static UserDAO user = null;
    private static AuctionDAO auction = null;
    private static AuctionMechanismDAOFactory mechanism = null;

    final private Peer peer;
    final private PeerDHT peerDHT;
    final private int DEFAULT_MASTER_PORT = 4000;


    // private constructor restricted to this class itself
    private AuctionMechanismDAOFactory(int id, String boot_peer, final MessageListener listener) throws Exception
    {
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

    // static method to get the  Singleton class
    public AuctionMechanismDAOFactory getInstance() {

        return mechanism;
    }

    // if mode == true we are in test mode, so is not a singleton because we need to instantiate more peers on the local machine for testing reason,
    // if mode == false, we are in production mode, therefore is a singleton.
    public static AuctionMechanismDAOFactory instantiate(int id, String boot_peer, final MessageListener listener, boolean mode) throws Exception {

        if(mode == true)
            mechanism = new AuctionMechanismDAOFactory(id, boot_peer, listener);

        if(mechanism == null) {
            mechanism = new AuctionMechanismDAOFactory(id, boot_peer, listener);
        }

        return mechanism;
    }


    public AuctionDAO getAuctionDAO()
    {
        if(auction == null) {
            auction = P2PAuctionDAO.getInstance(peerDHT);
        }
        return auction;
    }

    public UserDAO getUserDAO()
    {
        if(user == null) {
            user = P2PUserDAO.getInstance(peerDHT);
        }
        return user;
    }
}