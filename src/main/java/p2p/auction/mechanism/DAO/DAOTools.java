package p2p.auction.mechanism.DAO;

import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.utils.Pair;

import java.util.Map;

public interface  DAOTools {


    default  <K> Pair<Number640, K> checkVersions(Map<PeerAddress, Map<Number640, K>> rawData)
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

}
