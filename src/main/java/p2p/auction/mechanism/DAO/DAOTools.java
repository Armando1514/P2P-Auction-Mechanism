package p2p.auction.mechanism.DAO;

import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.utils.Pair;

import java.util.Map;

public interface  DAOTools {


    static  <K> Pair<Number640, K> checkVersions(Map<PeerAddress, Map<Number640, K>> rawData)
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
                assert latestData != null;
                if(!latestKey.equals(entry.getValue().keySet().iterator().next())
                        || !latestData.equals(entry.getValue().values()
                        .iterator().next()))
                {
                    return null;
                }
            }

        }
        return new Pair<>(latestKey, latestData);
    }

}
