package p2p.auction.mechanism.GUI;
import org.beryx.textio.*;

import javax.swing.*;

import p2p.auction.mechanism.Control.UserMechanism;
import p2p.auction.mechanism.DAO.User;

public class AuthenticationGUI {



    private TextTerminal<?> terminal ;
    private TextIO textIO;
    private User userSaved;
    private String keyStrokeLogin = "ctrl L";
    private String keyStrokeRegister = "ctrl R";

public   AuthenticationGUI(TextIO textIO, TextTerminal<?> terminal)
{
    this.textIO = textIO;
    this.terminal =  terminal;
}


    public User authenticationGUIDisplay() {



        final boolean[] authenticationDisableStrokes = {false};

        TerminalProperties<?> props = terminal.getProperties();


        boolean registerStroke = terminal.registerHandler(keyStrokeRegister, t -> {
            if (!authenticationDisableStrokes[0]){
                userSaved = this.registerGUI();
            if (userSaved != null) {
                authenticationDisableStrokes[0] = true;
                return new ReadHandlerData(ReadInterruptionStrategy.Action.ABORT).withRedrawRequired(true);

            } else
                terminal.println("A strange error occurs, try after.");
        }
        else {
                terminal.resetToBookmark("auction");
                terminal.println("You are not in the login session, command not allowed here.");
            }

            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);

        });
        boolean loginStroke = terminal.registerHandler(keyStrokeLogin, t -> {

            if (!authenticationDisableStrokes[0]){
                userSaved = this.loginGUI();
                if (userSaved != null) {
                    authenticationDisableStrokes[0] = true;
                    return new ReadHandlerData(ReadInterruptionStrategy.Action.ABORT).withRedrawRequired(true);

                } else
                    terminal.println("A strange error occurs, try after.");
            }
            else {
                terminal.resetToBookmark("auction");
                terminal.println("You are not in the login session, command not allowed here.");

            }
            return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
        });


        boolean hasHandlers = loginStroke || registerStroke  ;
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
        int confirmed = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to exit the program?", "Exit message",
                JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
        else{
            terminal.resetToBookmark("reset");
            this.authenticationGUIDisplay();

        }

    }

    }

