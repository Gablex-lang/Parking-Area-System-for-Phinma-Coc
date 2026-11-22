import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;




public class Parking extends javax.swing.JFrame {

   // ---------------- for parking capacity ----------------
    public static int maxCarSlots = 3;
    public static int maxMcSlots  = 2;
    
    
    private Connection con;

    public Parking() {
        initComponents();
        Connect();   // create DB connection
        loadTableData();  
        updateTotalParked();    // load table after connecting
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
    
    private void updateParkingStatus() {
    String sql =
        "SELECT v.vehicle_type, COUNT(*) AS cnt " +
        "FROM parkingtransaction pt " +
        "JOIN vehicle v ON pt.vehicle_id = v.vehicle_id " +
        "WHERE pt.parking_out IS NULL " +
        "AND v.vehicle_type IN ('Car', 'Motorcycle') " +
        "GROUP BY v.vehicle_type";

    int totalCars = 0;
    int totalMotorcycles = 0;

    try (PreparedStatement pst = con.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        while (rs.next()) {
            String type = rs.getString("vehicle_type");
            int total = rs.getInt("cnt");

            if ("Car".equalsIgnoreCase(type)) {
                totalCars = total;
            } else if ("Motorcycle".equalsIgnoreCase(type)) {
                totalMotorcycles = total;
            }
        }

        // Update totals
        txtTotalCarsParked.setText(String.valueOf(totalCars));
        txtTotalMParked.setText(String.valueOf(totalMotorcycles));
        txtCapacity.setText(String.valueOf(totalCars + totalMotorcycles));

        // Available slots
        int maxCars = Integer.parseInt(txtMaxCarSlots.getText().trim());
        int maxMcs  = Integer.parseInt(txtMaxMcSlots.getText().trim());

        int availableCars = maxCars - totalCars;
        int availableMcs  = maxMcs - totalMotorcycles;

        txtCarsAvailableSlot.setText(String.valueOf(availableCars));
        txtMAvailableSlot.setText(String.valueOf(availableMcs));

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Failed to count parked vehicles:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }
}

    // ---------------- METHOD TO LOAD DATA TO Main Dashboard ----------------
    private void loadTableData() {
      String[] columns = {"ID Number", "Lastname",
                        "Plate Number", "Vehicle Type",
                        "Parking In", "Parking Out"};
    DefaultTableModel model = new DefaultTableModel();
    model.setColumnIdentifiers(columns);

    String sql = """
        SELECT d.id_number, d.last_name,
               v.plate_number, v.vehicle_type,
               pt.parking_in, pt.parking_out
          FROM parkingtransaction pt
          JOIN vehicle v ON pt.vehicle_id = v.vehicle_id
          JOIN driver d ON v.driver_id = d.driver_id
         WHERE pt.parking_in IS NOT NULL
         ORDER BY pt.parking_in DESC
    """;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a"); // custom format

    try (PreparedStatement pst = con.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        while (rs.next()) {
            Timestamp in  = rs.getTimestamp("parking_in");
            Timestamp out = rs.getTimestamp("parking_out");

            model.addRow(new Object[] {
                rs.getString("id_number"),
                rs.getString("last_name"),
                rs.getString("plate_number"),
                rs.getString("vehicle_type"),  // ✅ added vehicle type
                in,   // keep Timestamp for sorting
                out   // keep Timestamp for sorting
            });
        }

        jTable1.setModel(model);

        // === Enable Sorting ===
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        sorter.setComparator(4, Comparator.nullsLast(Comparator.naturalOrder())); // parking_in
        sorter.setComparator(5, Comparator.nullsLast(Comparator.naturalOrder())); // parking_out
        jTable1.setRowSorter(sorter);

        // Default sort: latest parking_in
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(4, SortOrder.DESCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();

        // === Custom cell renderer to format display ===
        DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer() {
            private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
            @Override
            public void setValue(Object value) {
                if (value instanceof Timestamp) {
                    setText(df.format((Timestamp) value));
                } else {
                    setText("");
                }
            }
        };

        jTable1.getColumnModel().getColumn(4).setCellRenderer(dateRenderer); // parking_in col
        jTable1.getColumnModel().getColumn(5).setCellRenderer(dateRenderer); // parking_out col

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
            "Error loading table: " + ex.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE);
    }

}

     // === put the clearAllFields() method here ===
    private void clearAllFields() {
        txtSearch.setText("");
        txtID.setText("");
        txtFname.setText("");
        txtMname.setText("");
        txtSname.setText("");
        txtContact.setText("");
        txtAddress.setText("");
        txtPlate.setText("");
        txtAffiliation.setText("");
        txtVBrand.setText("");
        cmbVtype.setSelectedIndex(-1); // or 0 for a default selection
        txtVColor.setText("");
        txtSlot.setText("");
        dateTimePicker1.clear();
        dateTimePicker2.clear();
    }

       // ---------------- ParkingMaxSlots----------------
    private void loadSettingsFromDB() {
    try (PreparedStatement pst = con.prepareStatement(
            "SELECT setting_name, setting_value FROM settings")) {
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            String name = rs.getString("setting_name");
            int value = rs.getInt("setting_value");
            if ("maxCarSlots".equalsIgnoreCase(name)) {
                maxCarSlots = value;
            } else if ("maxMcSlots".equalsIgnoreCase(name)) {
                maxMcSlots = value;
            }
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Failed to load settings:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }
    
}
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        txtSname = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        txtFname = new javax.swing.JTextField();
        txtMname = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtID = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        txtAffiliation = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        txtContact = new javax.swing.JTextField();
        txtAddress = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtVBrand = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtPlate = new javax.swing.JTextField();
        cmbVtype = new javax.swing.JComboBox<>();
        jLabel20 = new javax.swing.JLabel();
        txtVColor = new javax.swing.JTextField();
        jPanel12 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtSlot = new javax.swing.JTextField();
        dateTimePicker2 = new com.github.lgooddatepicker.components.DateTimePicker();
        btnParkIn = new javax.swing.JButton();
        btnParkOut = new javax.swing.JButton();
        dateTimePicker1 = new com.github.lgooddatepicker.components.DateTimePicker();
        jPanel8 = new javax.swing.JPanel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jButton8 = new javax.swing.JButton();
        btnRegister = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnArchive = new javax.swing.JButton();
        btnReports = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        bntArchiveHistory = new javax.swing.JButton();
        btnUserManipulator = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        btnLogout = new javax.swing.JButton();
        panelbg = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        txtTotalMParked = new javax.swing.JTextField();
        txtMaxMcSlots = new javax.swing.JTextField();
        txtMAvailableSlot = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        txtTotalCarsParked = new javax.swing.JTextField();
        txtMaxCarSlots = new javax.swing.JTextField();
        txtCarsAvailableSlot = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        btnSaveSettings = new javax.swing.JButton();
        jLabel23 = new javax.swing.JLabel();
        txtCapacity = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setText("PHINMA COC PARKING SYSTEM");
        jLabel2.setBackground(new java.awt.Color(255, 255, 255));
        jLabel2.setFont(new java.awt.Font("Streetvertising", 0, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 28, -1, -1));

        jPanel1.setBackground(new java.awt.Color(153, 153, 153));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2), "Details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Streetvertising", 0, 14), new java.awt.Color(255, 255, 255))); // NOI18N
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel10.setBackground(new java.awt.Color(153, 153, 153));
        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(null, "Driver", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Streetvertising", 0, 12), new java.awt.Color(255, 255, 255)), "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Streetvertising", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        jPanel4.setBackground(new java.awt.Color(153, 153, 153));
        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        jLabel14.setText("Middle Name");
        jLabel14.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));

        jLabel13.setText("First Name");
        jLabel13.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));

        jLabel15.setText("Last Name");
        jLabel15.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));

        jLabel9.setText("ID Number");
        jLabel9.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));

        txtID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIDActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(jLabel13))
                        .addGap(24, 24, 24)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtID, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                            .addComponent(txtFname)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14)
                            .addComponent(jLabel15))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtSname, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtMname, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(txtFname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(txtMname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(txtSname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setBackground(new java.awt.Color(153, 153, 153));
        jPanel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        txtAffiliation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAffiliationActionPerformed(evt);
            }
        });

        jLabel4.setText("Affiliation");
        jLabel4.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));

        jLabel21.setText("Contact");
        jLabel21.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(255, 255, 255));

        txtContact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtContactActionPerformed(evt);
            }
        });

        txtAddress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAddressActionPerformed(evt);
            }
        });

        jLabel22.setText("Address");
        jLabel22.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap(10, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel21)
                    .addComponent(jLabel22))
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(txtAddress, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtContact, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtAffiliation))))
                .addGap(16, 16, 16))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtAffiliation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(txtContact, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel22)
                    .addComponent(txtAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 37, 500, -1));

        jPanel11.setBackground(new java.awt.Color(153, 153, 153));
        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(255, 255, 255)), " Vehicle", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Streetvertising", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        jLabel8.setText("Plate Number");
        jLabel8.setBackground(new java.awt.Color(255, 255, 255));
        jLabel8.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));

        jLabel5.setText("Vehicle Brand");
        jLabel5.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));

        jLabel6.setText("Vehicle Type");
        jLabel6.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));

        cmbVtype.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Car", "Motorcycle" }));
        cmbVtype.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbVtypeActionPerformed(evt);
            }
        });

        jLabel20.setText("Color");
        jLabel20.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel8)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(txtVBrand, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                    .addComponent(txtPlate, javax.swing.GroupLayout.Alignment.LEADING))
                .addGap(62, 62, 62)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(jLabel20)
                        .addGap(34, 34, 34)
                        .addComponent(txtVColor, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cmbVtype, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(32, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txtPlate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20)
                    .addComponent(txtVColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtVBrand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbVtype, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)))
        );

        jPanel1.add(jPanel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 270, 500, -1));

        jPanel12.setBackground(new java.awt.Color(153, 153, 153));
        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(255, 255, 204))); // NOI18N
        jPanel12.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel7.setText("Parking In");
        jLabel7.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jPanel12.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(13, 14, -1, -1));

        jPanel13.setBackground(new java.awt.Color(153, 153, 153));
        jPanel13.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel10.setText("Parking Out");
        jLabel10.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));

        jLabel11.setText("Parking Slot No.");
        jLabel11.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));

        txtSlot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSlotActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSlot, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(dateTimePicker2, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE))))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(dateTimePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(txtSlot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel12.add(jPanel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 50, -1, -1));

        btnParkIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/park.png"))); // NOI18N
        btnParkIn.setText("PARK IN");
        btnParkIn.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        btnParkIn.setForeground(new java.awt.Color(0, 153, 0));
        btnParkIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnParkInActionPerformed(evt);
            }
        });
        jPanel12.add(btnParkIn, new org.netbeans.lib.awtextra.AbsoluteConstraints(374, 7, 118, -1));

        btnParkOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/release.png"))); // NOI18N
        btnParkOut.setText("PARK OUT");
        btnParkOut.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        btnParkOut.setForeground(new java.awt.Color(255, 51, 0));
        btnParkOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnParkOutActionPerformed(evt);
            }
        });
        jPanel12.add(btnParkOut, new org.netbeans.lib.awtextra.AbsoluteConstraints(374, 50, 118, -1));
        jPanel12.add(dateTimePicker1, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 10, 280, -1));

        jPanel1.add(jPanel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 410, 500, 130));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 86, 523, 570));

        jPanel8.setBackground(new java.awt.Color(153, 153, 153));
        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2), "ENTER ID NUMBER", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Streetvertising", 0, 18), new java.awt.Color(255, 255, 255)), "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 18))); // NOI18N
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel8.add(txtSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(101, 32, 179, -1));

        btnSearch.setText("SEARCH");
        btnSearch.setBackground(new java.awt.Color(204, 204, 204));
        btnSearch.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });
        jPanel8.add(btnSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(298, 32, -1, -1));

        getContentPane().add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 16, 420, 70));

        jPanel2.setBackground(new java.awt.Color(153, 153, 153));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2), "Admin Operator", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Streetvertising", 0, 14), new java.awt.Color(255, 255, 255))); // NOI18N
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Actions-edit-clear-icon.png"))); // NOI18N
        jButton8.setText("CLEAR");
        jButton8.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 150, 130, 35));

        btnRegister.setIcon(new javax.swing.ImageIcon(getClass().getResource("/registration (1).png"))); // NOI18N
        btnRegister.setText("REGISTER");
        btnRegister.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        btnRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterActionPerformed(evt);
            }
        });
        jPanel2.add(btnRegister, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 130, -1));

        btnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Gear-icon (1).png"))); // NOI18N
        btnUpdate.setText("UPDATE");
        btnUpdate.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });
        jPanel2.add(btnUpdate, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 130, -1));

        btnArchive.setIcon(new javax.swing.ImageIcon(getClass().getResource("/archive.png"))); // NOI18N
        btnArchive.setText("ARCHIVES");
        btnArchive.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        btnArchive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnArchiveActionPerformed(evt);
            }
        });
        jPanel2.add(btnArchive, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, 130, -1));

        btnReports.setIcon(new javax.swing.ImageIcon(getClass().getResource("/print-icon.png"))); // NOI18N
        btnReports.setText("REPORTS");
        btnReports.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        btnReports.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReportsActionPerformed(evt);
            }
        });
        jPanel2.add(btnReports, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, 130, 35));

        btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/refresh (1).png"))); // NOI18N
        btnRefresh.setText("REFRESH");
        btnRefresh.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });
        jPanel2.add(btnRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, 130, -1));

        bntArchiveHistory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/archive (2).png"))); // NOI18N
        bntArchiveHistory.setText("ARCHIVE HISTORY");
        bntArchiveHistory.setFont(new java.awt.Font("Streetvertising", 0, 8)); // NOI18N
        bntArchiveHistory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bntArchiveHistoryActionPerformed(evt);
            }
        });
        jPanel2.add(bntArchiveHistory, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 270, 130, 35));

        btnUserManipulator.setIcon(new javax.swing.ImageIcon(getClass().getResource("/user(24).png"))); // NOI18N
        btnUserManipulator.setText("USER MODE");
        btnUserManipulator.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        btnUserManipulator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUserManipulatorActionPerformed(evt);
            }
        });
        jPanel2.add(btnUserManipulator, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 310, 130, 33));

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(549, 97, 150, 360));

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cocsmallerglow.png"))); // NOI18N
        getContentPane().add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(698, 16, -1, -1));

        btnLogout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/User-Interface-Logout-icon-1.png"))); // NOI18N
        btnLogout.setText("LOGOUT");
        btnLogout.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });
        getContentPane().add(btnLogout, new org.netbeans.lib.awtextra.AbsoluteConstraints(1248, 32, -1, -1));

        panelbg.setBackground(new java.awt.Color(153, 153, 153));
        panelbg.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel5.setBackground(new java.awt.Color(153, 153, 153));
        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2), "Parked Cars & Motorcycles", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Streetvertising", 0, 14), new java.awt.Color(255, 255, 255))); // NOI18N
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID Number", "Last Name", "Plate Number", "Park In", "Park Out"
            }
        ));
        jTable1.setGridColor(new java.awt.Color(0, 0, 0));
        jTable1.setSelectionBackground(new java.awt.Color(0, 0, 0));
        jScrollPane1.setViewportView(jTable1);

        jPanel5.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 28, 617, 229));

        jPanel3.setBackground(new java.awt.Color(153, 153, 153));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Parking Capacity", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Streetvertising", 0, 14), new java.awt.Color(255, 255, 255))); // NOI18N
        jPanel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jPanel15.setBackground(new java.awt.Color(153, 153, 153));
        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)), "Motorcycles", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Streetvertising", 0, 14), new java.awt.Color(255, 255, 255))); // NOI18N

        txtTotalMParked.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtTotalMParked.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTotalMParkedActionPerformed(evt);
            }
        });

        txtMaxMcSlots.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtMaxMcSlots.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMaxMcSlotsActionPerformed(evt);
            }
        });

        txtMAvailableSlot.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtMAvailableSlot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMAvailableSlotActionPerformed(evt);
            }
        });

        jLabel17.setText("Max ");
        jLabel17.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));

        jLabel18.setText("Parked");
        jLabel18.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(255, 255, 255));

        jLabel19.setText("Available");
        jLabel19.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel17)
                .addGap(12, 12, 12)
                .addComponent(txtMaxMcSlots, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(52, 52, 52)
                .addComponent(jLabel18)
                .addGap(18, 18, 18)
                .addComponent(txtTotalMParked, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(56, 56, 56)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtMAvailableSlot, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTotalMParked, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMaxMcSlots, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMAvailableSlot, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17)
                    .addComponent(jLabel18)
                    .addComponent(jLabel19))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel9.setBackground(new java.awt.Color(153, 153, 153));
        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)), "Cars ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Streetvertising", 0, 14), new java.awt.Color(255, 255, 255))); // NOI18N
        jPanel9.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N

        txtTotalCarsParked.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtTotalCarsParked.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTotalCarsParkedActionPerformed(evt);
            }
        });

        txtMaxCarSlots.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtMaxCarSlots.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMaxCarSlotsActionPerformed(evt);
            }
        });

        txtCarsAvailableSlot.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtCarsAvailableSlot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCarsAvailableSlotActionPerformed(evt);
            }
        });

        jLabel1.setText("Max ");
        jLabel1.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));

        jLabel3.setText("Parked");
        jLabel3.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));

        jLabel16.setText("Available");
        jLabel16.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(txtMaxCarSlots, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(txtTotalCarsParked, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtCarsAvailableSlot, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(82, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTotalCarsParked, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3)
                    .addComponent(txtMaxCarSlots, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCarsAvailableSlot, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnSaveSettings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/diskette.png"))); // NOI18N
        btnSaveSettings.setText("SAVE");
        btnSaveSettings.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        btnSaveSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveSettingsActionPerformed(evt);
            }
        });

        jLabel23.setText("Total Vehicle Parked");
        jLabel23.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(255, 255, 255));

        txtCapacity.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtCapacity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCapacityActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(118, 118, 118)
                        .addComponent(jLabel23)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtCapacity, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnSaveSettings)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSaveSettings)
                    .addComponent(txtCapacity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        jPanel5.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 269, 617, -1));

        panelbg.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 80, 641, 570));

        getContentPane().add(panelbg, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 1770, 820));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
       String idNumber = txtSearch.getText().trim();
    if (idNumber.isEmpty()) {
        JOptionPane.showMessageDialog(this,
                "Please enter an ID number to search.");
        return;
    }

    String sql =
    "SELECT d.id_number, d.first_name, d.middle_name, d.last_name, d.affiliation, " +
    "       d.contact, d.address, " +  // driver fields
    "       v.plate_number, v.vehicle_brand, v.vehicle_type, v.color, " + // vehicle fields
    "       t.slot_number, t.parking_in, t.parking_out " +
    "FROM driver d " +
    "JOIN vehicle v ON d.driver_id = v.driver_id " +
    "LEFT JOIN parkingtransaction t ON v.vehicle_id = t.vehicle_id " +
    "WHERE d.id_number = ? " +
    "ORDER BY t.parking_in DESC LIMIT 1";

    try (PreparedStatement pst = con.prepareStatement(sql)) {
        pst.setString(1, idNumber);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            // Person details
            txtID.setText(rs.getString("id_number"));
            txtFname.setText(rs.getString("first_name"));
            txtMname.setText(rs.getString("middle_name"));
            txtSname.setText(rs.getString("last_name"));
            txtAffiliation.setText(rs.getString("affiliation"));
            txtContact.setText(rs.getString("contact"));   // NEW
            txtAddress.setText(rs.getString("address"));   // NEW

            // Vehicle details
            txtPlate.setText(rs.getString("plate_number"));
            txtVBrand.setText(rs.getString("vehicle_brand"));
            cmbVtype.setSelectedItem(rs.getString("vehicle_type"));
            txtVColor.setText(rs.getString("color"));      // NEW

            // Parking transaction
            Timestamp inTime  = rs.getTimestamp("parking_in");
            Timestamp outTime = rs.getTimestamp("parking_out");

            if (outTime != null) {
                // Vehicle already parked out
                txtSlot.setText("");
                dateTimePicker1.clear();
                dateTimePicker2.clear();
            } else {
                txtSlot.setText(rs.getString("slot_number"));
                if (inTime != null) {
                    dateTimePicker1.setDateTimePermissive(inTime.toLocalDateTime());
                } else {
                    dateTimePicker1.clear();
                }
                dateTimePicker2.clear();
            }

        } else {
            JOptionPane.showMessageDialog(this,
                    "No record found for that ID number.");
        }

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Search failed:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        // Confirm logout
    int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to log out?",
            "Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
    );

    if (choice == JOptionPane.YES_OPTION) {
        try {
            // Close database connection if open
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }

        // Close current window
        this.dispose();

        // Go back to the login window
        java.awt.EventQueue.invokeLater(() -> {
            new Login().setVisible(true);
        });
    }


    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnReportsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReportsActionPerformed
        // T // TODO add your handling code here:

        ReportFrame RepForm = new ReportFrame();
        RepForm.setLocationRelativeTo(this);   // center relative to current window
        RepForm.setVisible(true);
    }//GEN-LAST:event_btnReportsActionPerformed

    private void btnArchiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnArchiveActionPerformed
    String idNumber = txtSearch.getText().trim();
if (idNumber.isEmpty()) {
JOptionPane.showMessageDialog(this,
"Please enter an ID number to archive.",
"Missing Information",
JOptionPane.WARNING_MESSAGE);
return;
}

int confirm = JOptionPane.showConfirmDialog(
    this,
    "Are you sure you want to move this record to archive?",
    "Confirm Archive",
    JOptionPane.YES_NO_OPTION
);

if (confirm != JOptionPane.YES_OPTION) return;

try {
    con.setAutoCommit(false);

    // ✅ Copy data to archive table
    String archiveSQL =
        "INSERT INTO archive (" +
        "driver_id, vehicle_id, transaction_id, slot_number, " +
        "id_number, first_name, middle_name, last_name, contact, address, " +
        "vehicle_type, plate_number, color, parking_in, parking_out, deleted_at" +
        ") " +
        "SELECT d.driver_id, v.vehicle_id, t.transaction_id, t.slot_number, " +
        "d.id_number, d.first_name, d.middle_name, d.last_name, d.contact, d.address, " +
        "v.vehicle_type, v.plate_number, v.color, t.parking_in, t.parking_out, NOW() " +
        "FROM driver d " +
        "LEFT JOIN vehicle v ON d.driver_id = v.driver_id " +
        "LEFT JOIN parkingtransaction t ON v.vehicle_id = t.vehicle_id " +
        "WHERE d.id_number = ?";

    try (PreparedStatement pst = con.prepareStatement(archiveSQL)) {
        pst.setString(1, idNumber);
        int rows = pst.executeUpdate();
        if (rows > 0) {
            JOptionPane.showMessageDialog(this,
                "✅ Record successfully moved to archive.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "⚠️ No matching record found for ID number: " + idNumber,
                "Not Found",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    con.commit();
    con.setAutoCommit(true);

    clearAllFields();

    // ✅ Clear jTable1 contents after archiving
    DefaultTableModel mainModel = (DefaultTableModel) jTable1.getModel();
    mainModel.setRowCount(0);

    

} catch (SQLException ex) {
    try { con.rollback(); } catch (SQLException ignore) {}
    JOptionPane.showMessageDialog(this,
        "❌ Failed to archive record:\n" + ex.getMessage(),
        "Database Error",
        JOptionPane.ERROR_MESSAGE);
    try { con.setAutoCommit(true); } catch (SQLException ignore) {}
}

    }//GEN-LAST:event_btnArchiveActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
    String idNumber = txtID.getText().trim();
    if (idNumber.isEmpty()) {
        JOptionPane.showMessageDialog(this,
            "Please load a record (Search) before updating.");
        return;
    }

    String firstName   = txtFname.getText().trim();
    String middleName  = txtMname.getText().trim();
    String surName     = txtSname.getText().trim();
    String affiliation = txtAffiliation.getText().trim();
    String contact     = txtContact.getText().trim();
    String address     = txtAddress.getText().trim();
    String plate       = txtPlate.getText().trim();
    String brand       = txtVBrand.getText().trim();
    String color       = txtVColor.getText().trim();
    String type        = cmbVtype.getSelectedItem() == null
        ? ""
        : cmbVtype.getSelectedItem().toString().trim();
    String slot        = txtSlot.getText().trim();
    Timestamp inTime   = dateTimePicker1.getDateTimePermissive() == null ? null
        : Timestamp.valueOf(dateTimePicker1.getDateTimePermissive());
    Timestamp outTime  = dateTimePicker2.getDateTimePermissive() == null ? null
        : Timestamp.valueOf(dateTimePicker2.getDateTimePermissive());

    if (firstName.isEmpty() || surName.isEmpty() || plate.isEmpty()) {
        JOptionPane.showMessageDialog(this,
            "First name, Surname, and Plate number cannot be empty.");
        return;
    }

    try {
        con.setAutoCommit(false);

        // 1️⃣ Update driver info (with contact and address)
        String sqlDriver =
            "UPDATE driver " +
            "SET first_name = ?, middle_name = ?, last_name = ?, " +
            "    affiliation = ?, contact = ?, address = ? " +
            "WHERE id_number = ?";
        try (PreparedStatement pst = con.prepareStatement(sqlDriver)) {
            pst.setString(1, firstName);
            pst.setString(2, middleName.isEmpty() ? null : middleName);
            pst.setString(3, surName);
            pst.setString(4, affiliation.isEmpty() ? null : affiliation);
            pst.setString(5, contact.isEmpty() ? null : contact);
            pst.setString(6, address.isEmpty() ? null : address);
            pst.setString(7, idNumber);
            pst.executeUpdate();
        }

        // 2️⃣ Update vehicle info (with color)
        String sqlVehicle =
            "UPDATE vehicle v " +
            "JOIN driver d ON v.driver_id = d.driver_id " +
            "SET v.plate_number = ?, v.vehicle_brand = ?, v.vehicle_type = ?, v.color = ? " +
            "WHERE d.id_number = ?";
        try (PreparedStatement pst = con.prepareStatement(sqlVehicle)) {
            pst.setString(1, plate);
            pst.setString(2, brand.isEmpty() ? null : brand);
            pst.setString(3, type.isEmpty() ? null : type);
            pst.setString(4, color.isEmpty() ? null : color);
            pst.setString(5, idNumber);
            pst.executeUpdate();
        }

        // 3️⃣ Update latest parking transaction
        String sqlTransaction =
            "UPDATE parkingtransaction t " +
            "JOIN vehicle v ON t.vehicle_id = v.vehicle_id " +
            "JOIN driver d ON v.driver_id = d.driver_id " +
            "SET t.slot_number = ?, t.parking_in = ?, t.parking_out = ? " +
            "WHERE d.id_number = ?";
        try (PreparedStatement pst = con.prepareStatement(sqlTransaction)) {
            pst.setString(1, slot.isEmpty() ? null : slot);
            if (inTime != null) pst.setTimestamp(2, inTime); else pst.setNull(2, java.sql.Types.TIMESTAMP);
            if (outTime != null) pst.setTimestamp(3, outTime); else pst.setNull(3, java.sql.Types.TIMESTAMP);
            pst.setString(4, idNumber);
            pst.executeUpdate();
        }

        con.commit();
        con.setAutoCommit(true);

        JOptionPane.showMessageDialog(this,
            "✅ Record updated successfully.",
            "Success",
            JOptionPane.INFORMATION_MESSAGE);

    } catch (SQLException ex) {
        try { con.rollback(); } catch (SQLException ignore) {}
        JOptionPane.showMessageDialog(this,
            "❌ Update failed:\n" + ex.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE);
        try { con.setAutoCommit(true); } catch (SQLException ignore) {}
    }

    loadTableData();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterActionPerformed
        // === 1. Collect and validate input ===
    String idNumber   = txtID.getText().trim();
    String firstName  = txtFname.getText().trim();
    String middleName = txtMname.getText().trim();
    String surName    = txtSname.getText().trim();
    String affiliation= txtAffiliation.getText().trim();
    String contact    = txtContact.getText().trim();
    String address    = txtAddress.getText().trim();
    String plate      = txtPlate.getText().trim();
    String brand      = txtVBrand.getText().trim();
    String color      = txtVColor.getText().trim();
    String type       = (cmbVtype.getSelectedItem() == null)
            ? ""
            : cmbVtype.getSelectedItem().toString().trim();

    // Basic validation
    if (idNumber.isEmpty() || firstName.isEmpty() || surName.isEmpty() ||
        plate.isEmpty() || middleName.isEmpty() || address.isEmpty() ||
        contact.isEmpty() || color.isEmpty()) {

        JOptionPane.showMessageDialog(this,
                "⚠️ Some required fields are missing. Please complete them before registering.");
        return;
    }

    try {
        con.setAutoCommit(false); // Start transaction

        // === 2️⃣ Check if ID already exists ===
        try (PreparedStatement check =
                     con.prepareStatement("SELECT 1 FROM driver WHERE id_number = ?")) {
            check.setString(1, idNumber);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this,
                            "This ID number is already registered.");
                    con.setAutoCommit(true);
                    return;
                }
            }
        }

        // === 3️⃣ Insert into driver table ===
        long driverId;
        String sqlDriver =
                "INSERT INTO driver (id_number, first_name, middle_name, last_name, affiliation, contact, address) " +
                "VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement pst = con.prepareStatement(sqlDriver,
                Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, idNumber);
            pst.setString(2, firstName);
            pst.setString(3, middleName.isEmpty() ? null : middleName);
            pst.setString(4, surName);
            pst.setString(5, affiliation.isEmpty() ? null : affiliation);
            pst.setString(6, contact.isEmpty() ? null : contact);
            pst.setString(7, address.isEmpty() ? null : address);
            pst.executeUpdate();

            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No driver_id returned.");
                driverId = keys.getLong(1);
            }
        }

        // === 4️⃣ Insert into vehicle (with color) ===
        String sqlVehicle =
                "INSERT INTO vehicle (driver_id, plate_number, vehicle_brand, vehicle_type, color) " +
                "VALUES (?,?,?,?,?)";
        try (PreparedStatement pst = con.prepareStatement(sqlVehicle)) {
            pst.setLong(1, driverId);
            pst.setString(2, plate);
            pst.setString(3, brand.isEmpty() ? null : brand);
            pst.setString(4, type.isEmpty() ? null : type);
            pst.setString(5, color.isEmpty() ? null : color);
            pst.executeUpdate();
        }

        // ✅ (Optional) No parkingtransaction insert here yet

        // === 5️⃣ Commit and finish ===
        con.commit();
        con.setAutoCommit(true);

        JOptionPane.showMessageDialog(this, "✅ Registration successful!");
        clearAllFields();
        loadTableData();

    } catch (SQLException ex) {
        try { con.rollback(); } catch (SQLException ignore) {}
        JOptionPane.showMessageDialog(this,
                "❌ Registration failed:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        try { con.setAutoCommit(true); } catch (SQLException ignore) {}
    }
    }//GEN-LAST:event_btnRegisterActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
    txtSearch.setText("");
    txtID.setText("");
    txtFname.setText("");
    txtMname.setText("");
    txtSname.setText("");
    txtAffiliation.setText("");
    txtContact.setText("");    
    txtAddress.setText("");    
    txtPlate.setText("");
    txtVBrand.setText("");
    txtVColor.setText("");     
    cmbVtype.setSelectedIndex(-1);
    txtSlot.setText("");
    dateTimePicker1.clear();
    dateTimePicker2.clear();
    }//GEN-LAST:event_jButton8ActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
        loadSettingsFromDB();
        txtMaxCarSlots.setText(String.valueOf(maxCarSlots));
        txtMaxMcSlots.setText(String.valueOf(maxMcSlots));
        
          updateParkingStatus();
    }//GEN-LAST:event_formWindowOpened

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        String sql =
        "SELECT v.vehicle_type, COUNT(*) AS cnt " +
        "FROM parkingtransaction pt " +
        "JOIN vehicle v ON pt.vehicle_id = v.vehicle_id " +
        "WHERE pt.parking_out IS NULL " +  // only currently parked
        "AND v.vehicle_type IN ('Car', 'Motorcycle') " +
        "GROUP BY v.vehicle_type";

    int totalCars = 0;
    int totalMotorcycles = 0;

    try (PreparedStatement pst = con.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        while (rs.next()) {
            String type = rs.getString("vehicle_type");
            int count = rs.getInt("cnt");

            if ("Car".equalsIgnoreCase(type)) {
                totalCars = count;
            } else if ("Motorcycle".equalsIgnoreCase(type)) {
                totalMotorcycles = count;
            }
        }

        // ✅ Update UI fields
        txtTotalCarsParked.setText(String.valueOf(totalCars));
        txtTotalMParked.setText(String.valueOf(totalMotorcycles));
        txtCapacity.setText(String.valueOf(totalCars + totalMotorcycles));

        // Optional feedback
        System.out.println("Refreshed parking stats: " +
                totalCars + " cars, " + totalMotorcycles + " motorcycles.");

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "❌ Failed to count parked vehicles:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }

    // ✅ Refresh other data
    updateParkingStatus();
    loadTableData();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void cmbVtypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbVtypeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbVtypeActionPerformed

    private void txtAddressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAddressActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAddressActionPerformed

    private void txtContactActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtContactActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtContactActionPerformed

    private void txtAffiliationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAffiliationActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAffiliationActionPerformed

    private void txtIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIDActionPerformed

    private void btnSaveSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveSettingsActionPerformed
        try {
        // ✅ Parse input
        int newCarSlots = Integer.parseInt(txtMaxCarSlots.getText().trim());
        int newMcSlots  = Integer.parseInt(txtMaxMcSlots.getText().trim());

        // ✅ Basic validation
        if (newCarSlots < 0 || newMcSlots < 0) {
            JOptionPane.showMessageDialog(this,
                "Slot values cannot be negative.",
                "Invalid Input",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ✅ Update database in one transaction
        con.setAutoCommit(false);

        try (PreparedStatement pst1 = con.prepareStatement(
                 "UPDATE settings SET setting_value = ? WHERE setting_name = 'maxCarSlots'");
             PreparedStatement pst2 = con.prepareStatement(
                 "UPDATE settings SET setting_value = ? WHERE setting_name = 'maxMcSlots'")) {

            pst1.setInt(1, newCarSlots);
            pst1.executeUpdate();

            pst2.setInt(1, newMcSlots);
            pst2.executeUpdate();

            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            throw ex; // rethrow for catch below
        } finally {
            con.setAutoCommit(true);
        }

        // ✅ Update local memory variables
        Parking.maxCarSlots = newCarSlots;
        Parking.maxMcSlots  = newMcSlots;

        JOptionPane.showMessageDialog(this, 
            "✅ Settings updated successfully!",
            "Success",
            JOptionPane.INFORMATION_MESSAGE);

    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this,
            "⚠️ Please enter valid numbers for slot capacity.",
            "Invalid Input",
            JOptionPane.WARNING_MESSAGE);
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
            "❌ Failed to save settings:\n" + ex.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_btnSaveSettingsActionPerformed

    private void txtMAvailableSlotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMAvailableSlotActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMAvailableSlotActionPerformed

    private void txtMaxMcSlotsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMaxMcSlotsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMaxMcSlotsActionPerformed

    private void txtTotalMParkedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTotalMParkedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTotalMParkedActionPerformed

    private void txtCarsAvailableSlotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCarsAvailableSlotActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCarsAvailableSlotActionPerformed

    private void txtMaxCarSlotsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMaxCarSlotsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMaxCarSlotsActionPerformed

    private void txtTotalCarsParkedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTotalCarsParkedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTotalCarsParkedActionPerformed

    private void bntArchiveHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bntArchiveHistoryActionPerformed
        // TODO add your handling code here:
        Archive Archive = new Archive();
        Archive.setLocationRelativeTo(this);   // center relative to current window
        Archive.setVisible(true);

    }//GEN-LAST:event_bntArchiveHistoryActionPerformed

    private void btnParkOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnParkOutActionPerformed
      String idNumber = txtID.getText().trim();
    if (idNumber.isEmpty()) {
        JOptionPane.showMessageDialog(this,
                "Please enter or search for an ID number before parking out.");
        return;
    }

    LocalDateTime pickedOut = dateTimePicker2.getDateTimePermissive();
    if (pickedOut == null) {
        JOptionPane.showMessageDialog(this,
                "Please select a Parking-Out date and time before checking out.");
        return;
    }

    Timestamp parkingOut = Timestamp.valueOf(pickedOut);

    try {
        con.setAutoCommit(false);

        // === STEP 1: Find vehicle_id using driver table ===
        int vehicleId = -1;
        String sqlVehicle = """
            SELECT v.vehicle_id
            FROM vehicle v
            JOIN driver d ON v.driver_id = d.driver_id
            WHERE d.id_number = ?
        """;
        try (PreparedStatement pst = con.prepareStatement(sqlVehicle)) {
            pst.setString(1, idNumber);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                vehicleId = rs.getInt("vehicle_id");
            } else {
                JOptionPane.showMessageDialog(this, "No vehicle found for this ID number.");
                con.setAutoCommit(true);
                return;
            }
        }

        // === STEP 2: Get the active parking_in record ===
        Timestamp parkingIn = null;
        String sqlGetIn = """
            SELECT parking_in
            FROM parkingtransaction
            WHERE vehicle_id = ? AND parking_out IS NULL
        """;
        try (PreparedStatement pst = con.prepareStatement(sqlGetIn)) {
            pst.setInt(1, vehicleId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                parkingIn = rs.getTimestamp("parking_in");
            } else {
                JOptionPane.showMessageDialog(this,
                        "This vehicle has no active parking record.");
                con.setAutoCommit(true);
                return;
            }
        }

        // === STEP 3: Validate date ===
        if (parkingOut.before(parkingIn)) {
            JOptionPane.showMessageDialog(this,
                    "Parking-Out time cannot be earlier than Parking-In time!");
            con.setAutoCommit(true);
            return;
        }

        // === STEP 4: Update transaction ===
        String sqlUpdate = """
            UPDATE parkingtransaction
            SET parking_out = ?
            WHERE vehicle_id = ? AND parking_out IS NULL
        """;
        try (PreparedStatement pst = con.prepareStatement(sqlUpdate)) {
            pst.setTimestamp(1, parkingOut);
            pst.setInt(2, vehicleId);
            int updated = pst.executeUpdate();

            if (updated == 0) {
                JOptionPane.showMessageDialog(this,
                        "This vehicle has no active parking record (already checked out).");
                con.setAutoCommit(true);
                return;
            }
        }

        con.commit();
        con.setAutoCommit(true);

        JOptionPane.showMessageDialog(this, "✅ Vehicle checked out successfully!");
        clearAllFields();
        loadTableData();
        updateTotalParked();
        updateParkingStatus();

    } catch (SQLException ex) {
        try { con.rollback(); } catch (SQLException ignore) {}
        JOptionPane.showMessageDialog(this,
                "❌ Park out failed:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        try { con.setAutoCommit(true); } catch (SQLException ignore) {}
    }  
    }//GEN-LAST:event_btnParkOutActionPerformed

    private void btnParkInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnParkInActionPerformed
    String idNumber = txtID.getText().trim();
    if (idNumber.isEmpty()) {
        JOptionPane.showMessageDialog(this,
                "Please enter or search for an ID number before parking in.");
        return;
    }

    // ✅ Require parking date/time
    LocalDateTime pickedIn = dateTimePicker1.getDateTimePermissive();
    if (pickedIn == null) {
        JOptionPane.showMessageDialog(this,
                "Please select a Parking-In date and time.");
        return;
    }

    // ✅ Must be today only (no past or future)
    LocalDate today = LocalDate.now();
    if (!pickedIn.toLocalDate().isEqual(today)) {
        JOptionPane.showMessageDialog(this,
                "You can only park for the current date (" + today + ").");
        return;
    }

    // Open map
    Map map = new Map();
    map.setVisible(true);

    // When Map closes, get selected slot
    map.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosed(java.awt.event.WindowEvent e) {
            String slot = map.getSelectedSlot();
            if (slot == null) {
                JOptionPane.showMessageDialog(null, "No slot selected. Parking canceled.");
                return;
            }

            txtSlot.setText(slot);

            try {
                con.setAutoCommit(false);

                // === STEP 1: Get vehicle_id ===
                final int vehicleId;
                final String vehicleType;

                String sqlVehicle = """
                    SELECT v.vehicle_id, v.vehicle_type
                    FROM vehicle v
                    JOIN driver d ON v.driver_id = d.driver_id
                    WHERE d.id_number = ?
                """;

                try (PreparedStatement pst = con.prepareStatement(sqlVehicle)) {
                    pst.setString(1, idNumber);
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        vehicleId = rs.getInt("vehicle_id");
                        vehicleType = rs.getString("vehicle_type");
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "No vehicle found for this ID number.");
                        con.setAutoCommit(true);
                        return;
                    }
                }

                // === STEP 2: Check if vehicle already parked in ===
                String sqlCheckExisting = """
                    SELECT slot_number, parking_in
                    FROM parkingtransaction
                    WHERE vehicle_id = ? AND parking_out IS NULL
                """;
                try (PreparedStatement pst = con.prepareStatement(sqlCheckExisting)) {
                    pst.setInt(1, vehicleId);
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        Timestamp existingIn = rs.getTimestamp("parking_in");
                        String existingSlot = rs.getString("slot_number");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
                        JOptionPane.showMessageDialog(null,
                                "This vehicle is already parked!\n" +
                                "Slot: " + existingSlot + "\n" +
                                "Parking-In: " + sdf.format(existingIn),
                                "Already Parked", JOptionPane.WARNING_MESSAGE);
                        con.setAutoCommit(true);
                        return;
                    }
                }

                // === STEP 3: Check if slot is occupied ===
                String sqlCheckSlot = """
                    SELECT COUNT(*) FROM parkingtransaction
                    WHERE slot_number = ? AND parking_out IS NULL
                """;
                try (PreparedStatement pst = con.prepareStatement(sqlCheckSlot)) {
                    pst.setString(1, slot);
                    ResultSet rs = pst.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(null,
                                "This slot is already occupied. Please choose another slot.");
                        con.setAutoCommit(true);
                        return;
                    }
                }

                // === STEP 4: Check if slot type matches vehicle type ===
                if ((vehicleType.equalsIgnoreCase("Car") && slot.startsWith("M")) ||
                    (vehicleType.equalsIgnoreCase("Motorcycle") && slot.startsWith("C"))) {
                    JOptionPane.showMessageDialog(null,
                            "This slot is not suitable for your vehicle type.\n" +
                            "Cars can only park in 'C' slots and motorcycles in 'M' slots.");
                    con.setAutoCommit(true);
                    return;
                }

                // === STEP 5: Check if parking area is full ===
                int maxCars = Integer.parseInt(txtMaxCarSlots.getText().trim());
                int maxMcs  = Integer.parseInt(txtMaxMcSlots.getText().trim());
                int currentCars = Integer.parseInt(txtTotalCarsParked.getText().trim());
                int currentMcs  = Integer.parseInt(txtTotalMParked.getText().trim());

                if (vehicleType.equalsIgnoreCase("Car") && currentCars >= maxCars) {
                    JOptionPane.showMessageDialog(null,
                            "🚫 Sorry, Car parking area is FULL!",
                            "Parking Full", JOptionPane.ERROR_MESSAGE);
                    con.setAutoCommit(true);
                    return;
                }
                if (vehicleType.equalsIgnoreCase("Motorcycle") && currentMcs >= maxMcs) {
                    JOptionPane.showMessageDialog(null,
                            "🚫 Sorry, Motorcycle parking area is FULL!",
                            "Parking Full", JOptionPane.ERROR_MESSAGE);
                    con.setAutoCommit(true);
                    return;
                }

                // === STEP 6: Insert into database ===
                Timestamp parkingIn = Timestamp.valueOf(pickedIn);
                String sqlInsert = """
                    INSERT INTO parkingtransaction (vehicle_id, slot_number, parking_in)
                    VALUES (?, ?, ?)
                """;
                try (PreparedStatement pst = con.prepareStatement(sqlInsert)) {
                    pst.setInt(1, vehicleId);
                    pst.setString(2, slot);
                    pst.setTimestamp(3, parkingIn);
                    pst.executeUpdate();
                }

                con.commit();
                con.setAutoCommit(true);

                JOptionPane.showMessageDialog(null, "✅ Vehicle parked successfully at slot " + slot + "!");
                clearAllFields();
                loadTableData();
                updateTotalParked();
                updateParkingStatus();

            } catch (SQLException ex) {
                try { con.rollback(); } catch (SQLException ignore) {}
                JOptionPane.showMessageDialog(null, "Error saving parking: " + ex.getMessage());
                try { con.setAutoCommit(true); } catch (SQLException ignore) {}
            }
        }
    });
    }//GEN-LAST:event_btnParkInActionPerformed

    private void txtSlotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSlotActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSlotActionPerformed

    private void btnUserManipulatorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUserManipulatorActionPerformed
        // TODO add your handling code here:
     Parking1 Parking1= new Parking1();
        Parking1.setLocationRelativeTo(this);   // center relative to current window
       Parking1.setVisible(true);
    }//GEN-LAST:event_btnUserManipulatorActionPerformed

    private void txtCapacityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCapacityActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCapacityActionPerformed

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
            java.util.logging.Logger.getLogger(Parking.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Parking.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Parking.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Parking.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Parking().setVisible(true);
            }
        });
    }
    
    // === Add this method anywhere inside the class but outside other methods ===
private void updateTotalParked() {
    String sql = """
        SELECT v.vehicle_type, COUNT(*) AS cnt
        FROM parkingtransaction pt
        JOIN vehicle v ON pt.vehicle_id = v.vehicle_id
        JOIN driver d ON v.driver_id = d.driver_id
        WHERE pt.parking_out IS NULL
        AND v.vehicle_type IN ('Car', 'Motorcycle')
        GROUP BY v.vehicle_type
    """;

    int totalCars = 0;
    int totalMotorcycles = 0;

    try (PreparedStatement pst = con.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        while (rs.next()) {
            String type = rs.getString("vehicle_type");
            int total = rs.getInt("cnt");

            if ("Car".equalsIgnoreCase(type)) {
                totalCars = total;
            } else if ("Motorcycle".equalsIgnoreCase(type)) {
                totalMotorcycles = total;
            }
        }

        // ✅ Update UI
        txtTotalCarsParked.setText(String.valueOf(totalCars));
        txtTotalMParked.setText(String.valueOf(totalMotorcycles));

        // ✅ Total vehicles currently parked
        int totalVehicles = totalCars + totalMotorcycles;
        txtCapacity.setText(String.valueOf(totalVehicles));

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Failed to count parked vehicles:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }

}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bntArchiveHistory;
    private javax.swing.JButton btnArchive;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnParkIn;
    private javax.swing.JButton btnParkOut;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnRegister;
    private javax.swing.JButton btnReports;
    private javax.swing.JButton btnSaveSettings;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JButton btnUserManipulator;
    private javax.swing.JComboBox<String> cmbVtype;
    private com.github.lgooddatepicker.components.DateTimePicker dateTimePicker1;
    private com.github.lgooddatepicker.components.DateTimePicker dateTimePicker2;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel panelbg;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JTextField txtAffiliation;
    private javax.swing.JTextField txtCapacity;
    private javax.swing.JTextField txtCarsAvailableSlot;
    private javax.swing.JTextField txtContact;
    private javax.swing.JTextField txtFname;
    private javax.swing.JTextField txtID;
    private javax.swing.JTextField txtMAvailableSlot;
    private javax.swing.JTextField txtMaxCarSlots;
    private javax.swing.JTextField txtMaxMcSlots;
    private javax.swing.JTextField txtMname;
    private javax.swing.JTextField txtPlate;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtSlot;
    private javax.swing.JTextField txtSname;
    private javax.swing.JTextField txtTotalCarsParked;
    private javax.swing.JTextField txtTotalMParked;
    private javax.swing.JTextField txtVBrand;
    private javax.swing.JTextField txtVColor;
    // End of variables declaration//GEN-END:variables
  }
