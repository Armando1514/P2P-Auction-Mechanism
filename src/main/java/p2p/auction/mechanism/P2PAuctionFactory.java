package p2p.auction.mechanism;

public class P2PAuctionFactory {

    private P2PAuction factory;

    public P2PAuctionFactory(int id, String boot_peer, final MessageListener listener) throws Exception
    {
        factory =  new P2PAuction(id, boot_peer, listener);
    }

    public P2PAuction getP2PAuctionMechanism()
    {
        return this.factory;
    }
}
