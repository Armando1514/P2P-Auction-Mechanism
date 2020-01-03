package p2p.auction.mechanism.GUI;
import org.beryx.textio.*;

import javax.swing.*;

import p2p.auction.mechanism.Control.UserMechanism;
import p2p.auction.mechanism.DAO.User;

import java.io.IOException;

public class AuthenticationGUI {



    private TextTerminal<?> terminal ;
    private TextIO textIO;
    private User userSaved;
    private String keyStrokeLogin = "ctrl L";
    private String keyStrokeRegister = "ctrl R";
    static final String keyStrokeQuit = "ctrl Q";

public   AuthenticationGUI(TextIO textIO, TextTerminal<?> terminal)
{
    this.textIO = textIO;
    this.terminal =  terminal;
}


    public User authenticationGUIDisplay() {




        final boolean[] authenticationDisableStrokes = {false};

        TerminalProperties<?> props = terminal.getProperties();

        boolean quitStroke = terminal.registerHandler(keyStrokeQuit, t -> {
            this.quitGUI();
            return new ReadHandlerData(ReadInterruptionStrategy.Action.ABORT).withRedrawRequired(true);
        });

        boolean registerStroke = terminal.registerHandler(keyStrokeRegister, t -> {
            if(!terminal.resetToBookmark("authentication")) {
                System.out.print("\033[H\033[2J");
                System.out.flush();
                this.printMenu();
            }
            if (!authenticationDisableStrokes[0]){
                userSaved = this.registerGUI();
            if (userSaved != null) {
                authenticationDisableStrokes[0] = true;
                UserMechanism.changeUserAddress(userSaved);
                return new ReadHandlerData(ReadInterruptionStrategy.Action.ABORT).withRedrawRequired(true);

            } else
                terminal.println("A strange error occurs, try after.");
        }
        else {
                terminal.println("You are not in the login session, command not allowed here.");
            }

            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);

        });
        boolean loginStroke = terminal.registerHandler(keyStrokeLogin, t -> {
            if(!terminal.resetToBookmark("authentication")) {
                System.out.print("\033[H\033[2J");
                System.out.flush();
                this.printMenu();
            }
            if (!authenticationDisableStrokes[0]){
                userSaved = this.loginGUI();
                if (userSaved != null) {
                    authenticationDisableStrokes[0] = true;
                    UserMechanism.changeUserAddress(userSaved);

                    return new ReadHandlerData(ReadInterruptionStrategy.Action.ABORT).withRedrawRequired(true);

                } else
                    terminal.println("A strange error occurs, try after.");
            }
            else {

                terminal.println("You are not in the login session, command not allowed here.");

            }
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
            this.printMenu();
            terminal.setBookmark("authentication");

        }

try {
    textIO.newStringInputReader().withPattern("(?i)(?<= |^)exit(?= |$)").read("\nWrite 'exit' to terminate...");
}
catch (ReadAbortedException e)
{
    return userSaved;
}
        this.quitGUI();

        textIO.dispose();
        return null;

    }

    private void printMenu()
    {
        terminal.println("--------------------------------------------------------------------------------");



        terminal.println("Press " + keyStrokeRegister + " to register");

        terminal.println("Press " + keyStrokeLogin + " to login");

        terminal.println("Press " + keyStrokeQuit + " to quit.");

        terminal.println("You can use these key combinations at any moment during your authentication entry session.");
        terminal.println("--------------------------------------------------------------------------------");

    }

    private User registerGUI()
    {
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
            nickRight = UserMechanism.storeUser(user);
            if(!nickRight)
                terminal.println("Username already exists, change it!");

        }
        String password = textIO.newStringInputReader()
                .withMinLength(6)
                .withInputMasking(true)
                .read("Password");
        terminal.println();
        user.setPassword(password);

        double money = textIO.newDoubleInputReader()
                .withMinVal(new Double(1))
                .read("Initial money");
        user.setMoney(new Double(money));
        if(UserMechanism.updateUser(user)) {
            terminal.println("User correctly created.");
            return user;
        }
        else
            return null;
    }

    private User loginGUI()
    {
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
            user = UserMechanism.findUser(nickname.toLowerCase());
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

            System.exit(0);

    }

    }

