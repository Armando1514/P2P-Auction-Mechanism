package p2p.auction.mechanism.Control;

import net.tomp2p.futures.FutureDirect;
import net.tomp2p.peers.PeerAddress;
import p2p.auction.mechanism.DAO.*;

import java.util.HashMap;
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
          }
          catch (Exception e) {
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

               return false;
          }
     }






     static void placeABid(AuctionBid bid) throws Exception
     {
          AuctionBidDAO auctionBidDAO = AuctionMechanismDAOFactory.getInstance().getAuctionBidDAO();
          if(bid.getAuction().checkStatus()) {
               if(!bid.getAuction().getSlots().isEmpty())
               {
                    String lastBidUser = bid.getAuction().getSlots().get(bid.getAuction().getSlots().size()-1).getUser().getNickname();
                    if(lastBidUser.equals(bid.getUser().getNickname()))
                         throw new BidAlreadyDone("The latest offer created is yours, you cannot compete alone.");
               }
               auctionBidDAO.create(bid);

               NotificationMessage not = new NotificationMessage();
               not.setBid(bid);
               String message = "The user: "+bid.getUser().getNickname()+", has placed a bid of: "+bid.getBidValue()+", in the auction: "+bid.getAuction().getAuctionName()+"(id: "+bid.getAuction().getId()+").";
               not.setMessage(message);
               not.setType(NotificationMessage.MessageType.BID);
               AuctionMechanism.noticePeers(not);

          }
          else
               throw new AuctionEndedException("The Auction is ended, is not possible to place a bid");

     }

     static void noticePeers(NotificationMessage not) {
          HashMap<String, PeerAddress> peers_on_topic = not.getBid().getAuction().getParticipants();
          for (String user : peers_on_topic.keySet()) {
               PeerAddress peer = peers_on_topic.get(user);

               if ((!peer.equals(AuctionMechanismDAOFactory.getInstance().getPeerAddress())) || (not.getType() == NotificationMessage.MessageType.WIN)) {
                    FutureDirect futureDirect = AuctionMechanismDAOFactory.getInstance().getDHT().peer().sendDirect(peer).object(not).start();
                    futureDirect.awaitUninterruptibly();
                    if (futureDirect.isFailed()) {

                         User notActiveUser = UserMechanism.findUser(user);
                         assert notActiveUser != null;
                         notActiveUser.setUnreadedMessages(not.getMessage());
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
                    StringBuilder list = new StringBuilder();
                    for (Map.Entry<Integer, Auction> auctionMap : auctions.entrySet()) {
                         Auction auction = auctionMap.getValue();
                         if (auction.getStatus() == Auction.AuctionStatus.ONGOING && (!auction.checkStatus())) {
                              AuctionMechanism.updateAuction(auction);
                         }
                         if (auction.getSlots() != null && (!auction.getSlots().isEmpty())) {
                              AuctionBid lastBid = auction.getSlots().get(auction.getSlots().size() - 1);
                              list.append("id: ").append(auction.getId()).append("\tname: ").append(auction.getAuctionName()).append("\tstatus: ").append(auction.getStatus().toString()).append("\tfastPrice: ").append(auction.getFastPrice()).append("\tlast bid: ").append(lastBid.getBidValue()).append("\n");
                         } else {
                              list.append("id: ").append(auction.getId()).append("\tname: ").append(auction.getAuctionName()).append("\tstatus: ").append(auction.getStatus().toString()).append("\tfastPrice: ").append(auction.getFastPrice()).append("\tlast bid: none \n");

                         }

                    }
                    return list.toString();
               }
          } catch (Exception e) {

               return null;
          }
     }

}
