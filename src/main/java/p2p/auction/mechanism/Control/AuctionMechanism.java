package p2p.auction.mechanism.Control;

import p2p.auction.mechanism.DAO.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public interface AuctionMechanism {

// we need to now the id that is assigned to the auction.
     static Auction createAuction(Auction auction)
     {
          AuctionDAO auctionDAO = AuctionMechanismDAOFactory.getInstance().getAuctionDAO();
          try {
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

     static boolean placeABid(AuctionBid bid)
     {
          AuctionBidDAO auctionBidDAO = AuctionMechanismDAOFactory.getInstance().getAuctionBidDAO();
          try {
                auctionBidDAO.create(bid);
               return true;
          } catch (Exception e) {
               e.printStackTrace();
               return false;
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
                              AuctionBid lastBid = auction.getSlots().get(0);
                              list +="id: "+auction.getId()+"\t-name: " + auction.getAuctionName()+"\t-status: " +auction.getStatus()+"\t-fastPrice: " +auction.getFastPrice()+"\t-last bid: "+ lastBid.getBidValue()+ "\n";
                         }
                         else
                         {
                              list +="id: "+auction.getId()+"\t-name: " + auction.getAuctionName()+"\t-status: " +auction.getStatus()+"\t-fastPrice: " +auction.getFastPrice()+"\t-last bid: none \n";

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
