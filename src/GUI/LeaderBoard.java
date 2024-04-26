/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package GUI;

import DataBase.DatabaseConnector;
import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author manula
 */
public class LeaderBoard extends javax.swing.JFrame {

    private final String userName;

    /**
     * Creates new form Register
     */
    public LeaderBoard(String userName) {

        this.userName = userName;
        initComponents();
        setupTableAndBackgroundComponents();
        loadCasualData();
        loadCampaignData(userName);

        String displayName = getDisplayNameFromDatabase(userName);
        jLabel5.setText(displayName);

    }

    private String getDisplayNameFromDatabase(String userName) {
        String displayName = null;

        // Database connection
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

    private void setupTableAndBackgroundComponents() {
        // Setup jTable1 appearance
        jTable1.setOpaque(false);
        jTable1.setShowGrid(false); // Hide grid lines
        jTable1.setBorder(BorderFactory.createEmptyBorder()); // Remove table border
        ((DefaultTableCellRenderer) jTable1.getDefaultRenderer(Object.class)).setOpaque(false);

        jTable2.setOpaque(false);
        jTable2.setShowGrid(false); // Hide grid lines
        jTable2.setBorder(BorderFactory.createEmptyBorder()); // Remove table border
        ((DefaultTableCellRenderer) jTable2.getDefaultRenderer(Object.class)).setOpaque(false);

        jScrollPane1.setOpaque(false);
        jScrollPane1.getViewport().setOpaque(false);

        jPanel1.setBackground(new Color(0, 0, 0, 60));

        jScrollPane2.setOpaque(false);
        jScrollPane2.getViewport().setOpaque(false);

        jPanel2.setBackground(new Color(0, 0, 0, 60));

    }

    private void loadCasualData() {
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

    private void loadCampaignData(String userName) {
        try {
            Connection con = DatabaseConnector.mycon();

            // Get the user ID based on the provided userName
            int currentUserId = getCurrentUserId(userName);

            String query = "SELECT users.display_name, campaign.level_id, campaign.correct_answers, campaign.time_to_complete "
                    + "FROM campaign "
                    + "INNER JOIN users ON campaign.player_id = users.user_id "
                    + "WHERE users.user_id = ? " // Add a condition for the current user
                    + "ORDER BY campaign.correct_answers DESC LIMIT 5";

            try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
                preparedStatement.setInt(1, currentUserId);  // Set the parameter for the current user ID
                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    DefaultTableModel model = new DefaultTableModel(new String[]{"Name", "Level Progress", "Score", "Time"}, 0);

                    boolean hasData = false;  // Flag to check if any data is loaded

                    while (resultSet.next()) {
                        String name = resultSet.getString("display_name");
                        int levelId = resultSet.getInt("level_id");
                        int score = resultSet.getInt("correct_answers");
                        String time = resultSet.getString("time_to_complete");

                        model.addRow(new Object[]{name, levelId, score, time});
                        hasData = true;  // Set the flag to true since data is loaded
                    }

                    // If no data is loaded, add an empty row to the table
                    if (!hasData) {
                        model.addRow(new Object[]{"", 0, 0, ""});  // Adjust the default values as needed
                    }

                    jTable2.setModel(model);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getCurrentUserId(String userName) {
        int currentUserId = -1;  // Default value if user ID is not found

        Connection con = DatabaseConnector.mycon();

        try {
            String selectQuery = "SELECT user_id FROM users WHERE user_name = ?";
            try (PreparedStatement preparedStatement = con.prepareStatement(selectQuery)) {

                preparedStatement.setString(1, userName);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        currentUserId = resultSet.getInt("user_id");
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

        return currentUserId;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton7 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButton8 = new javax.swing.JButton();
        lblBackground = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Felix Titling", 0, 36)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 102, 102));
        jLabel3.setText("Leaderboard");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 30, -1, -1));

        jTextField2.setText(" ");
        jTextField2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 1, true));
        jTextField2.setEnabled(false);
        getContentPane().add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 30, 380, 40));

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable1.setBackground(new java.awt.Color(0, 102, 102));
        jTable1.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
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

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 610, 240));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 160, 610, 240));

        jButton7.setFont(new java.awt.Font("Unispace", 1, 18)); // NOI18N
        jButton7.setForeground(new java.awt.Color(0, 102, 102));
        jButton7.setText("Campaign");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 420, 150, -1));

        jButton6.setFont(new java.awt.Font("Unispace", 1, 18)); // NOI18N
        jButton6.setForeground(new java.awt.Color(255, 0, 0));
        jButton6.setText("Back");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 600, 130, -1));

        jLabel5.setFont(new java.awt.Font("Unispace", 1, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("USER");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 20, -1, -1));

        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable2.setBackground(new java.awt.Color(0, 102, 102));
        jTable2.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jTable2.setForeground(new java.awt.Color(255, 255, 255));
        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Level", "Progress", "Score", "Time"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(jTable2);

        jPanel2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 610, 180));

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 460, 610, 180));

        jButton8.setFont(new java.awt.Font("Unispace", 1, 18)); // NOI18N
        jButton8.setForeground(new java.awt.Color(0, 102, 102));
        jButton8.setText("Casual");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 120, 150, -1));

        lblBackground.setForeground(new java.awt.Color(204, 204, 204));
        lblBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Scoreboard Background.png"))); // NOI18N
        getContentPane().add(lblBackground, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1020, 650));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:

        Dashboard1 dash = new Dashboard1();
        dash.setVisible(true);
        this.dispose();

    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton8ActionPerformed

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
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LeaderBoard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new LeaderBoard("player").setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JLabel lblBackground;
    // End of variables declaration//GEN-END:variables
}
