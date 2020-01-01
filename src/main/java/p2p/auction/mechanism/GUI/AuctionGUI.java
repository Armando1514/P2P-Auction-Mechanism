package p2p.auction.mechanism.GUI;

import org.beryx.textio.*;
import p2p.auction.mechanism.Control.AuctionMechanism;
import p2p.auction.mechanism.Control.UserMechanism;
import p2p.auction.mechanism.DAO.Auction;
import p2p.auction.mechanism.DAO.AuctionBid;
import p2p.auction.mechanism.DAO.User;

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



        String keyStrokeQuit = "ctrl Q";

        TerminalProperties<?> props = terminal.getProperties();
        boolean quitStroke = terminal.registerHandler(keyStrokeQuit, t -> {

            this.quitGUI();

            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });

        boolean createAuctionStroke = terminal.registerHandler(keyStrokeCreateAuction, t -> {
            if(this.createAuctionGUI() == null)
                terminal.println("A strange error has occurred, retry after.");
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
        });

        boolean listAuctionsStroke = terminal.registerHandler(keyStrokeListAuctions, t -> {
            this.listAuctionsGUI();
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
        });

        boolean placeABidStroke = terminal.registerHandler(keyStrokePlaceABid, t -> {
            if(this.placeABid() == false)
                terminal.println("A strange error has occurred, retry after.");
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
        });

        boolean viewAuctionStroke = terminal.registerHandler(keyStrokeViewAuction, t -> {
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

        boolean hasHandlers = createAuctionStroke || viewAuctionStroke || listAuctionsStroke;
        if(!hasHandlers) {
            terminal.println("No handlers can be registered.");
        } else {
            props.setPromptBold(true);
            props.setPromptColor("cyan");
            terminal.println("WELCOME " + user.getNickname() +", YOU HAVE: " + user.getMoney() + " $");
            props.setPromptColor("#00ff00");
            props.setPromptBold(false);

            terminal.println("--------------------------------------------------------------------------------");
            if(quitStroke) {
                terminal.println("Press " + keyStrokeCreateAuction + " to create an auction.");
                terminal.println("Press " + keyStrokeListAuctions + " for the list of auctions.");
                terminal.println("Press " + keyStrokeViewAuction + " to view an auction.");
                terminal.println("Press " + keyStrokePlaceABid + " to place a bid.");

            }
            terminal.println("You can use these key combinations at any moment during the session.");
            terminal.println("--------------------------------------------------------------------------------");


            terminal.setBookmark("auction");

        }
        terminal.resetToBookmark("auction");

        textIO.newStringInputReader().withPattern("(?i)(?<= |^)exit(?= |$)").read("\nWrite 'exit' to terminate...");


        textIO.dispose();


    }

    private boolean placeABid()
    {
        terminal.resetToBookmark("auction");
        TerminalProperties<?> props = terminal.getProperties();
        props.setPromptColor("red");
        terminal.resetLine();

        terminal.println("PLACE A BID:");
        props.setPromptColor("#00ff00");
        Integer auctionId = textIO.newIntInputReader()
                .read("Auction ID");
        Auction auction = AuctionMechanism.findAuction(auctionId);
        if( auction == null)
        {
            terminal.resetLine();
            terminal.println("Auction does not found");
        }
        else
        {
            if(auction.getStatus() == Auction.AuctionStatus.ENDED) {
                terminal.resetLine();
                terminal.println("Auction ENDED, is not allowed to place a bid.");
            }
            else
            {
                terminal.resetLine();
                terminal.println("If you want get the product without competing. Get it faster, place a bid of: " + auction.getFastPrice());
                Double bidValue = textIO.newDoubleInputReader()
                        .read("Bid value");
                AuctionBid bid = new AuctionBid(auction, user, bidValue);
                if(AuctionMechanism.placeABid(bid))
                {
                    props.setPromptColor("red");
                    terminal.resetLine();
                    if(bidValue  > auction.getFastPrice())
                    {

                        terminal.println("You got "+auction.getAuctionName()+", at the fast price of: "+auction.getFastPrice()+ "congratulations.");
                    }
                    else
                    {
                        terminal.println("You have placed a bid in the auction:  "+auction.getAuctionName()+", of the value: "+bidValue);

                    }
                    props.setPromptColor("#00ff00");

                }
                else
                    return false;
            }
        }

        return true;
    }

    private Auction viewAuctionGUI()
    {
        terminal.resetToBookmark("auction");
        terminal.resetLine();
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
        terminal.resetToBookmark("auction");
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
        terminal.resetToBookmark("auction");
        terminal.resetLine();
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

   /*REMEMBER FOR GET DATE
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.systemDefault()));
        cal.setTime(date);*/


        auction.setExpirationDate(date);

        double fastPrice = textIO.newDoubleInputReader()
                .read("Fast price");

            auction.setFastPrice(fastPrice);

            auction.setOwner(user);
        auction = AuctionMechanism.createAuction(auction);
        if( auction != null) {
            props.setPromptColor("red");
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
            System.out.println(date);
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
        int confirmed = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to exit the program?", "Exit message",
                JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
        else{
            terminal.resetToBookmark("reset");
            this.createAuctionGUI();

        }

    }
}
