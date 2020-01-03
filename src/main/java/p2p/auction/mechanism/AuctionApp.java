package p2p.auction.mechanism;

import org.beryx.textio.TerminalProperties;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import p2p.auction.mechanism.DAO.AuctionMechanismDAOFactory;
import p2p.auction.mechanism.DAO.User;
import p2p.auction.mechanism.GUI.AuctionGUI;
import p2p.auction.mechanism.GUI.AuthenticationGUI;

public class AuctionApp {

    @Option(name="-m", aliases="--masterip", usage="the master peer ip address", required=false)
    private static String master;

    @Option(name="-id", aliases="--identifierpeer", usage="the unique identifier for this peer", required=false)
    private static int id;

    public static void main(String[] args) {


        class MessageListenerImpl implements MessageListener{
            int peerid;

            public MessageListenerImpl(int peerid)
            {
                this.peerid=peerid;

            }
            public Object parseMessage(Object obj) {

                TextIO textIO = TextIoFactory.getTextIO();
                TextTerminal terminal = textIO.getTextTerminal();
                TerminalProperties<?> props = terminal.getProperties();
                props.setPromptColor("yellow");
                props.setPromptBold(true);
                props.setPromptUnderline(true);
                terminal.println("\nNew notification ~ "+obj+"\n\n");
                props.setPromptBold(false);
                props.setPromptUnderline(false);
                props.setPromptColor("#00ff00");
                return "success";
            }

        }

        AuctionApp example = new AuctionApp();
        final CmdLineParser parser = new CmdLineParser(example);
        try {

            if(master == null)
            {
                master = "127.0.0.1";
            }
            parser.parseArgument(args);
            AuctionMechanismDAOFactory.instantiate(id, master, new MessageListenerImpl(id), false);
             TextIO textIO = TextIoFactory.getTextIO();
            TextTerminal<?> terminal =  textIO.getTextTerminal();
            terminal.setBookmark("reset");

           User user = new AuthenticationGUI(textIO, terminal).authenticationGUIDisplay();
            terminal.resetToBookmark("reset");
            if(!user.getUnreadedMessages().isEmpty()) {
                TerminalProperties<?> props = terminal.getProperties();
                props.setPromptColor("red");
                terminal.println("You have: " + user.getUnreadedMessages().size() + " messages unreaded, press Ctrl U for read them.");
                props.setPromptColor("#00ff00");
            }

            new AuctionGUI(textIO,terminal, user).AuctionGUIDisplay();

        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
