package p2p.auction.mechanism.Control;

import net.tomp2p.futures.FutureDirect;
import net.tomp2p.peers.PeerAddress;
import p2p.auction.mechanism.DAO.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public interface AuctionMechanism {

// we need to now the id that is assigned to the auction.
     static Auction createAuction(Auction auction)
     {
          AuctionDAO auctionDAO = AuctionMechanismDAOFactory.getInstance().getAuctionDAO();
          try {
               PeerAddress peerAddress = AuctionMechanismDAOFactory.getInstance().getPeerAddress();
               auction.setParticipants(auction.getOwner().getNickname(),  peerAddress);
                return auctionDAO.create(auction);
          } catch (Exception e) {
               e.printStackTrace();
               return null;
          }
     }




     static Auction findAuction(Integer id)
     {
          AuctionDAO auctionDAO = AuctionMechanismDAOFactory.getInstance().getAuctionDAO();
          try {
               Auction auction = auctionDAO.read(id);
               if(auction == null)
               {
                    return null;
               }
               else
               {
                    if(auction.getStatus() == Auction.AuctionStatus.ONGOING && (!auction.checkStatus()))
                    {
                         AuctionMechanism.updateAuction(auction);
                    }
                    return auction;
               }
          } catch (Exception e) {
               e.printStackTrace();
               return null;
          }
     }


     static boolean updateAuction(Auction auction)
     {
          AuctionDAO auctionDAO = AuctionMechanismDAOFactory.getInstance().getAuctionDAO();
          try {
               auctionDAO.update(auction);
               return true;
          } catch (Exception e) {
               e.printStackTrace();
               return false;
          }
     }






     static void placeABid(AuctionBid bid) throws Exception
     {
          AuctionBidDAO auctionBidDAO = AuctionMechanismDAOFactory.getInstance().getAuctionBidDAO();
          if(bid.getAuction().checkStatus()) {
               auctionBidDAO.create(bid);
               AuctionMechanism.updateAuction(bid.getAuction());
               bid.getAuction().setParticipants(bid.getUser().getNickname(),AuctionMechanismDAOFactory.getInstance().getPeerAddress());
               AuctionMechanism.updateAuction(bid.getAuction());
               String message = "The user: "+bid.getUser().getNickname()+", has placed a bid of: "+bid.getBidValue()+", in the auction: "+bid.getAuction().getAuctionName()+"(id: "+bid.getAuction().getId()+").";
               AuctionMechanism.noticePeers(bid.getAuction(), message);

          }
          else
               throw new AuctionEndedException("The Auction is ended, is not possible to place a bid");


     }

     static void noticePeers(Auction auction,  String message) throws IOException, ClassNotFoundException {
          HashMap<String, PeerAddress> peers_on_topic = auction.getParticipants();
          Iterator<String> iterator = peers_on_topic.keySet().iterator();
          while(iterator.hasNext()){
               String user = iterator.next();
               PeerAddress peer = peers_on_topic.get(user);
               if(!peer.equals(AuctionMechanismDAOFactory.getInstance().getPeerAddress())) {
                    FutureDirect futureDirect = AuctionMechanismDAOFactory.getInstance().getDHT().peer().sendDirect(peer).object(message).start();
                    futureDirect.awaitUninterruptibly();
                    if(futureDirect.isFailed())
                    {
                        User notActiveUser = UserMechanism.findUser(user);
                        notActiveUser.setUnreadedMessages(message);
                        UserMechanism.updateUser(notActiveUser);
                    }
               }
          }

     }


     static String listAllAuctions()
     {
          AuctionDAO auctionDAO = AuctionMechanismDAOFactory.getInstance().getAuctionDAO();
          try {
               HashMap<Integer, Auction> auctions = auctionDAO.readAll();
               if(auctions == null)
               {
                    return null;
               }
               else
               {
                    String list ="";
                    Iterator<Map.Entry<Integer, Auction>> iterator = auctions.entrySet().iterator();
                    while(iterator.hasNext()) {
                         Map.Entry<Integer, Auction> auctionMap = iterator.next();
                         Auction auction = auctionMap.getValue();
                         if (auction.getStatus() == Auction.AuctionStatus.ONGOING && (!auction.checkStatus())) {
                              AuctionMechanism.updateAuction(auction);
                         }
                         if (auction.getSlots() != null && (!auction.getSlots().isEmpty())) {
                              AuctionBid lastBid = auction.getSlots().get(auction.getSlots().size()-1);
                              list +="id: "+auction.getId()+"\tname: " + auction.getAuctionName()+"\tstatus: " +auction.getStatus().toString()+"\tfastPrice: " +auction.getFastPrice()+"\tlast bid: "+ lastBid.getBidValue()+ "\n";
                         }
                         else
                         {
                              list +="id: "+auction.getId()+"\tname: " + auction.getAuctionName()+"\tstatus: " +auction.getStatus().toString()+"\tfastPrice: " +auction.getFastPrice()+"\tlast bid: none \n";

                         }

                    }
                    return list;
               }
          } catch (Exception e) {
               e.printStackTrace();
               return null;
          }
     }

}
