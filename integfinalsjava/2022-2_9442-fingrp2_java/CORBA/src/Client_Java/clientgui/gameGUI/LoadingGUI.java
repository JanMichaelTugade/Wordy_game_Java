package Client_Java.clientgui.gameGUI;

import Client_Java.WordyApp.WordyGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;

public class LoadingGUI extends JFrame {
    private ImageIcon bg;
    private JLabel label1, label2;
    private Timer timer;
    private int countdown;
    private static int roundNumber;
    private static WordyGame wordObj;
    private static String username;
    private static String roomName;

    public LoadingGUI(WordyGame wordObj, String username, String roomName, Point guiLocation, int roundNumber) {
        LoadingGUI.wordObj = wordObj;
        LoadingGUI.username = username;
        LoadingGUI.roomName = roomName;
        LoadingGUI.roundNumber = roundNumber;

        // Set the size and location of the GUI
        setSize(800, 600);
        setLocation(guiLocation);

        // Set the background color of the JFrame
        bg = new ImageIcon("CORBA/src/res/login.jpg");
        setContentPane(new JLabel(bg));

        // Create a JLabel with the text "GAME STARTS IN"
        label1 = new JLabel("GAME STARTS IN");
        label1.setFont(new Font("Times New Roman", Font.BOLD, 40));

        // Create a second JLabel with the initial text "3"
        label2 = new JLabel("5");
        label2.setFont(new Font("Times New Roman", Font.BOLD, 35));

        // Set up the timer for countdown
        timer = new Timer();
        countdown = 5;
        timer.schedule(new CountdownTask(), 1000, 1000);

        label1.setBounds(215, 200, 350, 120);
        label2.setBounds(384, 247, 100, 120);

        //go offline when window is closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                wordObj.logout(username);
                wordObj.leaveWaitingRoom(username);
            }
        });

        add(label1);
        add(label2);

        // Set the GUI to be visible
        setResizable(false);
        setVisible(true);

    }

    // Inner class for the TimerTask
    class CountdownTask extends TimerTask {
        public void run() {
            countdown--;
            label2.setText(Integer.toString(countdown));
            if (countdown == 0) {
                timer.cancel();
                Point location = getLocation();
                dispose(); // Close the loading GUI
                // Open the main game GUI here
                //round number starts at 0 adding one each time the loadingGUI is called.
                //roundNumber is used to invoke the array index of the server's randomLetters arrayList
                String random = wordObj.getRandomLetters(roundNumber);
                roundNumber++;
                new GameGUI(wordObj, random, username, roomName, location, roundNumber);
            }
        }
    }
}