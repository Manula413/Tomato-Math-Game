/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package GUI;

import GameEngine.GameEngineCasual;
import DataBase.DatabaseConnector;
import java.awt.BorderLayout;
import java.awt.Image;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;

/**
 *
 * @author manula
 */
public class Casual extends javax.swing.JFrame implements ActionListener {

    private GameEngineCasual gameEngine;
    private BufferedImage currentGame;
    private int streakCounter = 0;
    private Timer gameTimer;
    private int timeInSeconds;

    private Timer timerGame;
    private int gameTime;

    /**
     * Creates new form Casual
     */
    private String userName;
    private int score;

    public Casual() {
        initComponents();
        loadLeaderboardData();
        initializeGame();
        initializeGameTimer();

        setupTableAndBackgroundComponents();

    }

    public Casual(String userName) {
        initComponents();
        initializeGame();
        initializeGameTimer();
        loadLeaderboardData();
        this.userName = userName;
        String displayName = getDisplayNameFromDatabase(userName);

        txtDisplayName.setText(displayName);

        setupTableAndBackgroundComponents();

    }

    private void setupTableAndBackgroundComponents() {
        // Setup jTable1 appearance
        jTable1.setOpaque(false);
        jTable1.setShowGrid(false); // Hide grid lines
        jTable1.setBorder(BorderFactory.createEmptyBorder()); // Remove table border
        ((DefaultTableCellRenderer) jTable1.getDefaultRenderer(Object.class)).setOpaque(false);

        // Setup jScrollPane1 appearance
        jScrollPane1.setOpaque(false);
        jScrollPane1.getViewport().setOpaque(false);

        // Setup jPanel2 background
        jPanel2.setBackground(new Color(0, 0, 0, 60));

        // Set initial visibility for background components
        txtAnswerBackgroundSad.setVisible(false);
        txtAnswerBackgroundHappy.setVisible(false);
    }

    private String getDisplayNameFromDatabase(String userName) {
        String displayName = null;

        Connection con = DatabaseConnector.mycon();

        try {
            String selectQuery = "SELECT display_name FROM users WHERE user_name = ?";
            try (PreparedStatement preparedStatement = con.prepareStatement(selectQuery)) {
                preparedStatement.setString(1, userName);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    if (resultSet.next()) {
                        displayName = resultSet.getString("display_name");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return displayName;
    }

    private void loadLeaderboardData() {
        try {
            Connection con = DatabaseConnector.mycon();

            String query = "SELECT users.display_name, casual.score, casual.streak, casual.time FROM casual "
                    + "INNER JOIN users ON casual.user_id = users.user_id "
                    + "ORDER BY score DESC LIMIT 5";

            try (PreparedStatement preparedStatement = con.prepareStatement(query); ResultSet resultSet = preparedStatement.executeQuery()) {

                DefaultTableModel model = new DefaultTableModel(new String[]{"Name", "Score", "Streak", "Time"}, 0);

                while (resultSet.next()) {
                    String name = resultSet.getString("display_name");
                    int score = resultSet.getInt("score");
                    int streak = resultSet.getInt("streak");
                    String time = resultSet.getString("time");

                    model.addRow(new Object[]{name, score, streak, time});
                }

                jTable1.setModel(model);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeGame() {
        // GameEngine initialization
        gameEngine = new GameEngineCasual("Player");

        // Get the next game
        currentGame = gameEngine.nextGame();

        // Set up the jPanel1 layout
        jPanel1.setLayout(new BorderLayout());

        // Create and set up the image label
        JLabel imageLabel = new JLabel();
        jPanel1.add(imageLabel);

        // Set up the initial image icon
        ImageIcon initialImageIcon = new ImageIcon(currentGame);
        Image originalImage = initialImageIcon.getImage();

        // Scale the image to fit the jPanel1 dimensions
        int panelWidth = jPanel1.getWidth();
        int panelHeight = jPanel1.getHeight();
        Image scaledImage = originalImage.getScaledInstance(panelWidth, panelHeight, Image.SCALE_SMOOTH);
        ImageIcon scaledImageIcon = new ImageIcon(scaledImage);
        imageLabel.setIcon(scaledImageIcon);

        // Set up answer buttons
        JButton[] answerButtons = {
            jButton2, jButton4, jButton5, jButton6, jButton7,
            jButton8, jButton9, jButton10, jButton11, jButton12
        };

        for (int i = 0; i < answerButtons.length; i++) {
            answerButtons[i].setText(String.valueOf(i));
            answerButtons[i].addActionListener(this);
        }

        // Hide streak components
        txtStreak.setVisible(false);
        txtStreaklbl.setVisible(false);
        jTextFieldStreak.setVisible(false);
        txtStreakIcon.setVisible(false);
    }

    private void initializeGameTimer() {
        gameTime = 0;
        System.out.println("Starting Time: " + displayFormattedTime(gameTime));
        timerGame = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleTimerUpdate();
            }
        });
        timerGame.start();
    }

    private void handleTimerUpdate() {
        timeInSeconds++;

        String formattedTime = displayFormattedTime(timeInSeconds);
        txtTimer.setText(formattedTime);
    }

    private String displayFormattedTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    private int getGameTime() {
        return gameTime;
    }

    private void updateTimerDisplay(String formattedTime) {
        txtTimer.setText(formattedTime);
    }

    private void updateScoreLabel() {
        int score = gameEngine.getScore();
        txtScore.setText(String.valueOf(score));

    }

    private void updateScoreInDatabase( int score, int streakCounter, int timeInSeconds) {
        Connection con = DatabaseConnector.mycon();

        try {
            // Check if the user already has a row in the table
            String checkUserQuery = "SELECT COUNT(*) AS userCount FROM casual "
                    + "WHERE user_id = (SELECT user_id FROM users WHERE user_name = ?)";

            try (PreparedStatement checkUserStatement = con.prepareStatement(checkUserQuery)) {
                checkUserStatement.setString(1, userName);

                try (ResultSet userResultSet = checkUserStatement.executeQuery()) {
                    if (userResultSet.next() && userResultSet.getInt("userCount") == 0) {
                        // User doesn't have a row in the table, insert a new row
                        String insertQuery = "INSERT INTO casual (user_id, score, streak, time) "
                                + "VALUES ((SELECT user_id FROM users WHERE user_name = ?), ?, ?, ?)";

                        try (PreparedStatement insertStatement = con.prepareStatement(insertQuery)) {
                            insertStatement.setString(1, userName);
                            insertStatement.setInt(2, score);
                            insertStatement.setInt(3, streakCounter);
                            insertStatement.setInt(4, timeInSeconds);

                            int rowsAffected = insertStatement.executeUpdate();
                            if (rowsAffected > 0) {
                                System.out.println("New row inserted in the database.");
                            } else {
                                System.out.println("Failed to insert a new row in the database.");
                            }
                        }
                    } else {
                        // User already has a row in the table, update accordingly
                        if (score > getDatabaseScoreFromDatabase(userName) || timeInSeconds > 0) {
                            String updateQueryScore = "UPDATE casual "
                                    + "SET score = ?, time = ? "
                                    + "WHERE user_id = (SELECT user_id FROM users WHERE user_name = ?)";

                            try (PreparedStatement updateScoreStatement = con.prepareStatement(updateQueryScore)) {
                                updateScoreStatement.setInt(1, score);
                                updateScoreStatement.setString(2, displayFormattedTime(timeInSeconds));
                                updateScoreStatement.setString(3, userName);

                                int rowsAffected = updateScoreStatement.executeUpdate();
                                if (rowsAffected > 0) {
                                    System.out.println("Score and time updated in the database.");
                                } else {
                                    System.out.println("Failed to update score and time in the database.");
                                }
                            }
                        }

                        if (streakCounter > getDatabaseStreakFromDatabase(userName)) {
                            // Update streak only if it's higher
                            String updateQueryStreak = "UPDATE casual "
                                    + "SET streak = ? "
                                    + "WHERE user_id = (SELECT user_id FROM users WHERE user_name = ?)";

                            try (PreparedStatement updateStreakStatement = con.prepareStatement(updateQueryStreak)) {
                                updateStreakStatement.setInt(1, streakCounter);
                                updateStreakStatement.setString(2, userName);

                                int rowsAffected = updateStreakStatement.executeUpdate();
                                if (rowsAffected > 0) {
                                    System.out.println("Streak updated in the database.");
                                } else {
                                    System.out.println("Failed to update Streak in the database.");
                                }
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private int getDatabaseScoreFromDatabase(String userName) {
        int databaseScore = 0;

        Connection con = DatabaseConnector.mycon();

        try {
            String selectQuery = "SELECT score FROM casual "
                    + "INNER JOIN users ON casual.user_id = users.user_id "
                    + "WHERE users.user_name = ?";

            try (PreparedStatement preparedStatement = con.prepareStatement(selectQuery)) {
                preparedStatement.setString(1, userName);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        databaseScore = resultSet.getInt("score");

                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return databaseScore;
    }

    private int getDatabaseStreakFromDatabase(String userName) {

        int databaseStreak = 0;
        Connection con = DatabaseConnector.mycon();

        try {
            String selectQuery = "SELECT streak FROM casual "
                    + "INNER JOIN users ON casual.user_id = users.user_id "
                    + "WHERE users.user_name = ?";

            try (PreparedStatement preparedStatement = con.prepareStatement(selectQuery)) {
                preparedStatement.setString(1, userName);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        databaseStreak = resultSet.getInt("streak");

                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return databaseStreak;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int solution = Integer.parseInt(e.getActionCommand());
        boolean correct = gameEngine.checkSolution(solution);

        if (correct) {
            handleCorrectSolution();
        } else {
            handleIncorrectSolution();
        }

        updateScoreLabel();
        loadLeaderboardData();
        jPanel1.revalidate();
        jPanel1.repaint();
    }

    private void handleCorrectSolution() {
        System.out.println("Correct solution entered!");

        currentGame = gameEngine.nextGame();
        jPanel1.removeAll();
        jPanel1.setLayout(new BorderLayout());

        JLabel imageLabel = new JLabel();
        jPanel1.add(imageLabel);

        // Set the new image
        ImageIcon ii = new ImageIcon(currentGame);

        int panelWidth = jPanel1.getWidth();
        int panelHeight = jPanel1.getHeight();

        ImageIcon initialImageIcon = new ImageIcon(currentGame);
        Image originalImage = initialImageIcon.getImage();
        Image scaledImage = originalImage.getScaledInstance(panelWidth, panelHeight, Image.SCALE_SMOOTH);
        ImageIcon scaledImageIcon = new ImageIcon(scaledImage);
        imageLabel.setIcon(scaledImageIcon);

        txtResult.setText("Good!");

        streakCounter++;
        int streak = streakCounter;

        txtStreak.setText(String.valueOf(streakCounter));
        txtStreak.setVisible(true);
        jTextFieldStreak.setVisible(true);
        txtStreakIcon.setVisible(true);
        txtStreaklbl.setVisible(true);

        //txtAnswerBackgroundHappy.setVisible(true);

        // Start the existing timer
        updateScoreInDatabase( gameEngine.getScore(), streakCounter, timeInSeconds);
    }

    private void handleIncorrectSolution() {
        System.out.println("Not Correct");
        txtResult.setText("Oops. Try again!");

        streakCounter = 0;

        txtStreak.setVisible(false);
        txtStreaklbl.setVisible(false);
        jTextFieldStreak.setVisible(false);
        txtStreakIcon.setVisible(false);

       //
       //txtAnswerBackgroundSad.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton3 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        txtDisplayName = new javax.swing.JLabel();
        txtTimer = new javax.swing.JLabel();
        txtStreaklbl = new javax.swing.JLabel();
        txtResult = new javax.swing.JLabel();
        txtStreak = new javax.swing.JLabel();
        txtScorelbl = new javax.swing.JLabel();
        txtScore = new javax.swing.JLabel();
        jTextFieldStreak3 = new javax.swing.JTextField();
        jTextFieldStreak2 = new javax.swing.JTextField();
        txtStreakIcon = new javax.swing.JLabel();
        jTextFieldStreak = new javax.swing.JTextField();
        jTextFieldScore = new javax.swing.JTextField();
        jTextFieldAnswer = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        txtAnswerBackgroundSad = new javax.swing.JLabel();
        txtAnswerBackgroundHappy = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldAnswer2 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldStreak1 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();

        jButton3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 51, 51));
        jButton3.setText("0");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(0, 102, 102));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 86, 860, 555));

        jTable1.setBackground(new java.awt.Color(0, 102, 102));
        jTable1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTable1.setForeground(new java.awt.Color(255, 255, 255));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Name", "Score", "Streak", "Time"
            }
        ));
        jTable1.setRowHeight(25);
        jScrollPane1.setViewportView(jTable1);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(882, 480, 320, 160));

        txtDisplayName.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        txtDisplayName.setForeground(new java.awt.Color(0, 102, 102));
        txtDisplayName.setText("Display Name");
        getContentPane().add(txtDisplayName, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 20, -1, -1));

        txtTimer.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        txtTimer.setForeground(new java.awt.Color(0, 102, 102));
        txtTimer.setText("Timer : ");
        getContentPane().add(txtTimer, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 20, -1, -1));

        txtStreaklbl.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        txtStreaklbl.setForeground(new java.awt.Color(0, 102, 102));
        txtStreaklbl.setText("Streak:");
        getContentPane().add(txtStreaklbl, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 210, -1, -1));

        txtResult.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        txtResult.setForeground(new java.awt.Color(0, 102, 102));
        txtResult.setText("Select Your Answer");
        getContentPane().add(txtResult, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 110, 190, -1));

        txtStreak.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        txtStreak.setForeground(new java.awt.Color(0, 102, 102));
        txtStreak.setText("0");
        getContentPane().add(txtStreak, new org.netbeans.lib.awtextra.AbsoluteConstraints(1020, 210, -1, -1));

        txtScorelbl.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        txtScorelbl.setForeground(new java.awt.Color(0, 102, 102));
        txtScorelbl.setText("Score:");
        getContentPane().add(txtScorelbl, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 170, -1, -1));

        txtScore.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        txtScore.setForeground(new java.awt.Color(0, 102, 102));
        txtScore.setText("0");
        getContentPane().add(txtScore, new org.netbeans.lib.awtextra.AbsoluteConstraints(1000, 170, -1, -1));

        jTextFieldStreak3.setText(" ");
        jTextFieldStreak3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 102, 102)));
        jTextFieldStreak3.setEnabled(false);
        getContentPane().add(jTextFieldStreak3, new org.netbeans.lib.awtextra.AbsoluteConstraints(1020, 20, 150, 30));

        jTextFieldStreak2.setText(" ");
        jTextFieldStreak2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 102, 102)));
        jTextFieldStreak2.setEnabled(false);
        getContentPane().add(jTextFieldStreak2, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 20, 90, 30));

        txtStreakIcon.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        txtStreakIcon.setForeground(new java.awt.Color(255, 255, 255));
        txtStreakIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Tomato Streak RESIZED.png"))); // NOI18N
        txtStreakIcon.setText(" ");
        getContentPane().add(txtStreakIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 210, -1, 30));

        jTextFieldStreak.setText(" ");
        jTextFieldStreak.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 102, 102)));
        jTextFieldStreak.setEnabled(false);
        getContentPane().add(jTextFieldStreak, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 210, 160, 30));

        jTextFieldScore.setText(" ");
        jTextFieldScore.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 102, 102)));
        jTextFieldScore.setEnabled(false);
        getContentPane().add(jTextFieldScore, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 170, 100, 30));

        jTextFieldAnswer.setText(" ");
        jTextFieldAnswer.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 102, 102)));
        jTextFieldAnswer.setEnabled(false);
        getContentPane().add(jTextFieldAnswer, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 110, 220, 30));

        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtAnswerBackgroundSad.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Tomato Sad RESIZED.png"))); // NOI18N
        txtAnswerBackgroundSad.setText(" ");
        jPanel2.add(txtAnswerBackgroundSad, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 260, -1));

        txtAnswerBackgroundHappy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Tomato Happy RESIZED.png"))); // NOI18N
        txtAnswerBackgroundHappy.setText(" ");
        jPanel2.add(txtAnswerBackgroundHappy, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 260, -1));

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 90, 300, 550));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 102, 102));
        jLabel2.setText("Select the answer:");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 670, -1, -1));

        jTextFieldAnswer2.setText(" ");
        jTextFieldAnswer2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 102, 102)));
        jTextFieldAnswer2.setEnabled(false);
        getContentPane().add(jTextFieldAnswer2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 670, 200, 30));

        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton2.setForeground(new java.awt.Color(0, 102, 102));
        jButton2.setText("0");
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 670, -1, -1));

        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton4.setForeground(new java.awt.Color(0, 102, 102));
        jButton4.setText("1");
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 670, -1, -1));

        jButton5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton5.setForeground(new java.awt.Color(0, 102, 102));
        jButton5.setText("2");
        getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 670, -1, -1));

        jButton6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton6.setForeground(new java.awt.Color(0, 102, 102));
        jButton6.setText("3");
        getContentPane().add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 670, -1, -1));

        jButton7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton7.setForeground(new java.awt.Color(0, 102, 102));
        jButton7.setText("4");
        getContentPane().add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 670, -1, -1));

        jButton8.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton8.setForeground(new java.awt.Color(0, 102, 102));
        jButton8.setText("5");
        getContentPane().add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 670, -1, -1));

        jButton9.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton9.setForeground(new java.awt.Color(0, 102, 102));
        jButton9.setText("6");
        getContentPane().add(jButton9, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 670, -1, -1));

        jButton10.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton10.setForeground(new java.awt.Color(0, 102, 102));
        jButton10.setText("7");
        getContentPane().add(jButton10, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 670, -1, -1));

        jButton11.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton11.setForeground(new java.awt.Color(0, 102, 102));
        jButton11.setText("8");
        getContentPane().add(jButton11, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 670, -1, -1));

        jButton12.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton12.setForeground(new java.awt.Color(0, 102, 102));
        jButton12.setText("9");
        getContentPane().add(jButton12, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 670, -1, -1));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 102, 102));
        jLabel1.setText("What is the value of the Tomato?");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, -1, -1));

        jTextFieldStreak1.setText(" ");
        jTextFieldStreak1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 102, 102)));
        jTextFieldStreak1.setEnabled(false);
        getContentPane().add(jTextFieldStreak1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 400, 40));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Casual  Background.png"))); // NOI18N
        jLabel4.setText(" ");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1210, 730));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Casual.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Casual.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Casual.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Casual.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Casual().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextFieldAnswer;
    private javax.swing.JTextField jTextFieldAnswer2;
    private javax.swing.JTextField jTextFieldScore;
    private javax.swing.JTextField jTextFieldStreak;
    private javax.swing.JTextField jTextFieldStreak1;
    private javax.swing.JTextField jTextFieldStreak2;
    private javax.swing.JTextField jTextFieldStreak3;
    private javax.swing.JLabel txtAnswerBackgroundHappy;
    private javax.swing.JLabel txtAnswerBackgroundSad;
    private javax.swing.JLabel txtDisplayName;
    private javax.swing.JLabel txtResult;
    private javax.swing.JLabel txtScore;
    private javax.swing.JLabel txtScorelbl;
    private javax.swing.JLabel txtStreak;
    private javax.swing.JLabel txtStreakIcon;
    private javax.swing.JLabel txtStreaklbl;
    private javax.swing.JLabel txtTimer;
    // End of variables declaration//GEN-END:variables
}
