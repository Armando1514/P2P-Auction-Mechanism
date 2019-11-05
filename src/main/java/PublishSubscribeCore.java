import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;

public class PublishSubscribeCore implements PublishSubscribe {

    final private Peer peer;
    final private PeerDHT _dht;
    final private int DEFAULT_MASTER_PORT = 4000;

    final private ArrayList < String > s_topics = new ArrayList < String > ();

    public PublishSubscribeCore(int _id, String _boot_peer, final MessageListener _listener) throws Exception {
        /*
           1. Creates a peer builder with the provided peer ID and an empty key pair,
           2. Sets the UDP and TCP ports to the specified value.
           3. Creates a peer with our parameters and starts to listen for incoming connections.
         */
        peer = new PeerBuilder(Number160.createHash(_id)).
                ports(DEFAULT_MASTER_PORT + _id).start();

        // we start a peer DHT
        _dht = new PeerBuilderDHT(peer).start();

        /*
         The bootstrapping i.d. finding an existing peer in the overlay for the first
         connection is address with a well known peer listener (_boot_peer),
         so peers need to know the ip address where to connect the first time.
         */
        FutureBootstrap fb = peer.bootstrap().
                inetAddress(InetAddress.getByName(_boot_peer)).
                ports(DEFAULT_MASTER_PORT).start();

        // Wait for the asynchronous operation to end without interruption.
        fb.awaitUninterruptibly();
        // awaitUninterruptibly return true if the operation is finished.
        if (fb.isSuccess()) {

            /*
            bootstrapTo() returns a collection of of peers that were involved in
            the bootstrapping we are interested on discovering that peers.
             */
            peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).
                    start().awaitUninterruptibly();
        } else {
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
                return _listener.parseMessage(request);
            }
        });
    }

    public boolean createTopic(String _topic_name) {
        try {

            // The future object is used to create the topic
            FutureGet futureGet = _dht.
                    get(Number160.createHash(_topic_name)).start();
            // TomP2P uses non-blocking communication, so we need to wait the result
            futureGet.awaitUninterruptibly();
            // if we get the topic, and is empty (has not created yet)
            if (futureGet.isSuccess() && futureGet.isEmpty()) {
                /*
                     We put in the dht the key _topic_name
                     and we crete a  new set of PeerAddress data.
                 */
                _dht.put(Number160.createHash(_topic_name)).
                        data(new Data(new HashSet < PeerAddress > ())).
                        start().awaitUninterruptibly();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @SuppressWarnings("unchecked")
    public boolean subscribeTopic(String _topic_name) {
        try {
            FutureGet futureGet = _dht.get(Number160.createHash(_topic_name)).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                //topic not found
                if (futureGet.isEmpty()) return false;

                HashSet < PeerAddress > peers_on_topic;
                peers_on_topic = (HashSet < PeerAddress > ) futureGet.dataMap().values().iterator().next().object();
                //put our peer address in the topic
                peers_on_topic.add(_dht.peer().peerAddress());
                //update the hash table for the subscription
                _dht.put(Number160.createHash(_topic_name)).data(new Data(peers_on_topic)).start().awaitUninterruptibly();
                s_topics.add(_topic_name);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean publishToTopic(String _topic_name, Object _obj) {
        try {
            FutureGet futureGet = _dht.get(Number160.createHash(_topic_name)).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                HashSet < PeerAddress > peers_on_topic;
                peers_on_topic = (HashSet < PeerAddress > ) futureGet.dataMap().values().iterator().next().object();

                // for each peer address on the topic
                for (PeerAddress peer: peers_on_topic) {

                    //send the object to all the peers in the topic.
                    FutureDirect futureDirect = _dht.peer().sendDirect(peer).object(_obj).start();
                    futureDirect.awaitUninterruptibly();
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean unsubscribeFromTopic(String _topic_name) {
        try {
            FutureGet futureGet = _dht.get(Number160.createHash(_topic_name)).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                if (futureGet.isEmpty()) return false;
                HashSet < PeerAddress > peers_on_topic;
                peers_on_topic = (HashSet < PeerAddress > ) futureGet.dataMap().values().iterator().next().object();
                peers_on_topic.remove(_dht.peer().peerAddress());
                _dht.put(Number160.createHash(_topic_name)).data(new Data(peers_on_topic)).start().awaitUninterruptibly();
                s_topics.remove(_topic_name);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean leaveNetwork() {

        for (String topic: new ArrayList < String > (s_topics)) unsubscribeFromTopic(topic);
        _dht.peer().announceShutdown().start().awaitUninterruptibly();
        return true;
    }

}