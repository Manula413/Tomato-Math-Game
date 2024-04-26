/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package GUI;

import GameEngine.GameEngineCampaign;
import DataBase.DatabaseConnector;
import GameEngine.GameLevel;
import java.awt.BorderLayout;
import java.awt.Image;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.*;

/**
 *
 * @author manula
 */
public class Campaign extends javax.swing.JFrame implements ActionListener {

    private GameEngineCampaign gameEngine;
    private BufferedImage currentGame;
    private int streakCounter = 0;

    private Timer gameTimer;
    private int remainingTimeInSeconds;

    private GameLevel[] gameLevels;
    private GameLevel currentLevel;

    /**
     * Creates new form Casual
     */
    private String userName;

    public Campaign() {
        initComponents();
        setupTableAndBackgroundComponents();

        initGame();
        initTimer();
    }

    public Campaign(String userName) {
        initComponents();
        setupTableAndBackgroundComponents();
        initGame();
        initTimer();

        this.userName = userName;

        String displayname = getDisplayNameByUsername(userName);
        txtDisplayName.setText(displayname);
        initializeGameLevels();

    }

    private void setupTableAndBackgroundComponents() {

        jPanel2.setBackground(new Color(0, 0, 0, 60));

        txtAnswerBackgroundSad.setVisible(false);
        txtAnswerBackgroundHappy.setVisible(false);
    }

    private void initializeGameLevels() {

        gameLevels = gameEngine.getGameLevels();
    }

    private void initGame() {
        gameEngine = new GameEngineCampaign("Player");
        currentGame = gameEngine.nextGame();

        jPanel1.setLayout(new BorderLayout());

        JLabel imageLabel = new JLabel();
        ImageIcon initialImageIcon = new ImageIcon(currentGame);

        jPanel1.add(imageLabel);

        Image originalImage = initialImageIcon.getImage();

        int panelWidth = jPanel1.getWidth();
        int panelHeight = jPanel1.getHeight();
        Image scaledImage = originalImage.getScaledInstance(panelWidth, panelHeight, Image.SCALE_SMOOTH);

        ImageIcon scaledImageIcon = new ImageIcon(scaledImage);
        imageLabel.setIcon(scaledImageIcon);

        JButton[] answerButtons = {
            jButton2, jButton4, jButton5, jButton6, jButton7,
            jButton8, jButton9, jButton10, jButton11, jButton12
        };

        for (int i = 0; i < answerButtons.length; i++) {
            answerButtons[i].setText(String.valueOf(i));
            answerButtons[i].addActionListener(this);
        }
        txtStreak.setVisible(false);
        txtStreaklbl.setVisible(false);
        jTextFieldStreak.setVisible(false);
        txtStreakIcon.setVisible(false);
    }

    private void initTimer() {
        remainingTimeInSeconds = gameEngine.getCurrentLevel().getTimeLimitInSeconds();
        System.out.println("Initial Time Limit: " + remainingTimeInSeconds);
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleTimerTick();
            }
        });
        gameTimer.start();
    }

    private void handleTimerTick() {
        remainingTimeInSeconds--;

        if (remainingTimeInSeconds >= 0) {

            int minutes = remainingTimeInSeconds / 60;
            int seconds = remainingTimeInSeconds % 60;
            String formattedTime = String.format("%02d:%02d", minutes, seconds);
            txtTimer.setText(formattedTime);
        } else {

            gameTimer.stop();
            handleGameTimer();
        }
    }

    private void handleGameTimer() {
        // Display game-over message with total attempts, correct answers, and time elapsed
        GameOver gameOverForm = new GameOver(userName);
        gameOverForm.setVisible(true);
        gameTimer.stop();
        this.dispose();
    }

    private void stopTimer() {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
    }

    private void updateScoreLabel() {
        int score = gameEngine.getScore();
        txtScore.setText(String.valueOf(score));

    }

    private void updateScoreItoTheDatabase(String userName, int remainingTimeInSeconds) {
        Connection con = DatabaseConnector.mycon();

        try {
            int playerId = getPlayerIdByUsername(userName);
            int levelId = gameEngine.getCurrentLevelNumber();
            int correctAnswers = gameEngine.getCorrectAnswers();
            int wrongAnswers = gameEngine.getTotalAttempts() - correctAnswers;

            int timeToCompleteInSeconds = gameEngine.getCurrentLevel().getTimeLimitInSeconds() - remainingTimeInSeconds;
            int minutes = timeToCompleteInSeconds / 60;
            int seconds = timeToCompleteInSeconds % 60;
            String formattedTime = String.format("%02d:%02d", minutes, seconds);

            String insertQuery = "INSERT INTO campaign (player_id, level_id, correct_answers, wrong_answers, time_to_complete) VALUES (?, ?,  ?, ?, ?)";

            try (PreparedStatement preparedStatement = con.prepareStatement(insertQuery)) {
                preparedStatement.setInt(1, playerId);
                preparedStatement.setInt(2, levelId);
                preparedStatement.setInt(3, correctAnswers);
                preparedStatement.setInt(4, wrongAnswers);
                preparedStatement.setString(5, formattedTime);
                preparedStatement.executeUpdate();

                System.out.println("Level progress and score stored to the database.");

            } catch (SQLException e) {
                e.printStackTrace();
            }

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

    private int getPlayerIdByUsername(String username) {
        int playerId = -1;

        Connection con = DatabaseConnector.mycon();

        try {
            String selectQuery = "SELECT user_id  FROM users WHERE user_name = ?";
            try (PreparedStatement preparedStatement = con.prepareStatement(selectQuery)) {
                preparedStatement.setString(1, username);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        playerId = resultSet.getInt("user_id");
                        System.out.println("ID is: " + playerId);
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

        return playerId;
    }

    private String getDisplayNameByUsername(String username) {
        String displayName = null; // Initialize to null

        Connection con = DatabaseConnector.mycon();

        try {
            String selectQuery = "SELECT display_name FROM users WHERE user_name = ?";
            try (PreparedStatement preparedStatement = con.prepareStatement(selectQuery)) {
                preparedStatement.setString(1, username);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        displayName = resultSet.getString("display_name"); // Correct assignment

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

        return displayName; // Return the display name
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int solution = Integer.parseInt(e.getActionCommand());
        boolean correct = gameEngine.checkSolution(solution);

        System.out.println("ActionPerformed - Correct: " + correct);

        if (correct) {
            System.out.println("Correct solution entered!");

            currentGame = gameEngine.nextGame();
            checkGameOver(userName);
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

        } else {
            System.out.println("Not Correct");
            txtResult.setText("Oops. Try again!");

            streakCounter = 0;

            currentGame = gameEngine.nextGame();
            checkGameOver(userName); // Check game over condition before updating the image
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

            txtStreak.setVisible(false);
            txtStreaklbl.setVisible(false);
            jTextFieldStreak.setVisible(false);
            txtStreakIcon.setVisible(false);

           // txtAnswerBackgroundSad.setVisible(true);
        }

        // Update the score label and load leaderboard data
        updateScoreLabel();

        jPanel1.revalidate();
        jPanel1.repaint();
    }

    private void checkGameOver(String userName) {

        System.out.println("Total Attempts: " + gameEngine.getTotalAttempts());
        System.out.println("Correct Answers: " + gameEngine.getCorrectAnswers());
        System.out.println("Correct Answers for the level: " + gameEngine.getCurrentLevel().getCorrectAnswersToPass());

        if (gameEngine.getTotalAttempts() >= gameEngine.getCurrentLevel().getAnswerCount()) {

            if (gameEngine.getCorrectAnswers() >= gameEngine.getCurrentLevel().getCorrectAnswersToPass()) {

                updateScoreItoTheDatabase(userName, remainingTimeInSeconds);
                LevelComplete levelCompleteForm = new LevelComplete();
                levelCompleteForm.setVisible(true);

                Timer timer = new Timer(2000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        levelCompleteForm.dispose(); // Close the LevelComplete form
                        updateScoreItoTheDatabase(userName, remainingTimeInSeconds);

                        moveNextLevel();
                    }
                });
                timer.setRepeats(false);
                timer.start();

            } else {

                GameOver gameOverForm = new GameOver(userName);
                gameOverForm.setVisible(true);

                this.dispose();
            }
        } else {

        }
    }

    private void moveNextLevel() {
        setupTableAndBackgroundComponents();
        int nextLevelIndex = gameEngine.getNextLevelIndex();
        if (nextLevelIndex != -1) {
            gameEngine.setCurrentLevel(gameEngine.getGameLevels()[nextLevelIndex]);
            gameEngine.resetAnsweredQuestions(); // Reset answeredQuestions for the new level
            gameEngine.resetCorrectAnswers();
            gameEngine.resetTotalAttempts();// Reset correctAnswers for the new level
            stopTimer();
            gameTimer = null;
            initTimer();                // Restart the timer for the new level
            updateLevelLabel();         // Update UI to display the current level
            // Optionally, update other UI elements or perform additional actions for the new level
        } else {
            // No more levels, the player has completed the campaign
            JOptionPane.showMessageDialog(
                    this,
                    "Congratulations! You completed the campaign!\nTotal Score: " + gameEngine.getScore()
            );
            this.dispose();  // Close the Campaign window or take other actions as needed
        }
    }

    private void updateLevelLabel() {
        txtLevel.setText("Level: " + gameEngine.getCurrentLevelNumber());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton3 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        txtDisplayName = new javax.swing.JLabel();
        txtTimer = new javax.swing.JLabel();
        txtStreaklbl = new javax.swing.JLabel();
        txtResult = new javax.swing.JLabel();
        txtStreak = new javax.swing.JLabel();
        txtScorelbl = new javax.swing.JLabel();
        txtScore = new javax.swing.JLabel();
        txtLevel = new javax.swing.JLabel();
        jTextFieldStreak4 = new javax.swing.JTextField();
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
        jTextFieldStreak5 = new javax.swing.JTextField();
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 86, 880, 555));

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        getContentPane().add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 86, 860, 555));

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

        txtLevel.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        txtLevel.setForeground(new java.awt.Color(0, 102, 102));
        txtLevel.setText("Level: 1");
        getContentPane().add(txtLevel, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 20, -1, -1));

        jTextFieldStreak4.setText(" ");
        jTextFieldStreak4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 102, 102)));
        jTextFieldStreak4.setEnabled(false);
        jTextFieldStreak4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldStreak4ActionPerformed(evt);
            }
        });
        getContentPane().add(jTextFieldStreak4, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 20, 90, 30));

        jTextFieldStreak3.setText(" ");
        jTextFieldStreak3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 102, 102)));
        jTextFieldStreak3.setEnabled(false);
        getContentPane().add(jTextFieldStreak3, new org.netbeans.lib.awtextra.AbsoluteConstraints(1020, 20, 150, 30));

        jTextFieldStreak2.setText(" ");
        jTextFieldStreak2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 102, 102)));
        jTextFieldStreak2.setEnabled(false);
        jTextFieldStreak2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldStreak2ActionPerformed(evt);
            }
        });
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

        jTextFieldStreak5.setText(" ");
        jTextFieldStreak5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 102, 102)));
        jTextFieldStreak5.setEnabled(false);
        jTextFieldStreak5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldStreak5ActionPerformed(evt);
            }
        });
        getContentPane().add(jTextFieldStreak5, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 20, 90, 30));

        jTextFieldAnswer2.setText(" ");
        jTextFieldAnswer2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 102, 102)));
        jTextFieldAnswer2.setEnabled(false);
        getContentPane().add(jTextFieldAnswer2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 670, 200, 30));

        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton2.setForeground(new java.awt.Color(0, 102, 102));
        jButton2.setText("0");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 670, -1, -1));

        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton4.setForeground(new java.awt.Color(0, 102, 102));
        jButton4.setText("1");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 670, -1, -1));

        jButton5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton5.setForeground(new java.awt.Color(0, 102, 102));
        jButton5.setText("2");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 670, -1, -1));

        jButton6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton6.setForeground(new java.awt.Color(0, 102, 102));
        jButton6.setText("3");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 670, -1, -1));

        jButton7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton7.setForeground(new java.awt.Color(0, 102, 102));
        jButton7.setText("4");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 670, -1, -1));

        jButton8.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton8.setForeground(new java.awt.Color(0, 102, 102));
        jButton8.setText("5");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 670, -1, -1));

        jButton9.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton9.setForeground(new java.awt.Color(0, 102, 102));
        jButton9.setText("6");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton9, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 670, -1, -1));

        jButton10.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton10.setForeground(new java.awt.Color(0, 102, 102));
        jButton10.setText("7");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton10, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 670, -1, -1));

        jButton11.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton11.setForeground(new java.awt.Color(0, 102, 102));
        jButton11.setText("8");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton11, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 670, -1, -1));

        jButton12.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton12.setForeground(new java.awt.Color(0, 102, 102));
        jButton12.setText("9");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });
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

    private void jTextFieldStreak2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldStreak2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldStreak2ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jTextFieldStreak4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldStreak4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldStreak4ActionPerformed

    private void jTextFieldStreak5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldStreak5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldStreak5ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {


        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Campaign().setVisible(true);
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
    private javax.swing.JPanel jPanel6;
    private javax.swing.JTextField jTextFieldAnswer;
    private javax.swing.JTextField jTextFieldAnswer2;
    private javax.swing.JTextField jTextFieldScore;
    private javax.swing.JTextField jTextFieldStreak;
    private javax.swing.JTextField jTextFieldStreak1;
    private javax.swing.JTextField jTextFieldStreak2;
    private javax.swing.JTextField jTextFieldStreak3;
    private javax.swing.JTextField jTextFieldStreak4;
    private javax.swing.JTextField jTextFieldStreak5;
    private javax.swing.JLabel txtAnswerBackgroundHappy;
    private javax.swing.JLabel txtAnswerBackgroundSad;
    private javax.swing.JLabel txtDisplayName;
    private javax.swing.JLabel txtLevel;
    private javax.swing.JLabel txtResult;
    private javax.swing.JLabel txtScore;
    private javax.swing.JLabel txtScorelbl;
    private javax.swing.JLabel txtStreak;
    private javax.swing.JLabel txtStreakIcon;
    private javax.swing.JLabel txtStreaklbl;
    private javax.swing.JLabel txtTimer;
    // End of variables declaration//GEN-END:variables
}
