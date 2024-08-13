package Server_Java;

import Server_Java.ObjectClassesDB.Users;
import Server_Java.WordyApp.WordyGamePOA;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Timer;

import static Server_Java.WordyServerHelper.WordyServerMethods.generateRandomLetters;
import static Server_Java.WordyServerHelper.WordyServerMethods.generateRandomRoomname;

public class WordyImplementation extends WordyGamePOA {
    private static Connection con;
    private ArrayList<String> playerList = new ArrayList<>();
    public static ArrayList<String> randomLetters = new ArrayList<>();
    int timerCount = 10;
    String waitingRoomStatus = "No waiting room in session";

    private static String roomName = "temp";
    public WordyImplementation(){
        setUpConnection();
    }

    /**
     * The setUpConnection sets up the Database Connection
     */
    public static void setUpConnection() {
        String url = "jdbc:mysql://localhost:3306/wordy";
        String username = "root";
        String password = "";
        try {
            con = DriverManager.getConnection(url, username, password);
            System.out.println("Database connection successful.");
        } catch (SQLException e) {
            System.out.println("Database connection failed. " + e);
            System.exit(0);
        }
    }

    /**
     * The setStatus method updates the status of the user to OFFLINE when they choose to logout or close the application window.
     * It changes the user's status from ONLINE to OFFLINE in the database to reflect their current status.
     * @param username
     */
    @Override
    public void logout(String username) {
        String query = "UPDATE users SET status=? WHERE username=?";
        try (PreparedStatement statement = con.prepareStatement(query, ResultSet.CONCUR_UPDATABLE, ResultSet.TYPE_SCROLL_INSENSITIVE)) {
            statement.setString(1, "OFFLINE");
            statement.setString(2, username);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
        }
    }

    /** The login method checks if the user is already logged in, then verifies their credentials against the database.
     * If the login is successful, it updates the user's status to "ONLINE" and returns "Login successful!".
     * If the login fails, it returns either "Invalid username or password!" or "User is already logged in!"
     * depending on the reason for the failure.
     *
     * @param username
     * @param password
     * @param result
     * @return
     */
    @Override
    public String login(String username, String password, String result) {

        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement statement = con.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int userID = rs.getInt("userID");
                String userName = rs.getString("userName");
                String passwordFromDB = rs.getString("password");
                String status = rs.getString("status");

                if (status.equals("ONLINE")) {
                    return "User is already logged in!";
                } else if (username.equals(userName) && passwordFromDB.equals(password)) {
                    Users user = new Users(userID, userName, password, "ONLINE");
                    updateUserStatus(user);
                    return "Login successful!";
                }
            }
        } catch (SQLException e) {
            return "Invalid username or password!";
        }
        return "Invalid username or password!";
    }

    /** The register method registers a new user with the provided username and password.
     * It checks if the input is empty, and returns false if either is null.
     * If the username already exists, it returns false. Otherwise, it adds the new user to the database and returns true.
     * If an error occurs, the method returns false and logs the error message.
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public boolean register(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            return false;
        }
        String query = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement statement = con.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return false;
            } else {
                // If username is unique, it adds it to the database
                query = "INSERT INTO users (username, password) VALUES (?, ?)";
                try (PreparedStatement insertStatement = con.prepareStatement(query)) {
                    insertStatement.setString(1, username);
                    insertStatement.setString(2, password);
                    insertStatement.executeUpdate();
                    System.out.println(username + " has successfully registered.");
                    return true;
                } catch (SQLException e) {
                    System.err.println("SQLException: " + e.getMessage());
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
            return false;
        }
    }

    /**
     * The getUserIDFromUsername method retrieves the user ID associated with a given username by querying a database table called "users".
     * It prepares an SQL statement with a question mark placeholder for the username parameter, sets the parameter value, and executes the statement.
     * If the result set contains a row, it returns the integer value of the "userID" column of that row.
     * If any SQLException occurs, it is printed to the console. If no rows are found, it returns 0.
     *
     * @param username
     * @return
     */
    @Override
    public int getUserIDFromUsername(String username) {
        String query = "SELECT userID FROM users WHERE userName = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, username);
            // execute the SQL statement and retrieve the results
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("userID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * The getPlayerList method returns an array of strings that contains the usernames of the players in the current waiting room.
     * The method creates a temporary array with the size of the playerList and copies the elements of the playerList
     * into the temporary array using the toArray() method. It then returns the temporary array.
     * @return
     */
    @Override
    public String[] getPlayerList() {
        String[] temp = new String[playerList.size()];
        return playerList.toArray(temp);
    }

    /**
     * The getTopFivePlayers method retrieves the top five players from the leaderboard table by performing an SQL query that joins the users and leaderboard
     * tables and sorts the results by the number of matches won. It then stores the top five players' usernames in an array and returns the array.
     * If there are fewer than five players in the leaderboard, the method returns an empty array.
     *
     * @return
     */
    @Override
    public String[] getTopFivePlayers() {
        String[] topFive;
        String query = "SELECT users.username, leaderboard.matchesWon " +
                "FROM users " +
                "JOIN leaderboard ON users.userID = leaderboard.userID " +
                "ORDER BY leaderboard.matchesWon DESC " +
                "LIMIT 5;";

        try (Statement statement = con.createStatement()) {
            ResultSet rs = statement.executeQuery(query);
            List<String> playerList = new ArrayList<>();

            while (rs.next()) {
                String username = rs.getString("username");
                int matchesWon = rs.getInt("matchesWon");
                playerList.add(username + " - " + matchesWon);
            }

            topFive = playerList.toArray(new String[0]);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
            topFive = new String[0]; // Return an empty array in case of exception
        }

        return topFive;
    }



    /**
     * The getWordsData method readsthe words.txt file and store it into a string array and returns it
     * @return
     */
    @Override
    public String[] getWordsData() {
        ArrayList<String> data = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("CORBA/src/res/words.txt"));

            String line = br.readLine();
            while (line != null) {
                data.add(line);
                line = br.readLine();
            }
            br.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return data.toArray(new String[0]);
    }


    /**
     * The getRandomLetters method returns the random letters generated from the server.
     * @return
     */
    @Override
    public String getRandomLetters(int roundNumber) {
        randomLetters.add(generateRandomLetters());
        return randomLetters.get(roundNumber);
    }

    /**
     *The validateInput method validates whether a given input string contains only the letters that are present in
     * the given random letters, taking into account the frequency of each letter in the random letters. This also
     * validates if the input string's length is greater than 4.
     * @param randomLetters
     * @param inputString
     * @return
     */
    @Override
    public boolean validateInput(String randomLetters, String inputString, String[] data) {
        String vowels = "AEIOU";
        String consonants = "BCDFGHJKLMNPQRSTVWXYZ";
        Map<Character, Integer> letterCounts = new HashMap<>();

        ArrayList<String> dataList = new ArrayList<>(Arrays.asList(data));
        inputString = inputString.replaceAll("\\s", "").toUpperCase();
        String lowercaseString = inputString.replaceAll("\\s", "").toLowerCase();
        randomLetters = randomLetters.replaceAll("\\s", "").toUpperCase();

        // Count the frequency of each letter in randomLetters
        for (int i = 0; i < randomLetters.length(); i++) {
            char c = randomLetters.charAt(i);
            if (!letterCounts.containsKey(c)) {
                letterCounts.put(c, 1);
            } else {
                letterCounts.put(c, letterCounts.get(c) + 1);
            }
        }

        // Check if inputString is valid
        if (lowercaseString.length() < 5 || !dataList.contains(lowercaseString)) {
            return false;
        }

        // Check if each letter in inputString is present in randomLetters
        for (int i = 0; i < inputString.length(); i++) {
            char c = inputString.charAt(i);
            if (vowels.indexOf(c) >= 0 && !letterCounts.containsKey(c)) {
                // The letter is a vowel and is not present in the random letters
                return false;
            } else if (consonants.indexOf(c) >= 0 && !letterCounts.containsKey(c)) {
                // The letter is a consonant and is not present in the random letters
                return false;
            } else {
                // The letter is present in the random letters, decrement its count
                int count = letterCounts.get(c);
                if (count == 0) {
                    // The letter is not available anymore in the random letters
                    return false;
                } else {
                    letterCounts.put(c, count - 1);
                }
            }
        }
        // All letters are present in the random letters given, the length is greater than 4 and answer is on the words.txt
        return true;
    }


    /**
     * The updateLongestWordFormed method updates the longest word in the game_session table of the database for a
     * specific user in a specific room with a new value provided.
     * @param userID
     * @param wordFormed
     * @param roomName
     */
    @Override
    public void updateLongestWordFormed(int userID, String wordFormed, String roomName) {
        String query = "UPDATE game_session SET longestWord = ? WHERE roomName = ? AND userID = ?";
        try (PreparedStatement statement = con.prepareStatement(query)) {
            statement.setString(1, wordFormed);
            statement.setString(2, roomName);
            statement.setInt(3, userID);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
        }
    }

    /**
     *The insertIntoLeaderboardTable method inserts a new row into the leaderboard table in the database, containing
     * the user's ID, their longest word formed in a game session, and the number of matches they have won.
     * @param userID
     * @param longestWord
     * @param matchesWon
     */
    @Override
    public void insertIntoLeaderboardTable(int userID, String longestWord, int matchesWon) {
        String query1 = "INSERT INTO `leaderboard`(`userID`, `longestWord`, `matchesWon`) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(query1)) {
            stmt.setInt(1, userID);
            stmt.setString(2, longestWord);
            stmt.setInt(3, matchesWon);
            stmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * The setLongestWordToNull method updates the longestWord column in the game_session table in the database
     * to null for the given user ID and room name every new round.
     * @param roomName
     */
    @Override
    public void setLongestWordToNull(String roomName) {
        String query1 = "UPDATE game_session SET longestWord = 'NULL' WHERE roomName = ?";
        try (PreparedStatement statement = con.prepareStatement(query1)) {
            statement.setString(1, roomName);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
        }
    }

    /**
     * The getLongestWordLength method takes an userID as input, and retrieves the longest word formed by the user in a
     * game session from the database. If the longest word is not null, the length of the word is returned. Otherwise,
     * the method returns 0. This  uses a SQL SELECT statement with a prepared statement to retrieve the longestWord
     * field from the game_session table where the userID matches the input userID. The retrieved result set is checked
     * to see if there is a longest word value present, and if so, its length is returned. If there is no longest word
     * present, the method returns 0.
     * @param userID
     * @return
     */
    @Override
    public int getLongestWordLength(int userID) {
        String query = "SELECT longestWord FROM game_session WHERE userID = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            String longestWord;
            stmt.setInt(1, userID);
            // execute the SQL statement and retrieve the results
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                longestWord = rs.getString("longestWord");
                if (longestWord != null) {
                    return longestWord.length();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * The compareWordFormedToLongestWord method retrieves the longest word from the leaderboard for a given user ID,
     * and if the length of the word formed in a game session is greater than or equal to the length of the longest
     * word, it updates the longest word in the leaderboard for that user.
     * @param userID
     * @param wordFormed
     */
    @Override
    public void compareWordFormedToLongestWord(int userID, String wordFormed) {
        String query1 = "SELECT longestWord FROM leaderboard WHERE userID = ?";
        try (PreparedStatement stmt1 = con.prepareStatement(query1)) {
            String longestWord;
            stmt1.setInt(1, userID);
            // execute the SQL statement and retrieve the results
            ResultSet rs1 = stmt1.executeQuery();

            if (rs1.next()) {
                longestWord = rs1.getString("longestWord");
                if (longestWord.length() <= wordFormed.length()) {
                    String query2 = "UPDATE leaderboard SET longestWord = ? WHERE userID = ?";
                    try (PreparedStatement stmt2 = con.prepareStatement(query2)) {
                        stmt2.setString(1, wordFormed);
                        stmt2.setInt(2, userID);
                        stmt2.executeUpdate();
                    }

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * The checkPointsAchieved method retrieves the points achieved by all players in a specified room from the
     * database and checks if any player has achieved 3 points. If so, it returns true, indicating that the game
     * in the room should end. Otherwise, it returns false, indicating that the game should continue.
     * @param roomName
     * @return
     */
    @Override
    public boolean checkPointsAchieved(String roomName) {
        ArrayList<String> userIDList = new ArrayList<>();
        String query = "SELECT pointsAchieved, userID FROM game_session WHERE roomName = ?";
        try (PreparedStatement statement = con.prepareStatement(query)) {
            statement.setString(1, roomName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String points = resultSet.getString("pointsAchieved");
                String userID = resultSet.getString("userID");
                if (points != null && points.contains("3")) {
                    return true;
                }
                userIDList.add(userID);
            }
            if (userIDList.size() < 2) {
                return true;
            }
        } catch (SQLException e) {
            // Handle any exceptions
            e.printStackTrace();
        }
        return false;
    }

    /**
     * The checkExistingUserID method checks if a given userID already exists in the leaderboard table of the database
     * @param userID
     * @return
     */
    @Override
    public boolean checkExistingUserID(int userID) {
        String query = "SELECT userID FROM leaderboard WHERE userID = ?";
        try (PreparedStatement statement = con.prepareStatement(query)) {
            statement.setInt(1, userID);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int user = resultSet.getInt("userID");
                if (user == userID) {
                    return true;
                }
            }
        } catch (SQLException e) {
            // Handle any exceptions
            e.printStackTrace();
        }
        return false;
    }

    /**
     * The updateMatchesWon method retrieves the number of matches won by a user with a given userID from the leaderboard
     * table, increments the value by 1, and updates the matchesWon column of that user in the leaderboard table
     * @param userID
     */
    @Override
    public void updateMatchesWon(int userID) {
        String query1 = "SELECT matchesWon FROM leaderboard WHERE userID = ?";
        try (PreparedStatement statement1 = con.prepareStatement(query1)) {
            statement1.setInt(1, userID);
            ResultSet resultSet = statement1.executeQuery();
            if (resultSet.next()) {
                int matchesWon = resultSet.getInt("matchesWon");
                matchesWon = matchesWon + 1;
                String query2 = "UPDATE leaderboard SET matchesWon = ? WHERE userID = ?";
                PreparedStatement statement2 = con.prepareStatement(query2);
                statement2.setInt(1, matchesWon);
                statement2.setInt(2, userID);
                statement2.executeUpdate();
            }
        } catch (SQLException e) {
            // Handle any exceptions
            e.printStackTrace();
        }

    }

    /**
     * The modifyPointsAchieved method takes a user ID as input and modifies the number of points achieved by the user
     * in a game session. It first retrieves the user's longest word from the game_session table and their current
     * points achieved. If the user has a longest word, it compares it to the longest words of other users in the
     * same game session with shorter lengths and increments the user's points if their longest word is longer.
     * Finally, it updates the user's points achieved in the game_session table and returns the new total points.
     * @param userID
     * @return
     */
    @Override
    public int modifyPointsAchieved(int userID, String roomName) {
        int points = 0;
        int longestWordLength = 0;
        ArrayList<String> longestWordsList = new ArrayList<>();
        String query1 = "SELECT longestWord FROM game_session WHERE userID = ? AND roomName = ?";
        String query2 = "SELECT pointsAchieved FROM game_session WHERE userID = ? AND roomName = ?";
        String query3 = "UPDATE game_session SET pointsAchieved = ? WHERE userID = ? AND roomName = ?";
        String query4 = "SELECT userID, longestWord FROM game_session WHERE userID != ? AND roomName = ?";
        try (PreparedStatement stmt1 = con.prepareStatement(query1);
             PreparedStatement stmt2 = con.prepareStatement(query2);
             PreparedStatement stmt3 = con.prepareStatement(query3);
             PreparedStatement stmt4 = con.prepareStatement(query4)) {

            // Get the user's longest word
            stmt1.setInt(1, userID);
            stmt1.setString(2,roomName);
            ResultSet rs1 = stmt1.executeQuery();
            String userLW = null;
            if (rs1.next()) {
                userLW = rs1.getString("longestWord");
            }

            // Get the user's current points
            stmt2.setInt(1, userID);
            stmt2.setString(2,roomName);
            ResultSet rs2 = stmt2.executeQuery();
            if (rs2.next()) {
                points = rs2.getInt("pointsAchieved");
                if (userLW != null) {

                    // Compare the user's longest word to other users' longest words
                    stmt4.setInt(1, userID);
                    stmt4.setString(2, roomName);
                    ResultSet rs4 = stmt4.executeQuery();
                    while (rs4.next()) {
                        String longestWords = rs4.getString("longestWord");
                        longestWordsList.add(longestWords);
                    }
                    for (String word : longestWordsList) {
                        if (word != null) {
                        if (word.length() > longestWordLength) {
                            longestWordLength = word.length();
                        }
                        }
                    }

                    if (longestWordLength == userLW.length()) {
                        return points;
                    } else if (longestWordLength > userLW.length()) {
                        return points;
                    } else {
                        points += 1;
                    }

                    // Update the user's points
                    stmt3.setInt(1, points);
                    stmt3.setInt(2, userID);
                    stmt3.setString(3,roomName);
                    stmt3.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return points;
    }

    /**
     * This method creates a new waiting room for a game by setting the waiting room status to "Waiting room in session,"
     * generating random letters, creating a random room name, and starting a timer that repeats every second.
     * If there are at least two players in the player list when the timer counts down to zero,
     * it sets the waiting room status to "Waiting room ready" and passes the player list to the creation of a new room object.
     * If not, it sets the waiting room status to "Waiting room invalid." It then clears the player list, sleeps for 3 seconds,
     * sets the waiting room status to "No waiting room in session," and cancels the timer. Finally, it prints out messages
     * to the console to indicate the status of the waiting room.
     */
    @Override
    public void startNewWaitingRoom() {
        System.out.println("Waiting room started.");

        waitingRoomStatus = "Waiting room in session";
        randomLetters = new ArrayList<>();
        randomLetters.add(generateRandomLetters());
        roomName = generateRandomRoomname();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerCount--;
                if (timerCount <= 0) {
                    if (playerList.size() >= 2) {
                        System.out.println("Game starting with " + playerList.size() + " players.");
                        waitingRoomStatus = "Waiting room ready";
                        try {
                            Thread.sleep(3000); // delay for 2 seconds
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // code to pass onto the creation of a room object, pass the playerList array too

                        playerList.clear();
                        waitingRoomStatus = "No waiting room in session";
                        timer.cancel(); // stop the timer after execution
                    } else {
                        System.out.println("Not enough players to start the game.");
                        waitingRoomStatus = "Waiting room invalid";
                        playerList.clear();
                        try {
                            Thread.sleep(3000); // delay for 2 seconds
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        waitingRoomStatus = "No waiting room in session";
                        timer.cancel(); // stop the timer after execution
                    }
                    timerCount = 10; // reset timerCount to 10
                }
            }
        }, 0, 1000); // delay 0ms, repeat every 1000ms (1s)
    }

    /**
     * The joinWaitingRoom method adds the given username to the player list of the waiting room if the waiting room status is "Waiting room in session."
     * It also tries to insert the user's data into a database table called "game_session" by using their user ID and the room name.
     * If this is successful, it returns true. If not, it returns false. If the waiting room status is "No waiting room in session," it also returns false.
     * @param username
     * @return
     */
    @Override
    public boolean joinWaitingRoom(String username) {
        if(waitingRoomStatus.equals("Waiting room in session")) {
            playerList.add(username);
            try {
                int userId = getUserIDFromUsername(username);
                String sql = "INSERT INTO game_session (roomName, userID) VALUES (?, ?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, roomName);
                ps.setInt(2, userId);
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        } else if(waitingRoomStatus.equals("No waiting room in session")) {
            return false;
        }
        return false;
    }

    /**
     * The leaveWaitingRoom method called leaveWaitingRoom() removes the given username from the player list of the waiting room.
     * It also tries to delete the user's data from a database table called "game_session" by using their user ID.
     * @param username
     */
    @Override
    public void leaveWaitingRoom(String username) {
        playerList.remove(username);
        try {
            int userId = getUserIDFromUsername(username);
            String sql = "DELETE FROM game_session WHERE userID=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * The checkWaitingRoomStatus method returns an array of strings containing the current waiting room status, the remaining time on the timer, and the name of the room.
     * @return
     */
    @Override
    public String[] checkWaitingRoomStatus() {
        return new String[]{waitingRoomStatus, String.valueOf(timerCount), roomName};
    }

    /**
     *  The displayScores method gets and display the scores of the players in the specific room from the database.
     *
     * @param userID
     * @return
     */
    @Override
    public int displayScores(int userID) {
        int score = 0;
        String query = "SELECT users.username, game_session.pointsAchieved FROM users NATURAL JOIN game_session WHERE userID = ? ORDER BY game_session.pointsAchieved DESC";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                score += rs.getInt("pointsAchieved");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return score;
    }

    /**
     * The getPlayersInGameSession method retrieves the usernames of all players who are currently participating in a specified game session
     * (identified by its room name) from the database, and returns them as a string array.
     *
     * @param roomName
     * @return
     */
    @Override
    public String[] getPlayersInGameSession(String roomName) {
        List<String> players = new ArrayList<>();
        String query = "SELECT DISTINCT username FROM users JOIN game_session ON users.userID = game_session.userID WHERE roomName = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, roomName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                players.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return players.toArray(new String[0]);
    }

    /**
     * The getFiveLongestWords method retrieves an array of five longest words and the name of the respective player from a database table named "leaderboard".
     * It fetches the longest words by executing an SQL query and creates an empty array of size five to store the words.
     * Then, it loops through the result set and adds the longest words to the array before returning it.
     * If an error occurs during the query, the method logs an error message.
     * @return
     */
    @Override
    public String[] getFiveLongestWords() {
        String[] longestWords;
        String query = "SELECT leaderboard.longestWord, users.userName " +
                "FROM leaderboard " +
                "INNER JOIN users ON leaderboard.userID = users.userID " +
                "ORDER BY CHAR_LENGTH(longestWord) DESC LIMIT 5";

        try (Statement statement = con.createStatement()) {
            ResultSet rs = statement.executeQuery(query);
            List<String> wordList = new ArrayList<>();

            while (rs.next()) {
                String longestWord = rs.getString("longestWord");
                String userName = rs.getString("userName");
                wordList.add(longestWord + " (" + userName + ")");
            }

            longestWords = wordList.toArray(new String[wordList.size()]);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
            longestWords = new String[0]; // Return an empty array in case of exception
        }

        return longestWords;
    }

    /**
     * The updateUserStatus method updates the status of a user in the database using a "Users" object as a parameter.
     * It updates the user's status to the new status provided in the object using an SQL update statement.
     * It logs an error message if there is a problem during the update.
     *
     * @param user
     */
    private void updateUserStatus(Users user) {
        String query = "UPDATE users SET status = ? WHERE userID = ?";
        try (PreparedStatement statement = con.prepareStatement(query)) {
            statement.setString(1, user.getStatus());
            statement.setInt(2, user.getUserID());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Cannot update status of user.");
        }
    }
}

