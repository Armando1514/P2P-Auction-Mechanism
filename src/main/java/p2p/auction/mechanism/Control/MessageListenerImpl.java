package p2p.auction.mechanism.Control;

import org.beryx.textio.TerminalProperties;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import p2p.auction.mechanism.DAO.NotificationMessage;
import p2p.auction.mechanism.GUI.AuctionGUI;
import p2p.auction.mechanism.MessageListener;

public class MessageListenerImpl implements MessageListener {


    public String parseMessage(Object obj) {

        TextIO textIO = TextIoFactory.getTextIO();
        TextTerminal terminal = textIO.getTextTerminal();
        TerminalProperties< ? > props = terminal.getProperties();
        props.setPromptColor("yellow");
        props.setPromptBold(true);
        props.setPromptUnderline(true);
        NotificationMessage not = (NotificationMessage) obj;
        terminal.println("\nNew notification ~ " + not.getMessage() + "\n");

        if ((not.getType() == NotificationMessage.MessageType.WIN) && not.getBid().getUser().getNickname().equals(AuctionGUI.getUser().getNickname()))
            AuctionGUI.updateAuctionWon(not);

        props.setPromptBold(false);
        props.setPromptUnderline(false);
        props.setPromptColor("#00ff00");
        return "success";
    }

}

