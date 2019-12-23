package p2p.auction.mechanism.DAO;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.dht.PutBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import net.tomp2p.utils.Pair;

import p2p.auction.mechanism.User;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

public class P2PUserDAO implements UserDAO {
    final private PeerDHT peerDHT;
    private static P2PUserDAO p2PUserDAO = null;




    private P2PUserDAO(PeerDHT peerDHT)  {

        this.peerDHT = peerDHT;
    }

    private static final Random RND = new Random(42L);


    /* Read operation */
    public User read(String nickname) throws Exception {

        FutureGet futureGet = this.peerDHT.get(Number160.createHash(nickname)).getLatest().start();
        futureGet.awaitUninterruptibly();
        if (futureGet.isSuccess()) {
            //auction not found
            if (futureGet.isEmpty())
                return null;
            return (User) futureGet.data().object();
        }
        return null;
    }


    /* Update the auction's values in an async p2p system, maintaining the consistency */
    public void create(User user) throws UserException, IOException {

        FuturePut p = peerDHT.put(Number160.createHash(user.getNickname())).putIfAbsent()
                        .data(new Data(user)).start().awaitUninterruptibly();
        if(!p.isSuccess())
        throw new UserException("The nickname is not available, change it.");


    }



    public static P2PUserDAO getInstance(PeerDHT peerDHT){

        if(p2PUserDAO == null) {
            p2PUserDAO = new P2PUserDAO(peerDHT);
        }

        return p2PUserDAO;
    }

}
