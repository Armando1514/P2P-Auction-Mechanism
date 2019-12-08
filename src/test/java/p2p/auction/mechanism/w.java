package p2p.auction.mechanism;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.peers.Number160;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

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
            final P2PAuction peer0 = new P2PAuction(0, "127.0.0.1", new MessageListenerImpl(0));

            final P2PAuction peer1 = new P2PAuction(1, "127.0.0.1", new MessageListenerImpl(1));

            final P2PAuction peer2 = new P2PAuction(2, "127.0.0.1", new MessageListenerImpl(2));

            final P2PAuction peer3 = new P2PAuction(3, "127.0.0.1", new MessageListenerImpl(3));

            Double x = new Double(1);
            AuctionUser user = new AuctionUser("test","password", x, null, null);
            peer0.createAuction("Nino",user, new Date(),324, "ciao");

            final CountDownLatch cl = new CountDownLatch(4);
            final Double x0 = new Double(0);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        peer0.placeAbid("Nino",x0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cl.countDown();
                }
            }).start();
            final Double x1 = new Double(1);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        peer1.placeAbid("Nino",x1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cl.countDown();
                }
            }).start();
            final Double x2 = new Double(2);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        peer2.placeAbid("Nino",x2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cl.countDown();
                }
            }).start();
            final Double x3 = new Double(4);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        peer3.placeAbid("Nino",x3);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cl.countDown();
                }
            }).start();

            // wait until all 3 threads are finished
            cl.await();
            // get latest version
            System.out.println("1: "+ peer0.getElement("Nino").getSlots().get(0).getBidValue());
            System.out.println("2: "+ peer0.getElement("Nino").getSlots().get(1).getBidValue());
            System.out.println("3: "+ peer0.getElement("Nino").getSlots().get(2).getBidValue());
            System.out.println("4: "+ peer0.getElement("Nino").getSlots().get(3).getBidValue());


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
