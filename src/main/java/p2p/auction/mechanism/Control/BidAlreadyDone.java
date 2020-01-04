package p2p.auction.mechanism.Control;


public class BidAlreadyDone extends Exception {

    BidAlreadyDone(String message) {

        super(message);
    }
}