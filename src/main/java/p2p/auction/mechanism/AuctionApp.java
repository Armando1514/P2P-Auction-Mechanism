package p2p.auction.mechanism;

import org.beryx.textio.TerminalProperties;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import p2p.auction.mechanism.Control.MessageListenerImpl;
import p2p.auction.mechanism.DAO.AuctionMechanismDAOFactory;

import p2p.auction.mechanism.DAO.NotificationMessage;
import p2p.auction.mechanism.GUI.AuctionGUI;
import p2p.auction.mechanism.GUI.AuthenticationGUI;

public class AuctionApp {

    @Option(name = "-m", aliases = "--masterip", usage = "the master peer ip address", required = true)
    private static String master;

    @Option(name = "-tz", aliases = "--timezone", usage = "the local timezone", required = true)
    private static String timezone;

    @Option(name = "-id", aliases = "--identifierpeer", usage = "the unique identifier for this peer", required = true)
    private static int id;


    public static void main(String[] args) {





        AuctionApp example = new AuctionApp();
        final CmdLineParser parser = new CmdLineParser(example);
        try {

            parser.parseArgument(args);


            AuctionMechanismDAOFactory.instantiate(id, master, new MessageListenerImpl(), false);
            TextIO textIO = TextIoFactory.getTextIO();

            TextTerminal < ? > terminal = textIO.getTextTerminal();

            terminal.setBookmark("reset");

            new AuthenticationGUI(textIO, terminal).authenticationGUIDisplay();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getTimezone() {

        return timezone;
    }
}