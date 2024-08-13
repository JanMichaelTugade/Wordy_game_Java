package Client_Java.clientgui.gameGUI;

import Client_Java.WordyApp.WordyGame;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GameGUI extends JFrame {
    private ImageIcon bg;
    private JLabel round, user, time, score, word, sec, validation = new JLabel();
    private JButton shuffleButton;
    private Timer timer;
    private int remainingTime = 11;
    private JTextField ans;
    private static WordyGame wordObj;
    public static int roundNumber;
    private static String username, roomName, firstLetterLine, secondLetterLine;
    private static int userID;

    public GameGUI(WordyGame obj, String randomLetters, String username, String roomName, Point guiLocation, int roundNumber) {
        wordObj = obj;
        GameGUI.username = username;
        GameGUI.roomName = roomName;
        GameGUI.roundNumber = roundNumber;
        userID = wordObj.getUserIDFromUsername(username);

        int points = wordObj.modifyPointsAchieved(userID, roomName);
        // Set the size and location of the GUI
        setSize(800, 650);
        setLocation(guiLocation);

        // Set the background color of the JFrame
        bg = new ImageIcon("CORBA/src/res/login.jpg");
        setContentPane(new JLabel(bg));

        round = new JLabel("Round " + roundNumber);
        round.setFont(new Font("Times New Roman", Font.BOLD, 40));

        user = new JLabel(username);
        user.setFont(new Font("Times New Roman", Font.BOLD, 23));

        time = new JLabel("Timer");
        time.setFont(new Font("Times New Roman", Font.BOLD, 30));

        sec = new JLabel("10"); //edit the 10
        sec.setFont(new Font("Times New Roman", Font.BOLD, 30));

        timer = new Timer();
        timer.scheduleAtFixedRate(new CountdownTask(), 0, 1000);

        score = new JLabel("Score: " + points);
        score.setFont(new Font("Times New Roman", Font.BOLD, 23));

        shuffleButton = new JButton("Shuffle");
        shuffleButton.setFont(new Font("Times New Roman", Font.BOLD, 17));
        shuffleButton.setBackground(Color.YELLOW);
        shuffleButton.setForeground(Color.BLUE);

        word = new JLabel("<html><div style='text-align:center;'>" + firstLetterLine +
                "<br><span style='display:inline-block;width:130px;text-align:center;'>" +
                secondLetterLine + "</span></div></html>");
        word.setFont(new Font("Times New Roman", Font.BOLD, 50));
        word.setVerticalAlignment(JLabel.TOP);
        shuffleLetters(randomLetters);

        class UpperCaseDocument extends PlainDocument {  // CAPSLOCK
            @Override
            public void insertString(int offs, String str, AttributeSet a)
                    throws BadLocationException {
                if (str == null) {
                    return;
                }
                super.insertString(offs, str.toUpperCase(), a); // Convert text to upper case
            }
        }

        ans = new JTextField();
        ans.setFont(new Font("Arial", Font.PLAIN, 20));
        ans.setDocument(new UpperCaseDocument()); // Set the custom document
        ans.setHorizontalAlignment(JTextField.CENTER);
        ans.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] data = obj.getWordsData();
                String answer = ans.getText();

                if (obj.validateInput(randomLetters, answer, data)) {
                    getContentPane().remove(validation);
                    validation = new JLabel("ANSWER IS VALID");
                    validation.setFont(new Font("Arial", Font.BOLD, 15));
                    validation.setForeground(Color.GREEN);
                    validation.setBounds(340, 500, 550, 45);
                    add(validation);
                    if (answer.length() >= obj.getLongestWordLength(userID)) {
                        obj.updateLongestWordFormed(userID, answer, roomName);
                        if (obj.checkExistingUserID(userID)) {
                            obj.compareWordFormedToLongestWord(userID, answer);
                        } else {
                            obj.insertIntoLeaderboardTable(userID, answer, 0);
                        }
                    }

                } else {
                    getContentPane().remove(validation);
                    validation = new JLabel("INVALID WORD FORMED");
                    validation.setFont(new Font("Arial", Font.BOLD, 15));
                    validation.setForeground(Color.RED);
                    validation.setBounds(300, 500, 550, 45);
                    add(validation);
                }


                ans.setText("");
                // Refresh the JFrame to show the JLabel
                getContentPane().validate();
                getContentPane().repaint();
            }
        }) ;

        shuffleButton.addActionListener(e -> shuffleLetters(randomLetters)) ;

        round.setBounds(320, -25, 350, 120);
        user.setBounds(10, -40, 200, 120);
        score.setBounds(685, -40, 200, 120);

        time.setBounds(357, 70, 200, 120);
        sec.setBounds(387, 105, 200, 120);


        word.setBounds(160, 230, 800, 120);

        ans.setBounds(120, 430, 550, 45);
        shuffleButton.setBounds(570, 395, 100, 25);


        add(round);
        add(user);
        add(time);
        add(score);
        add(sec);
        add(word);
        add(ans);
        add(shuffleButton);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                wordObj.logout(username);
                wordObj.leaveWaitingRoom(username);
                System.exit(0);
            }
        });

        // Set the GUI to be visible
        setResizable(true);
        setVisible(true);

    }

    private class CountdownTask extends TimerTask {
        @Override
        public void run() {
            remainingTime--;
            sec.setText(Integer.toString(remainingTime));
            if (remainingTime < 0) {
                timer.cancel();
                dispose();
                wordObj.modifyPointsAchieved(userID, roomName);

                try {
                    Thread.sleep(3000); // Wait for 2 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                boolean flag = wordObj.checkPointsAchieved(roomName);
                Point location = getLocation();
                wordObj.setLongestWordToNull(roomName);
                if (!flag) {
                    new RanksGUI(wordObj, roundNumber, username, roomName, location);
                } else if (wordObj.modifyPointsAchieved(userID, roomName) == 3 || wordObj.checkPointsAchieved(roomName)) {
                    wordObj.updateMatchesWon(userID);
                    new ResultGUI(wordObj, userID, roomName, username, location);
                } else if(wordObj.modifyPointsAchieved(userID, roomName) != 3 || wordObj.checkPointsAchieved(roomName)){
                    new ResultGUI(wordObj, userID, roomName, username, location);
                }
                // Close the loading GUI
                // Open the main game GUI here
            }
        }
    }
    private void shuffleLetters(String randomLetters) {

        char[] lettersArray = randomLetters.toCharArray();

        //THE FISHER-YALES SHUFFLE ALGORITHM
        Random rand = new Random();
        for (int i = lettersArray.length - 1; i > 0; i--) {
            int index = rand.nextInt(i + 1);
            char temp = lettersArray[index];
            lettersArray[index] = lettersArray[i];
            lettersArray[i] = temp;
        }

        firstLetterLine = new String(lettersArray, 0, 10);
        secondLetterLine = new String(lettersArray, 10, 7);

        firstLetterLine = firstLetterLine.replaceAll("", " ").trim();
        secondLetterLine = secondLetterLine.replaceAll("", " ").trim();

        word.setText("<html><div style='text-align:center;'>" + firstLetterLine +
                "<br><span style='display:inline-block;width:130px;text-align:center;'>" +
                secondLetterLine + "</span></div></html>");
    }
}

