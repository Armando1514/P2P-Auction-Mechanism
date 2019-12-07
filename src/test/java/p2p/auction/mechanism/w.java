package p2p.auction.mechanism;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.peers.Number160;

import java.io.IOException;
import java.util.Date;

public class w {
    public static void main(String[] args) throws Exception {
        class MessageListenerImpl implements MessageListener{
            int peerid;
            public MessageListenerImpl(int peerid)
            {
                this.peerid=peerid;
            }
            public Object parseMessage(Object obj) {
                System.out.println(peerid+"] (Direct Message Received) "+obj);
                return "success";
            }

        }

        try {
            P2PAuction peer0 = new P2PAuction(0, "127.0.0.1", new MessageListenerImpl(0));

            P2PAuction peer1 = new P2PAuction(1, "127.0.0.1", new MessageListenerImpl(1));

            P2PAuction peer2 = new P2PAuction(2, "127.0.0.1", new MessageListenerImpl(2));

            P2PAuction peer3 = new P2PAuction(3, "127.0.0.1", new MessageListenerImpl(3));

            Double x = new Double(23);
            AuctionUser user = new AuctionUser("test","password", x, null, null);
            peer0.createAuction("Nino",user, new Date(),324, "ciao");
            Auction auction = peer3.getElement("Nino");
            System.out.println(auction.getAuctionName());

            x = new Double(293);
            peer1.placeAbid("Nino",x);
             auction = peer3.getElement("Nino");
            System.out.println(auction.getSlots().get(0).getBidValue());




        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
