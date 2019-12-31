package p2p.auction.mechanism.GUI;

import org.beryx.textio.*;
import p2p.auction.mechanism.Control.AuctionControl;
import p2p.auction.mechanism.Control.AuctionMechanism;
import p2p.auction.mechanism.Control.UserControl;
import p2p.auction.mechanism.Control.UserMechanism;
import p2p.auction.mechanism.DAO.Auction;
import p2p.auction.mechanism.DAO.User;

import javax.swing.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
            this.createAuctionGUI();
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
        });



        boolean hasHandlers = createAuctionStroke ;
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
                terminal.println("Press " + keyStrokeQuit + " to exit");
            }
            terminal.println("You can use these key combinations at any moment during your authentication entry session.");
            terminal.println("--------------------------------------------------------------------------------");


            terminal.setBookmark("auction");

        }
        terminal.resetToBookmark("auction");

        textIO.newStringInputReader().withPattern("(?i)(?<= |^)exit(?= |$)").read("\nWrite 'exit' to terminate...");


        textIO.dispose();


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

        AuctionMechanism control = new AuctionControl();

        String auctionName = textIO.newStringInputReader()
                .withMinLength(4).withPattern("^(?![0-9]*$)[a-zA-Z0-9]+$")
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


        if(control.createAuction(auction)) {

            terminal.println("Auction correctly created.");
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
