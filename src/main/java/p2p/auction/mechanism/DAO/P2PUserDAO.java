package p2p.auction.mechanism.DAO;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import net.tomp2p.utils.Pair;

import java.io.IOException;
import java.util.Random;

public class P2PUserDAO implements UserDAO {
    final private PeerDHT peerDHT;
    private static P2PUserDAO p2PUserDAO = null;




    private P2PUserDAO(PeerDHT peerDHT) {

        this.peerDHT = peerDHT;
    }

    private static final Random RND = new Random(42L);


    /* Read operation */
    public User read(String nickname) throws Exception {

        FutureGet futureGet = this.peerDHT.get(Number160.createHash(nickname)).getLatest().start().awaitUninterruptibly();
        if (futureGet.isSuccess()) {
            //auction not found
            if (futureGet.isEmpty())
                return null;

            return (User) futureGet.data().object();
        }
        return null;
    }


    /* Update the auction's values in an async p2p system, maintaining the consistency */
    public void create(User user) throws UsernameExistsException, IOException {

        FuturePut p = peerDHT.put(Number160.createHash(user.getNickname())).putIfAbsent()
                .data(new Data(user)).start().awaitUninterruptibly();
        if (!p.isSuccess())
            throw new UsernameExistsException("The nickname is not available, change it.");


    }

    /* Update the auction's values in an async p2p system, maintaining the consistency */
    public void update(User newUser) throws Exception {
        Pair < Number640, Byte > pair2 = null;
        Pair < Number160, Data > pair;
        for (int i = 0; i < 5; i++) {
            pair = getAndUpdate(newUser);

            if (pair == null) {
                throw new DAOException("we cannot handle this kind of inconsistency. Wait and retry after several minutes");
            }
            // id never changes
            FuturePut fp = peerDHT
                    .put(Number160.createHash(newUser.getNickname()))
                    .data(pair.element1().prepareFlag(),
                            pair.element0()).start().awaitUninterruptibly();

            pair2 = DAOTools.checkVersions(fp.rawResult());
            // 1 is PutStatus.OK_PREPARED
            if (pair2 != null && pair2.element1() == 1) {

                break;
            }


            // if not removed, a low ttl will eventually get rid of it
            peerDHT.remove(Number160.createHash(newUser.getNickname())).versionKey(pair.element0()).start()
                    .awaitUninterruptibly();
            Thread.sleep(RND.nextInt(500));
        }
        if (pair2 != null && pair2.element1() == 1) {
            peerDHT.put(Number160.createHash(newUser.getNickname()))
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
    private Pair < Number160, Data > getAndUpdate(User newUser) throws InterruptedException, ClassNotFoundException,
            IOException {
        Pair < Number640, Data > pair = null;

        for (int i = 0; i < 5; i++) {
            // get the latest version of the auction.
            FutureGet fg = peerDHT.get(Number160.createHash(newUser.getNickname())).getLatest().start().awaitUninterruptibly();
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
            User last = (User) pair.element1().object();

            //the problem of collision is related only to the unreaded messages field, because is editable from all the peers.
            last = last.updateElements(newUser);

            Data newData = new Data(last);

            Number160 v = pair.element0().versionKey();
            long version = v.timestamp() + 1;
            newData.addBasedOn(v);
            //since we create a new version, we can access old version as well
            //Creates a new key with a long for the first 64bits, and using the lower 96bits for the rest.

            return new Pair < > (new Number160(version,
                    newData.hash()), newData);
        }

        return null;
    }




    static P2PUserDAO getInstance(PeerDHT peerDHT) {

        if (p2PUserDAO == null) {
            p2PUserDAO = new P2PUserDAO(peerDHT);
        }

        return p2PUserDAO;
    }

}