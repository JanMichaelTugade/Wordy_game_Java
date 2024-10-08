package Client_Java.clientgui.mainGUI;

import Client_Java.WordyApp.WordyGame;
import Client_Java.clientgui.loginGUI.ClientLoginGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class HomePageGUI extends JFrame {
    private ImageIcon bg;
    private JLabel wordyLabel, userLabel;
    private JButton startButton, joinButton, rulesButton, leaderboardButton, logoutButton;

    public HomePageGUI(WordyGame wordObj, String username, Point guiLocation) {
        super("Wordy Homepage");

        //Set up the GUI components
        wordyLabel = new JLabel("W o r d y    G a m e");
        wordyLabel.setFont(new Font("Arial", Font.BOLD, 40));
        wordyLabel.setForeground(Color.white);

        userLabel = new JLabel("Hello, " + username);
        userLabel.setFont(new Font("Arial", Font.BOLD, 25));
        userLabel.setForeground(Color.black);

        startButton = new JButton("Start");
        startButton.setFont(new Font("Arial", Font.BOLD, 20));

        joinButton = new JButton("Join");
        joinButton.setFont(new Font("Arial", Font.BOLD, 20));

        rulesButton = new JButton("Rules");
        rulesButton.setFont(new Font("Arial", Font.BOLD, 20));

        leaderboardButton = new JButton("Leaderboard");
        leaderboardButton.setFont(new Font("Arial", Font.BOLD, 20));

        logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 10));

        bg = new ImageIcon("CORBA/src/res/login.jpg");
        setContentPane(new JLabel(bg));

        //Set the layout of the GUI
        setLayout(null);

        //Set the bounds of the GUI components
        wordyLabel.setBounds(0, 10, 500, 50);
        wordyLabel.setHorizontalAlignment(JLabel.CENTER);

        userLabel.setBounds(0, 60, 500, 50);
        userLabel.setHorizontalAlignment(JLabel.CENTER);

        startButton.setBounds(190, 110, 120, 30);
        startButton.setForeground(Color.blue);
        startButton.setBackground(Color.yellow);

        startButton.addActionListener(e -> {
            String[] joined = wordObj.checkWaitingRoomStatus();
            if(joined[0].equals("Waiting room in session")) {
                JOptionPane.showMessageDialog(this,
                        "Waiting room is already in session! Please join the current" +
                                " waiting room or wait to make a new one", "Notification", JOptionPane.INFORMATION_MESSAGE);
            } else {
                wordObj.startNewWaitingRoom();
                wordObj.joinWaitingRoom(username);
                Point location = getLocation();
                dispose();
                new WaitingRoomGUI(wordObj, username, location);
            }
        });

        joinButton.setBounds(190, 150, 120, 30);
        joinButton.setForeground(Color.blue);
        joinButton.setBackground(Color.yellow);

        joinButton.addActionListener(e -> {
            String[] joined = wordObj.checkWaitingRoomStatus();
            if(joined[0].equals("No waiting room in session")) {
                JOptionPane.showMessageDialog(this,
                        "No waiting room is currently in session!",
                        "Notification", JOptionPane.INFORMATION_MESSAGE);
            } else if(joined[0].equals("Waiting room in session")) {
                if(wordObj.joinWaitingRoom(username)) {
                    Point location = getLocation();
                    dispose();
                    new WaitingRoomGUI(wordObj, username, location);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error joining waiting room!",
                            "Notification", JOptionPane.INFORMATION_MESSAGE);

                }
            }
        });

        rulesButton.setBounds(190, 190, 120, 30);
        rulesButton.setForeground(Color.blue);
        rulesButton.setBackground(Color.yellow);

        rulesButton.addActionListener(e -> {
            Point location = getLocation();
            dispose();
            new RulesGUI(wordObj, location);
        });

        leaderboardButton.setBounds(170, 230, 160, 30);
        leaderboardButton.setForeground(Color.blue);
        leaderboardButton.setBackground(Color.yellow);

        leaderboardButton.addActionListener(e -> {
            Point location = getLocation();
            dispose();
            new LeaderboardGUI(wordObj, location);
        });

        logoutButton.setBounds(370, 280, 90, 20);
        logoutButton.setForeground(Color.white);
        logoutButton.setBackground(Color.red);

        logoutButton.addActionListener(e -> {
            dispose();
            wordObj.logout(username);
            new ClientLoginGUI(wordObj, guiLocation);
        });

        //go offline when window is closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                wordObj.logout(username);
            }
        });

        //Add the components to the GUI
        add(wordyLabel);
        add(userLabel);
        add(startButton);
        add(joinButton);
        add(rulesButton);
        add(leaderboardButton);
        add(logoutButton);

        // Set the size and location of the GUI
        setSize(500, 350);
        setLocation(guiLocation);

        // Set the GUI to be visible
        setResizable(false);
        setVisible(true);

    }
}
