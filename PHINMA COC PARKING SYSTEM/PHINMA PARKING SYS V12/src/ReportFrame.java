import java.sql.DriverManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author RYAN
 */
public class ReportFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ReportFrame.class.getName());
    private Connection con;

    public ReportFrame() {
        initComponents();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        Connect();   // connect to DB
        
    }
    
    // ---------------- DB CONNECTION ----------------
    private void Connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/parkingdb",
                    "root",
                    ""
            );
            System.out.println("Connected to MySQL!");
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Parking.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this,
                    "Database connection failed:\n" + ex.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }  // <--- end of Connect()
    
  //--------------------------Load Report Table-------------------------
    
    private void loadTable2Data(Date startDate, Date endDate) {
    // Define columns
    String[] columns = {
        "ID Number", "First Name", "Middle Name", "Last Name",
        "Affiliation", "Contact", "Address",
        "Plate Number", "Vehicle Brand", "Vehicle Type", "Color",
        "Parking In", "Parking Out"
    };

    DefaultTableModel model = new DefaultTableModel();
    model.setColumnIdentifiers(columns);
    model.setRowCount(0); // clear previous data

    String sql = "SELECT p.id_number, p.first_name, p.middle_name, p.last_name, " +
                 "       p.affiliation, COALESCE(p.contact, 'None') AS contact, " +
                 "       COALESCE(p.address, '') AS address, " +
                 "       v.plate_number, v.vehicle_brand, v.vehicle_type, v.color, " +
                 "       pt.parking_in, pt.parking_out " +
                 "FROM person p " +
                 "LEFT JOIN vehicle v ON p.person_id = v.person_id " +
                 "LEFT JOIN parkingtransaction pt ON v.vehicle_id = pt.vehicle_id " +
                 "WHERE (pt.parking_in BETWEEN ? AND ? " +
                 "   OR pt.parking_out BETWEEN ? AND ?) " +
                 "ORDER BY pt.parking_in DESC";

    try (PreparedStatement pst = con.prepareStatement(sql)) {

        // ✅ Convert java.util.Date → LocalDate safely
        java.time.LocalDate localStart = ((java.sql.Date) new java.sql.Date(startDate.getTime())).toLocalDate();
        java.time.LocalDate localEnd = ((java.sql.Date) new java.sql.Date(endDate.getTime())).toLocalDate();

        // ✅ Full-day range: from 00:00:00 to 23:59:59
        java.time.LocalDateTime startDateTime = localStart.atStartOfDay();
        java.time.LocalDateTime endDateTime = localEnd.plusDays(1).atStartOfDay().minusSeconds(1);

        java.sql.Timestamp sqlStart = java.sql.Timestamp.valueOf(startDateTime);
        java.sql.Timestamp sqlEnd = java.sql.Timestamp.valueOf(endDateTime);

        pst.setTimestamp(1, sqlStart);
        pst.setTimestamp(2, sqlEnd);
        pst.setTimestamp(3, sqlStart);
        pst.setTimestamp(4, sqlEnd);

        try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("id_number"),
                    rs.getString("first_name"),
                    rs.getString("middle_name"),
                    rs.getString("last_name"),
                    rs.getString("affiliation"),
                    rs.getString("contact"),
                    rs.getString("address"),
                    rs.getString("plate_number"),
                    rs.getString("vehicle_brand"),
                    rs.getString("vehicle_type"),
                    rs.getString("color"),
                    rs.getTimestamp("parking_in"),
                    rs.getTimestamp("parking_out")
                });
            }
        }

        jTable2.setModel(model);

        // ✅ Enable column sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        jTable2.setRowSorter(sorter);
        sorter.setSortKeys(Arrays.asList(
            new RowSorter.SortKey(11, SortOrder.DESCENDING)
        ));
        sorter.sort();

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
            "Error loading data: " + ex.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE);
    }
}
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        btnSearchRep = new javax.swing.JButton();
        btnPrint = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        datePicker3 = new com.github.lgooddatepicker.components.DatePicker();
        datePicker4 = new com.github.lgooddatepicker.components.DatePicker();
        btnBackReport = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("PARKING TRANSACTION REPORT");
        jLabel1.setFont(new java.awt.Font("Streetvertising", 0, 18)); // NOI18N

        jLabel2.setText("From:");
        jLabel2.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N

        jLabel3.setText("To:");
        jLabel3.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID Number", "First Name", "Middle Name", "Last Name", "Affiliation", "Contact", "Address", "Plate number", "Vehicle brand", "Vehicle type", "Color"
            }
        ));
        jTable2.setSelectionBackground(new java.awt.Color(0, 0, 0));
        jScrollPane1.setViewportView(jTable2);

        btnSearchRep.setIcon(new javax.swing.ImageIcon(getClass().getResource("/searchrmvbg.gif"))); // NOI18N
        btnSearchRep.setText("SEARCH");
        btnSearchRep.setBackground(new java.awt.Color(204, 204, 204));
        btnSearchRep.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        btnSearchRep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchRepActionPerformed(evt);
            }
        });

        btnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/printermvbg.gif"))); // NOI18N
        btnPrint.setText("PRINT");
        btnPrint.setBackground(new java.awt.Color(204, 204, 204));
        btnPrint.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });

        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/exportrmvbg.gif"))); // NOI18N
        btnExport.setText("EXPORT");
        btnExport.setBackground(new java.awt.Color(204, 204, 204));
        btnExport.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cocsmall.png"))); // NOI18N

        btnBackReport.setText("BACK");
        btnBackReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackReportActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(44, 44, 44)
                                .addComponent(btnPrint, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(134, 134, 134)
                                .addComponent(btnExport))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(datePicker3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(46, 46, 46)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(datePicker4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(58, 58, 58)
                                .addComponent(btnSearchRep)))
                        .addGap(0, 298, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btnBackReport)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(jLabel4))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnBackReport)
                        .addGap(26, 26, 26)
                        .addComponent(jLabel1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(datePicker3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(datePicker4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearchRep))
                .addGap(27, 27, 27)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(58, 58, 58)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnPrint, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(72, 72, 72))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSearchRepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchRepActionPerformed
        // TODO add your handling code here:
        java.time.LocalDate startDate = datePicker3.getDate();
        java.time.LocalDate endDate = datePicker4.getDate();

        if (startDate == null || endDate == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select both start and end dates.",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convert LocalDate → java.util.Date
        Date utilStart = java.sql.Date.valueOf(startDate);
        Date utilEnd = java.sql.Date.valueOf(endDate);

        loadTable2Data(utilStart, utilEnd);
    }//GEN-LAST:event_btnSearchRepActionPerformed

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
        // TODO add your handling code here:
        try {
        boolean complete = jTable2.print(
                javax.swing.JTable.PrintMode.FIT_WIDTH, // Fit the table to page width
                new java.text.MessageFormat("Parking Transaction Report"), // Header
                new java.text.MessageFormat("Page - {0}") // Footer with page number
        );

        if (complete) {
            JOptionPane.showMessageDialog(this,
                    "Printing Complete",
                    "Print Status",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Printing Cancelled",
                    "Print Status",
                    JOptionPane.WARNING_MESSAGE);
        }
    } catch (java.awt.print.PrinterException e) {
        JOptionPane.showMessageDialog(this,
                "Printing Failed: " + e.getMessage(),
                "Print Error",
                JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_btnPrintActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        // TODO add your handling code here:
    javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
    fileChooser.setDialogTitle("Save Report");
    int userSelection = fileChooser.showSaveDialog(this);

    if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
        java.io.File fileToSave = fileChooser.getSelectedFile();

        // Ensure file has .csv extension
        if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
            fileToSave = new java.io.File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
        }

        try (java.io.FileWriter fw = new java.io.FileWriter(fileToSave)) {
            DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
            int colCount = model.getColumnCount();

            // Write header
            for (int i = 0; i < colCount; i++) {
                fw.write(model.getColumnName(i));
                if (i < colCount - 1) fw.write(",");
            }
            fw.write("\n");

            // Write rows
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < colCount; col++) {
                    Object value = model.getValueAt(row, col);
                    fw.write(value == null ? "" : value.toString());
                    if (col < colCount - 1) fw.write(",");
                }
                fw.write("\n");
            }

            fw.flush();
            JOptionPane.showMessageDialog(this, "Export successful: " + fileToSave.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    }//GEN-LAST:event_btnExportActionPerformed

    private void btnBackReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackReportActionPerformed
        // TODO add your handling code here:
        // Close current window
    this.dispose();

    // Return to login form
    java.awt.EventQueue.invokeLater(() -> {
        new Parking().setVisible(true);
    });
    }//GEN-LAST:event_btnBackReportActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new ReportFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBackReport;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnSearchRep;
    private com.github.lgooddatepicker.components.DatePicker datePicker3;
    private com.github.lgooddatepicker.components.DatePicker datePicker4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable2;
    // End of variables declaration//GEN-END:variables
}
