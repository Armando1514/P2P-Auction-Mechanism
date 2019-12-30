package p2p.auction.mechanism.GUI;
import org.beryx.textio.*;

import javax.swing.*;

import p2p.auction.mechanism.DAO.User;

public class AuthenticationGUI {



    private TextTerminal<?> terminal ;
    private TextIO textIO;

public   AuthenticationGUI(TextIO textIO, TextTerminal<?> terminal)
{
    this.textIO = textIO;
    this.terminal =  terminal;
}


    public void authenticationGUIDisplay() {


        String keyStrokeLogin = "ctrl L";
        String keyStrokeRegister = "ctrl R";
        String keyStrokeQuit = "ctrl Q";

        TerminalProperties<?> props = terminal.getProperties();
        boolean quitStroke = terminal.registerHandler(keyStrokeQuit, t -> {

            this.quitGUI();

            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });

        boolean registerStroke = terminal.registerHandler(keyStrokeRegister, t -> {
            this.registerGUI();
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
        });
        boolean loginStroke = terminal.registerHandler(keyStrokeLogin, t -> {

            this.loginGUI();

            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
        });


        boolean hasHandlers = loginStroke || registerStroke || quitStroke ;
        if(!hasHandlers) {
            terminal.println("No handlers can be registered.");
        } else {
            props.setPromptBold(true);
            props.setPromptColor("cyan");
            terminal.println("WELCOME TO THE AUCTION SYSTEM");
            props.setPromptUnderline(true);
            props.setPromptColor("red");
            terminal.println("YOU NEED TO PROVIDE AN AUTHENTICATION.");
            props.setPromptColor("#00ff00");
            props.setPromptUnderline(false);
            props.setPromptBold(false);

            terminal.println("--------------------------------------------------------------------------------");
            if(quitStroke) {
                terminal.println("Press " + keyStrokeQuit + " to exit");
            }
            if(registerStroke) {
                terminal.println("Press " + keyStrokeRegister + " to register");
            }
            if(loginStroke) {
                terminal.println("Press " + keyStrokeLogin + " to login");
            }
            terminal.println("You can use these key combinations at any moment during your authentication entry session.");
            terminal.println("--------------------------------------------------------------------------------");


            terminal.setBookmark("authentication");

        }
        terminal.resetToBookmark("authentication");

        textIO.newStringInputReader()
                .withNumberedPossibleValues(keyStrokeLogin, keyStrokeRegister, keyStrokeQuit)
                .read("Waiting a command ...");

        textIO.dispose();


    }


    private User registerGUI()
    {
        terminal.resetToBookmark("authentication");
        terminal.resetLine();
        TerminalProperties<?> props = terminal.getProperties();
        props.setPromptColor("red");
        terminal.moveToLineStart();

        terminal.println("REGISTER:");
        props.setPromptColor("#00ff00");

        boolean nickRight = false;
        User user = new User();

        while(!nickRight) {
            String nickname = textIO.newStringInputReader()
                    .withMinLength(4).withPattern("^(?![0-9]*$)[a-zA-Z0-9]+$")
                    .read("Username");
            user.setNickname(nickname.toLowerCase());
            nickRight = user.storeUser();
            if(!nickRight)
                terminal.println("Username already exists, change it!");

        }
        String password = textIO.newStringInputReader()
                .withMinLength(6)
                .withInputMasking(true)
                .read("Password");
        terminal.println();
        user.setPassword(password);

        double money = textIO.newIntInputReader()
                .withMinVal(1)
                .read("Initial money");
        user.setMoney(new Double(money));
        if(user.updateUser())
            terminal.println("User correctly created.");

        return user;
    }

    private User loginGUI()
    {
        terminal.resetToBookmark("authentication");
        terminal.resetLine();
        TerminalProperties<?> props = terminal.getProperties();
        props.setPromptColor("red");
        terminal.moveToLineStart();

        terminal.println("LOGIN:");
        props.setPromptColor("#00ff00");

        boolean nickRight = false;
        User user = new User() ;

        while(!nickRight) {
            String nickname = textIO.newStringInputReader()
                    .withMinLength(4).withPattern("^(?![0-9]*$)[a-zA-Z0-9]+$")
                    .read("Username");
            user = user.getUser(nickname.toLowerCase());
            if(user == null)
                terminal.println("There is no user with this nickname, if you want to register press CTRL R.");
            else
            {
                nickRight = true;
            }
        }

        boolean passwordRight = false;

        while(!passwordRight) {
            String password = textIO.newStringInputReader()
                    .withMinLength(6)
                    .withInputMasking(true)
                    .read("Password");
            if (!user.getPassword().equals(password))
                terminal.println("Wrong password, retry.");
            else
                passwordRight = true;

        }
        return user;
    }

    private void quitGUI()
    {
        int confirmed = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to exit the program?", "Exit Program Message Box",
                JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            System.exit(0);
        }

    }

    }

