package p2p.auction.mechanism.GUI;

import org.beryx.textio.*;
import p2p.auction.mechanism.Control.AuctionMechanism;
import p2p.auction.mechanism.Control.UserMechanism;
import p2p.auction.mechanism.DAO.*;

import javax.swing.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.ZoneId;
import java.util.*;

public class AuctionGUI {

    private TextTerminal<?> terminal ;
    private TextIO textIO;
    private User user;

    public   AuctionGUI(TextIO textIO, TextTerminal<?> terminal, User user)
    {
        this.textIO = textIO;
        this.terminal =  terminal;
        this.user = user;
    }


    public void AuctionGUIDisplay() {


        String keyStrokeCreateAuction = "ctrl C";

        String keyStrokeViewAuction = "ctrl V";

        String keyStrokeListAuctions = "ctrl L";

        String keyStrokePlaceABid = "ctrl P";

        String keyStrokeQuit = AuthenticationGUI.keyStrokeQuit;


        String keyStrokeCheckUserStatus = "ctrl S";



        TerminalProperties<?> props = terminal.getProperties();


        boolean quitStroke = terminal.registerHandler(keyStrokeQuit, t -> {
            this.quitGUI();
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART);
        });

        boolean createAuctionStroke = terminal.registerHandler(keyStrokeCreateAuction, t -> {
            terminal.resetToBookmark("auction");
            if(this.createAuctionGUI() == null)
                terminal.println("A strange error has occurred, retry after.");
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART);
        });

        boolean listAuctionsStroke = terminal.registerHandler(keyStrokeListAuctions, t -> {
            terminal.resetToBookmark("auction");

            this.listAuctionsGUI();
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
        });

        boolean placeABidStroke = terminal.registerHandler(keyStrokePlaceABid, t -> {
            terminal.resetToBookmark("auction");

            if(this.placeABidGUI() == false)
                terminal.println("A strange error has occurred, retry after.");
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
        });


        boolean viewAuctionStroke = terminal.registerHandler(keyStrokeViewAuction, t -> {
            terminal.resetToBookmark("auction");

            Auction auction = this.viewAuctionGUI();
            if( auction== null)
                terminal.println("Auction does not found.");
            else
            {
                props.setPromptColor("cyan");
                terminal.resetToBookmark("auction");
                terminal.println("Auction id: " + auction.getId());
                terminal.println("Auction name: " + auction.getAuctionName());
                terminal.println("Status: " + auction.getStatus());
                terminal.println("Owner: " + auction.getOwner().getNickname());
                terminal.println("Expiration Date: " +  calendarFormat(auction.getExpirationDate()) );
                terminal.println("Fast price: " + auction.getFastPrice());
                terminal.println("Auction creation time: " + this.calendarFormat(auction.getCreationDate()));

                int i = 0;

                ArrayList<AuctionBid> slots = auction.getSlots();
                while(i < (slots).size())
                {
                    AuctionBid auctionBid = slots.get(i);
                    terminal.println("Bid number: "+i+", from user:" + auctionBid.getUser().getNickname()+", value: "+ auctionBid.getBidValue());
                    i++;
                }

                props.setPromptColor("#00ff00");
            }

            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
        });

        boolean userStatusStroke = terminal.registerHandler(keyStrokeCheckUserStatus, t -> {
            terminal.resetToBookmark("auction");
            terminal.moveToLineStart();

            this.userStatusGUI();
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
        });

        boolean hasHandlers = createAuctionStroke || viewAuctionStroke || listAuctionsStroke || placeABidStroke || userStatusStroke || quitStroke;
        if(!hasHandlers) {
            terminal.println("No handlers can be registered.");
        } else {
            props.setPromptBold(true);
            props.setPromptColor("cyan");
            terminal.println("WELCOME " + user.getNickname());
            props.setPromptColor("#00ff00");
            props.setPromptBold(false);

            terminal.println("--------------------------------------------------------------------------------");
                terminal.println("Press " + keyStrokeCheckUserStatus + " to check your user status.");
                terminal.println("Press " + keyStrokeCreateAuction + " to create an auction.");
                terminal.println("Press " + keyStrokeListAuctions + " for the list of auctions.");
                terminal.println("Press " + keyStrokeViewAuction + " to view an auction.");
                terminal.println("Press " + keyStrokePlaceABid + " to place a bid.");
                terminal.println("Press " + keyStrokeQuit + " to quit.");


            terminal.println("You can use these key combinations at any moment during the session.");
            terminal.println("--------------------------------------------------------------------------------");


            terminal.setBookmark("auction");

        }
        terminal.resetToBookmark("auction");

        textIO.newStringInputReader().withPattern("(?i)(?<= |^)exit(?= |$)").read("\nWrite 'exit' to terminate...");


        textIO.dispose();


    }

    private void userStatusGUI()
    {


        TerminalProperties<?> props = terminal.getProperties();
        props.setPromptColor("red");
        terminal.resetLine();

        terminal.println("USER STATUS:");
        props.setPromptColor("cyan");

        terminal.println("Nickname: "+user.getNickname());
        terminal.println("Money: "+user.getMoney());
        if(!user.getMyAuctions().isEmpty()){
            ArrayList<Auction> myAuctions = user.getMyAuctions();
            int i = 0;
            String list = "";
            while(i < myAuctions.size()) {
                Auction auction =myAuctions.get(i);
                list += "id: " + auction.getId() + "\tname: " + auction.getAuctionName() +"\n";
            i++;
            }
            terminal.println("MY AUCTIONS:");
            terminal.println(list);
        }
        if(!user.getAuctionsJoined().isEmpty()){
            ArrayList<Auction> myAuctionsJoined = user.getAuctionsJoined();
            int i = 0;
            String list = "";
            while(i < myAuctionsJoined.size()) {
                Auction auction = myAuctionsJoined.get(i);
                list += "id: " + auction.getId() + "\tname: " + auction.getAuctionName() + "\n";
            i++;
            }

            terminal.println("JOINED AUCTIONS:");
            terminal.println(list);
        }
        props.setPromptColor("#00ff00");


    }

    private boolean placeABidGUI()
    {
        TerminalProperties<?> props = terminal.getProperties();
        props.setPromptColor("red");

        terminal.println("PLACE A BID:");
        props.setPromptColor("#00ff00");
        Integer auctionId = textIO.newIntInputReader()
                .read("Auction ID");
        Auction auction = AuctionMechanism.findAuction(auctionId);
        if( auction == null)
        {
          terminal.println("Auction does not found");
        }
        else
        {
            if(auction.getStatus() == Auction.AuctionStatus.ENDED) {
                props.setPromptColor("red");

                terminal.println("Auction ENDED, is not allowed to place a bid.");
            }
            else
            {
                if(auction.getFastPrice() != null)
                terminal.println("If you want get the product without competing. Get it faster, place a bid of: " + auction.getFastPrice());
                Double bidValue = textIO.newDoubleInputReader()
                        .read("Bid value");
                AuctionBid bid = new AuctionBid(auction, user, bidValue);
                try {
                    props.setPromptColor("red");

                   AuctionMechanism.placeABid(bid);
                        if(auction.getFastPrice() != null) {
                            if (bidValue >= auction.getFastPrice()) {
                                props.setPromptColor("cyan");
                                terminal.println("You got " + auction.getAuctionName() + ", at the fast price of: " + auction.getFastPrice() + ", congratulations.");
                                props.setPromptColor("#00ff00");

                            }
                            else {
                                props.setPromptColor("cyan");
                                terminal.println("You have placed a bid in the auction:  " + auction.getAuctionName() + ", of the value: " + bidValue);
                                props.setPromptColor("#00ff00");
                            }
                        }
                         else {
                            props.setPromptColor("cyan");
                            terminal.println("You have placed a bid in the auction:  " + auction.getAuctionName() + ", of the value: " + bidValue);
                            props.setPromptColor("#00ff00");
                        }
                         if(!user.getAuctionsJoined().contains(bid.getAuction())) {
                             user.setAuctionsJoined(bid.getAuction());
                             UserMechanism.updateUser(user);
                         }
                }
                catch (HigherBidExistenceException e) {
                terminal.println(e.getMessage());
            }
                catch (AuctionEndedException e) {
                    terminal.println(e.getMessage());
            } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }


            }
            props.setPromptColor("#00ff00");

            }


        return true;
    }

    private Auction viewAuctionGUI()
    {
        TerminalProperties<?> props = terminal.getProperties();
        props.setPromptColor("red");
        terminal.resetLine();

        terminal.println("VIEW AUCTION:");
        props.setPromptColor("#00ff00");
        Integer auctionId = textIO.newIntInputReader()
                .read("Auction ID");
        return AuctionMechanism.findAuction(auctionId);

    }

    private void listAuctionsGUI()
    {
        TerminalProperties<?> props = terminal.getProperties();

        props.setPromptColor("red");
        terminal.resetLine();

        terminal.println("AUCTIONS LIST:");
        props.setPromptColor("#00ff00");
        props.setPromptColor("cyan");
        String list = AuctionMechanism.listAllAuctions();
        terminal.resetLine();
        if(list != null)
        terminal.println(list);
        else
            terminal.println("There are no auctions.");
        props.setPromptColor("#00ff00");
    }

    private Auction createAuctionGUI()
    {

        TerminalProperties<?> props = terminal.getProperties();
        props.setPromptColor("red");
        terminal.moveToLineStart();

        terminal.println("CREATE AUCTION:");
        props.setPromptColor("#00ff00");

        Auction auction = new Auction();


        String auctionName = textIO.newStringInputReader()
                .withMinLength(4).withPattern("(?!^\\d+$)^.+$")
                .read("Auction Name");

        auction.setAuctionName(auctionName);

        terminal.println("Expiration date:");
        int day = textIO.newIntInputReader()
                .withMinVal(1).withMaxVal(31)
                .read("Day");
        Month month = textIO.newEnumInputReader(Month.class)
                .read("Month");
        int year = textIO.newIntInputReader().withMaxVal(9999)
                .withMinVal(Calendar.getInstance().get(Calendar.YEAR))
                .read("Year");
        int hours = textIO.newIntInputReader()
                .withMinVal(0).withMaxVal(24)
                .read("Hours (0 to 24)");
        int minutes  = textIO.newIntInputReader()
                .withMinVal(0).withMaxVal(60)
                .read("Minutes (0 to 60)");
        Date date = parseDate(day+"/"+month+"/"+year+" "+hours+":"+minutes);
        auction.setExpirationDate(date);


        auction.setExpirationDate(date);

        double fastPrice = textIO.newDoubleInputReader()
                .read("Fast price ( -1 if you don't want this option)");

            auction.setFastPrice(fastPrice);

            auction.setOwner(user);
        auction = AuctionMechanism.createAuction(auction);
        if( auction != null) {
            props.setPromptColor("cyan");
            terminal.resetToBookmark("auction");
            terminal.println("Auction with id: '"+auction.getId()+"', correctly created.");
            props.setPromptColor("#00ff00");
            user.setMyAuctions(auction);
            UserMechanism.updateUser(user);
            return auction;
        }
        else
            return null;
    }


    private  Date parseDate(String date) {
        try {
            return new SimpleDateFormat("dd/MMMM/yyyy HH:mm", Locale.US).parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    private String calendarFormat(Date date)
    {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.systemDefault()));
        cal.setTime(date);
        return cal.get(Calendar.HOUR_OF_DAY) +":"+cal.get(Calendar.MINUTE)+" "+cal.get(Calendar.DAY_OF_MONTH)+"/"+cal.get(Calendar.MONTH)+"/"+cal.get(Calendar.YEAR);
    }

    private void quitGUI()
    {

            System.exit(0);


    }
}
