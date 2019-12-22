package p2p.auction.mechanism;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.util.Date;

public class ConsoleControl {
    @Option(name="-m", aliases="--masterip", usage="the master peer ip address", required=true)
    private static String master;

    @Option(name="-id", aliases="--identifierpeer", usage="the unique identifier for this peer", required=true)
    private static int id;


    public static void main(String[] args) throws Exception {
        class MessageListenerImpl implements MessageListener {
            int peerid;

            public MessageListenerImpl(int peerid) {
                this.peerid = peerid;

            }
            public Object parseMessage(Object obj) {

                TextIO textIO = TextIoFactory.getTextIO();
                TextTerminal terminal = textIO.getTextTerminal();
                terminal.printf("\n" + peerid + "] (Direct Message Received) " + obj + "\n\n");
                return "success";
            }

        }

        ConsoleControl console = new ConsoleControl();
        final CmdLineParser parser = new CmdLineParser(console);
        try {
            parser.parseArgument(args);
            TextIO textIO = TextIoFactory.getTextIO();
            TextTerminal terminal = textIO.getTextTerminal();
            P2PAuction peer = new P2PAuctionFactory(id, master, new MessageListenerImpl(id)).getP2PAuctionMechanism();

            terminal.printf("\nStaring peer id: %d on master node: %s\n",
                    id, master);
            while (true) {
                printMenu(terminal);

                int option = textIO.newIntInputReader()
                        .withMaxVal(5)
                        .withMinVal(1)
                        .read("Option");
                switch (option) {
                    case 1:
                        terminal.printf("\nENTER TOPIC NAME\n");
                        String name = textIO.newStringInputReader()
                                .withDefaultValue("default-topic")
                                .read("Name:");
                        Double x = new Double(23);
                       AuctionUser user = new AuctionUser("test","password", x, null, null);
                        if (peer.createAuction(name,user, new Date(),324, "ciao"))
                            terminal.printf("\nTOPIC %s SUCCESSFULLY CREATED\n", name);
                        else
                            terminal.printf("\nERROR IN TOPIC CREATION\n");
                        break;
                    case 2:
                        terminal.printf("\nENTER TOPIC NAME\n");
                        String sname = textIO.newStringInputReader()
                                .withDefaultValue("default-topic")
                                .read("Name:");
                        peer.placeABid(sname, 3);
                    terminal.printf("\n SUCCESSFULLY SUBSCRIBED TO %s\n", sname);
                        break;



                    default:
                        break;
                }
            }



        } catch (CmdLineException clEx) {
            System.err.println("ERROR: Unable to parse command-line options: " + clEx);
        }


    }
    public static void printMenu(TextTerminal terminal) {
        terminal.printf("\n1 - CREATE TOPIC\n");
        terminal.printf("\n2 - SUBSCRIBE TOPIC\n");
        terminal.printf("\n3 - UN SUBSCRIBE ON TOPIC\n");
        terminal.printf("\n4 - PUBLISH ON TOPIC\n");
        terminal.printf("\n5 - EXIT\n");

    }
}
