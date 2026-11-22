
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//---------------------ug ako nalang diay pls, try lang nato ;/---------------------------------

public class Map extends javax.swing.JFrame {


   private String selectedSlot = null;
   

    public Map() {
        initComponents();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setDefaultSlotColors();
        loadOccupiedSlotsFromDB();
        addSlotClickEvents();
    }

    private void setDefaultSlotColors() {
        JButton[] buttons = { 
            btnCar1, btnCar2, btnCar3,btnCar4,btnCar5,btnCar6,btnCar7,btnCar8,btnCar9,btnCar10,
            btnCar11,btnCar12,btnCar13,btnCar14,btnCar15,btnCar16,btnCar17,btnCar18,btnCar19,btnCar20,
            btnCar21,btnCar22,btnCar23,btnCar24,btnCar25,btnCar26,btnCar27,btnCar28,btnCar29,btnCar30,
            btnCar31,btnCar32,
            
            
            
            btnMc1,btnMc2,btnMc3,btnMc4,btnMc5,btnMc6,btnMc7,btnMc8,btnMc9,btnMc10,
            btnMc11,btnMc12,btnMc13,btnMc14,btnMc15,btnMc16,btnMc17,btnMc18,btnMc19,btnMc20,
            btnMc21,btnMc22,btnMc23, btnMc24,btnMc25,
            
           
        };
        for (JButton b : buttons) {
            b.setBackground(Color.GRAY);
            b.setOpaque(true);
            b.setBorderPainted(false);
        }
    }

    private void addSlotClickEvents() {
        JButton[] buttons = { 
            btnCar1, btnCar2, btnCar3,btnCar4,btnCar5,btnCar6,btnCar7,btnCar8,btnCar9,btnCar10,
            btnCar11,btnCar12,btnCar13,btnCar14,btnCar15,btnCar16,btnCar17,btnCar18,btnCar19,btnCar20,
            btnCar21,btnCar22,btnCar23,btnCar24,btnCar25,btnCar26,btnCar27,btnCar28,btnCar29,btnCar30,
            btnCar31,btnCar32,
            
            
            btnMc1,btnMc2,btnMc3,btnMc4,btnMc5,btnMc6,btnMc7,btnMc8,btnMc9,btnMc10,
            btnMc11,btnMc12,btnMc13,btnMc14,btnMc15,btnMc16,btnMc17,btnMc18,btnMc19,btnMc20,
            btnMc21,btnMc22,btnMc23,btnMc24,btnMc25 
          
        };

        for (JButton btn : buttons) {
            btn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Do you want to take this slot (" + btn.getName() + ")?",
                        "Confirm Slot",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    selectedSlot = btn.getName();
                    btn.setBackground(Color.RED);
                    this.dispose(); // close Map window
                }
            });
        }
    }

    public String getSelectedSlot() {
        return selectedSlot;
    }
    
    public void refreshSlots(Connection con) {
    try (PreparedStatement pst = con.prepareStatement(
            "SELECT slot_number FROM parkingtransaction WHERE parking_out IS NULL")) {
        ResultSet rs = pst.executeQuery();

        // Reset all buttons to GREEN first
        JButton[] buttons = { 
            btnCar1, btnCar2, btnCar3,btnCar4,btnCar5,btnCar6,btnCar7,btnCar8,btnCar9,btnCar10,
            btnCar11,btnCar12,btnCar13,btnCar14,btnCar15,btnCar16,btnCar17,btnCar18,btnCar19,btnCar20,
            btnCar21,btnCar22,btnCar23,btnCar24,btnCar25,btnCar26,btnCar27,btnCar28,btnCar29,btnCar30,
            btnCar31,btnCar32,
            
            btnMc1,btnMc2,btnMc3,btnMc4,btnMc5,btnMc6,btnMc7,btnMc8,btnMc9,btnMc10,
            btnMc11,btnMc12,btnMc13,btnMc14,btnMc15,btnMc16,btnMc17,btnMc18,btnMc19,btnMc20,
            btnMc21,btnMc22,btnMc23,btnMc24,btnMc25
        };
        for (JButton b : buttons) {
            b.setBackground(Color.GREEN);
        }

        // Set occupied slots to RED
        while (rs.next()) {
            String slotNum = rs.getString("slot_number");
            for (JButton b : buttons) {
                if (b.getName().equalsIgnoreCase(slotNum)) {
                    b.setBackground(Color.RED);
                }
            }
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error refreshing slots: " + ex.getMessage());
    }
}

    // Load occupied slots (turn them red)
    private void loadOccupiedSlotsFromDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/parkingdb", "root", "");
            String sql = "SELECT slot_number FROM parkingtransaction WHERE parking_out IS NULL";
            try (PreparedStatement pst = con.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {

                while (rs.next()) {
                    String slot = rs.getString("slot_number");
                    switch (slot) {
                        case "Car1": btnCar1.setBackground(Color.RED); break;
                        case "Car2": btnCar2.setBackground(Color.RED); break;
                        case "Car3": btnCar3.setBackground(Color.RED); break;
                        case "Car4": btnCar4.setBackground(Color.RED); break;
                        case "Car5": btnCar5.setBackground(Color.RED); break;
                        case "Car6": btnCar6.setBackground(Color.RED); break;
                        case "Car7": btnCar7.setBackground(Color.RED); break;
                        case "Car8": btnCar8.setBackground(Color.RED); break;
                        case "Car9": btnCar9.setBackground(Color.RED); break;
                        case "Car10": btnCar10.setBackground(Color.RED); break;
                        case "Car11": btnCar11.setBackground(Color.RED); break;
                        case "Car12": btnCar12.setBackground(Color.RED); break;
                        case "Car13": btnCar13.setBackground(Color.RED); break;
                        case "Car14": btnCar14.setBackground(Color.RED); break;
                        case "Car15": btnCar15.setBackground(Color.RED); break;
                        case "Car16": btnCar16.setBackground(Color.RED); break;
                        case "Car17": btnCar17.setBackground(Color.RED); break;
                        case "Car18": btnCar18.setBackground(Color.RED); break;
                        case "Car19": btnCar19.setBackground(Color.RED); break;
                        case "Car20": btnCar20.setBackground(Color.RED); break;
                        case "Car21": btnCar21.setBackground(Color.RED); break;
                        case "Car22": btnCar22.setBackground(Color.RED); break;
                        case "Car23": btnCar23.setBackground(Color.RED); break;
                        case "Car24": btnCar24.setBackground(Color.RED); break;
                        case "Car25": btnCar25.setBackground(Color.RED); break;
                        case "Car26": btnCar26.setBackground(Color.RED); break;
                        case "Car27": btnCar27.setBackground(Color.RED); break;
                        case "Car28": btnCar28.setBackground(Color.RED); break;
                        case "Car29": btnCar29.setBackground(Color.RED); break;
                        case "Car30": btnCar30.setBackground(Color.RED); break;
                        case "Car31": btnCar31.setBackground(Color.RED); break;
                        case "Car32": btnCar32.setBackground(Color.RED); break;
                        
                        
                        case "Mc1": btnMc1.setBackground(Color.RED); break;
                        case "Mc2": btnMc2.setBackground(Color.RED); break;
                        case "Mc3": btnMc3.setBackground(Color.RED); break;
                        case "Mc4": btnMc4.setBackground(Color.RED); break;
                        case "Mc5": btnMc5.setBackground(Color.RED); break;
                        case "Mc6": btnMc6.setBackground(Color.RED); break;
                        case "Mc7": btnMc7.setBackground(Color.RED); break;
                        case "Mc8": btnMc8.setBackground(Color.RED); break;
                        case "Mc9": btnMc9.setBackground(Color.RED); break;
                        case "Mc10": btnMc10.setBackground(Color.RED); break;
                        case "Mc11": btnMc11.setBackground(Color.RED); break;
                        case "Mc12": btnMc12.setBackground(Color.RED); break;
                        case "Mc13": btnMc13.setBackground(Color.RED); break;
                        case "Mc14": btnMc14.setBackground(Color.RED); break;
                        case "Mc15": btnMc15.setBackground(Color.RED); break;
                        case "Mc16": btnMc16.setBackground(Color.RED); break;
                        case "Mc17": btnMc17.setBackground(Color.RED); break;
                        case "Mc18": btnMc18.setBackground(Color.RED); break;
                        case "Mc19": btnMc19.setBackground(Color.RED); break;
                        case "Mc20": btnMc20.setBackground(Color.RED); break;
                        case "Mc21": btnMc21.setBackground(Color.RED); break;
                        case "Mc22": btnMc22.setBackground(Color.RED); break;
                        case "Mc23": btnMc23.setBackground(Color.RED); break;
                        case "Mc24": btnMc24.setBackground(Color.RED); break;
                        case "Mc25": btnMc25.setBackground(Color.RED); break;
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading occupied slots:\n" + ex.getMessage());
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

        btnCar1 = new javax.swing.JButton();
        btnCar2 = new javax.swing.JButton();
        btnCar3 = new javax.swing.JButton();
        btnCar4 = new javax.swing.JButton();
        btnCar5 = new javax.swing.JButton();
        btnCar6 = new javax.swing.JButton();
        btnCar7 = new javax.swing.JButton();
        btnCar8 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        btnCar13 = new javax.swing.JButton();
        btnCar14 = new javax.swing.JButton();
        btnCar10 = new javax.swing.JButton();
        btnCar15 = new javax.swing.JButton();
        btnCar11 = new javax.swing.JButton();
        btnCar18 = new javax.swing.JButton();
        btnCar16 = new javax.swing.JButton();
        btnCar20 = new javax.swing.JButton();
        btnCar19 = new javax.swing.JButton();
        btnCar21 = new javax.swing.JButton();
        btnCar22 = new javax.swing.JButton();
        btnCar24 = new javax.swing.JButton();
        btnCar23 = new javax.swing.JButton();
        btnCar17 = new javax.swing.JButton();
        btnCar26 = new javax.swing.JButton();
        btnCar29 = new javax.swing.JButton();
        btnCar25 = new javax.swing.JButton();
        btnCar28 = new javax.swing.JButton();
        btnCar27 = new javax.swing.JButton();
        btnCar30 = new javax.swing.JButton();
        btnMc5 = new javax.swing.JButton();
        btnCar32 = new javax.swing.JButton();
        btnMc6 = new javax.swing.JButton();
        btnMc4 = new javax.swing.JButton();
        btnCar31 = new javax.swing.JButton();
        btnCar12 = new javax.swing.JButton();
        btnCar9 = new javax.swing.JButton();
        btnMc25 = new javax.swing.JButton();
        btnMc3 = new javax.swing.JButton();
        btnMc10 = new javax.swing.JButton();
        btnMc9 = new javax.swing.JButton();
        btnMc12 = new javax.swing.JButton();
        btnMc13 = new javax.swing.JButton();
        btnMc14 = new javax.swing.JButton();
        btnMc8 = new javax.swing.JButton();
        btnMc16 = new javax.swing.JButton();
        btnMc18 = new javax.swing.JButton();
        btnMc17 = new javax.swing.JButton();
        btnMc11 = new javax.swing.JButton();
        btnMc19 = new javax.swing.JButton();
        btnMc23 = new javax.swing.JButton();
        btnMc22 = new javax.swing.JButton();
        btnMc15 = new javax.swing.JButton();
        btnMc21 = new javax.swing.JButton();
        btnMc20 = new javax.swing.JButton();
        btnMc24 = new javax.swing.JButton();
        btnMc7 = new javax.swing.JButton();
        btnMc2 = new javax.swing.JButton();
        btnMc1 = new javax.swing.JButton();
        parkinglotBG = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        btnBack = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnCar1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80).png"))); // NOI18N
        btnCar1.setBackground(new java.awt.Color(102, 102, 102));
        btnCar1.setForeground(new java.awt.Color(102, 102, 102));
        btnCar1.setName("Car1"); // NOI18N
        getContentPane().add(btnCar1, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 110, 40, 90));

        btnCar2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80).png"))); // NOI18N
        btnCar2.setBackground(new java.awt.Color(102, 102, 102));
        btnCar2.setForeground(new java.awt.Color(102, 102, 102));
        btnCar2.setName("Car2"); // NOI18N
        getContentPane().add(btnCar2, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 110, 40, 90));

        btnCar3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80).png"))); // NOI18N
        btnCar3.setBackground(new java.awt.Color(102, 102, 102));
        btnCar3.setForeground(new java.awt.Color(102, 102, 102));
        btnCar3.setName("Car3"); // NOI18N
        btnCar3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCar3ActionPerformed(evt);
            }
        });
        getContentPane().add(btnCar3, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 110, 40, 90));

        btnCar4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80).png"))); // NOI18N
        btnCar4.setBackground(new java.awt.Color(102, 102, 102));
        btnCar4.setForeground(new java.awt.Color(102, 102, 102));
        btnCar4.setName("Car4"); // NOI18N
        getContentPane().add(btnCar4, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 110, 40, 90));

        btnCar5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80).png"))); // NOI18N
        btnCar5.setBackground(new java.awt.Color(102, 102, 102));
        btnCar5.setForeground(new java.awt.Color(102, 102, 102));
        btnCar5.setName("Car5"); // NOI18N
        getContentPane().add(btnCar5, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 110, 40, 90));

        btnCar6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80).png"))); // NOI18N
        btnCar6.setBackground(new java.awt.Color(102, 102, 102));
        btnCar6.setForeground(new java.awt.Color(102, 102, 102));
        btnCar6.setName("Car6"); // NOI18N
        btnCar6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCar6ActionPerformed(evt);
            }
        });
        getContentPane().add(btnCar6, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 110, 40, 90));

        btnCar7.setBackground(new java.awt.Color(102, 102, 102));
        btnCar7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80).png"))); // NOI18N
        btnCar7.setName("Car7"); // NOI18N
        getContentPane().add(btnCar7, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 110, 40, 90));

        btnCar8.setBackground(new java.awt.Color(102, 102, 102));
        btnCar8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80).png"))); // NOI18N
        btnCar8.setName("Car8"); // NOI18N
        getContentPane().add(btnCar8, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 110, 40, 90));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bushes-unscreen-ezgif.com-resize.gif"))); // NOI18N
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 350, 60, 40));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bushes-ezgif.com-resize.gif"))); // NOI18N
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 350, 60, -1));

        btnCar13.setBackground(new java.awt.Color(102, 102, 102));
        btnCar13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80)2.png"))); // NOI18N
        btnCar13.setToolTipText("");
        btnCar13.setName("Car13"); // NOI18N
        btnCar13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCar13ActionPerformed(evt);
            }
        });
        getContentPane().add(btnCar13, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 260, 40, -1));

        btnCar14.setBackground(new java.awt.Color(102, 102, 102));
        btnCar14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80)2.png"))); // NOI18N
        btnCar14.setName("Car14"); // NOI18N
        getContentPane().add(btnCar14, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 260, 40, -1));

        btnCar10.setBackground(new java.awt.Color(102, 102, 102));
        btnCar10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80)2.png"))); // NOI18N
        btnCar10.setName("Car10"); // NOI18N
        getContentPane().add(btnCar10, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 260, 40, -1));

        btnCar15.setBackground(new java.awt.Color(102, 102, 102));
        btnCar15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80)2.png"))); // NOI18N
        btnCar15.setName("Car15"); // NOI18N
        getContentPane().add(btnCar15, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 260, 40, -1));

        btnCar11.setBackground(new java.awt.Color(102, 102, 102));
        btnCar11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80)2.png"))); // NOI18N
        btnCar11.setName("Car11"); // NOI18N
        btnCar11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCar11ActionPerformed(evt);
            }
        });
        getContentPane().add(btnCar11, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 260, 40, -1));

        btnCar18.setBackground(new java.awt.Color(102, 102, 102));
        btnCar18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80).png"))); // NOI18N
        btnCar18.setName("Car18"); // NOI18N
        getContentPane().add(btnCar18, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 400, 40, -1));

        btnCar16.setBackground(new java.awt.Color(102, 102, 102));
        btnCar16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80)2.png"))); // NOI18N
        btnCar16.setName("Car16"); // NOI18N
        getContentPane().add(btnCar16, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 260, 40, -1));

        btnCar20.setBackground(new java.awt.Color(102, 102, 102));
        btnCar20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80).png"))); // NOI18N
        btnCar20.setName("Car20"); // NOI18N
        getContentPane().add(btnCar20, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 400, 40, -1));

        btnCar19.setBackground(new java.awt.Color(102, 102, 102));
        btnCar19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80).png"))); // NOI18N
        btnCar19.setName("Car19"); // NOI18N
        getContentPane().add(btnCar19, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 400, 40, -1));

        btnCar21.setBackground(new java.awt.Color(102, 102, 102));
        btnCar21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80).png"))); // NOI18N
        btnCar21.setName("Car21"); // NOI18N
        getContentPane().add(btnCar21, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 400, 40, -1));

        btnCar22.setBackground(new java.awt.Color(102, 102, 102));
        btnCar22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80).png"))); // NOI18N
        btnCar22.setName("Car22"); // NOI18N
        getContentPane().add(btnCar22, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 400, 40, -1));

        btnCar24.setBackground(new java.awt.Color(102, 102, 102));
        btnCar24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80).png"))); // NOI18N
        btnCar24.setName("Car24"); // NOI18N
        getContentPane().add(btnCar24, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 400, 40, -1));

        btnCar23.setBackground(new java.awt.Color(102, 102, 102));
        btnCar23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80).png"))); // NOI18N
        btnCar23.setName("Car23"); // NOI18N
        getContentPane().add(btnCar23, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 400, 40, -1));

        btnCar17.setBackground(new java.awt.Color(102, 102, 102));
        btnCar17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80).png"))); // NOI18N
        btnCar17.setName("Car17"); // NOI18N
        getContentPane().add(btnCar17, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 400, 40, -1));

        btnCar26.setBackground(new java.awt.Color(102, 102, 102));
        btnCar26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80)2.png"))); // NOI18N
        btnCar26.setName("Car26"); // NOI18N
        getContentPane().add(btnCar26, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 560, 40, -1));

        btnCar29.setBackground(new java.awt.Color(102, 102, 102));
        btnCar29.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80)2.png"))); // NOI18N
        btnCar29.setName("Car29"); // NOI18N
        getContentPane().add(btnCar29, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 560, 40, -1));

        btnCar25.setBackground(new java.awt.Color(102, 102, 102));
        btnCar25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80)2.png"))); // NOI18N
        btnCar25.setName("Car25"); // NOI18N
        getContentPane().add(btnCar25, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 560, 40, -1));

        btnCar28.setBackground(new java.awt.Color(102, 102, 102));
        btnCar28.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80)2.png"))); // NOI18N
        btnCar28.setName("Car28"); // NOI18N
        getContentPane().add(btnCar28, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 560, 40, -1));

        btnCar27.setBackground(new java.awt.Color(102, 102, 102));
        btnCar27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80)2.png"))); // NOI18N
        btnCar27.setName("Car27"); // NOI18N
        getContentPane().add(btnCar27, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 560, 40, -1));

        btnCar30.setBackground(new java.awt.Color(102, 102, 102));
        btnCar30.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80)2.png"))); // NOI18N
        btnCar30.setName("Car30"); // NOI18N
        getContentPane().add(btnCar30, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 560, 40, -1));

        btnMc5.setBackground(new java.awt.Color(102, 102, 102));
        btnMc5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc5.setName("Mc5"); // NOI18N
        getContentPane().add(btnMc5, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 210, 70, 20));

        btnCar32.setBackground(new java.awt.Color(102, 102, 102));
        btnCar32.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80)2.png"))); // NOI18N
        btnCar32.setName("Car32"); // NOI18N
        getContentPane().add(btnCar32, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 560, 40, -1));

        btnMc6.setBackground(new java.awt.Color(102, 102, 102));
        btnMc6.setForeground(new java.awt.Color(102, 102, 102));
        btnMc6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc6.setName("Mc6"); // NOI18N
        btnMc6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMc6ActionPerformed(evt);
            }
        });
        getContentPane().add(btnMc6, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 230, 70, 20));

        btnMc4.setBackground(new java.awt.Color(102, 102, 102));
        btnMc4.setForeground(new java.awt.Color(102, 102, 102));
        btnMc4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc4.setName("Mc4"); // NOI18N
        btnMc4.setSelected(true);
        btnMc4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMc4ActionPerformed(evt);
            }
        });
        getContentPane().add(btnMc4, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 180, 70, 20));

        btnCar31.setBackground(new java.awt.Color(102, 102, 102));
        btnCar31.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80)2.png"))); // NOI18N
        btnCar31.setName("Car31"); // NOI18N
        getContentPane().add(btnCar31, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 560, 40, -1));

        btnCar12.setBackground(new java.awt.Color(102, 102, 102));
        btnCar12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80)2.png"))); // NOI18N
        btnCar12.setName("Car12"); // NOI18N
        getContentPane().add(btnCar12, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 260, 40, -1));

        btnCar9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/carAesfd (40) (80)2.png"))); // NOI18N
        btnCar9.setBackground(new java.awt.Color(102, 102, 102));
        btnCar9.setForeground(new java.awt.Color(102, 102, 102));
        btnCar9.setName("Car9"); // NOI18N
        btnCar9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCar9ActionPerformed(evt);
            }
        });
        getContentPane().add(btnCar9, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 260, 40, -1));

        btnMc25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (2).png"))); // NOI18N
        btnMc25.setBackground(new java.awt.Color(102, 102, 102));
        btnMc25.setForeground(new java.awt.Color(102, 102, 102));
        btnMc25.setName("Mc25"); // NOI18N
        btnMc25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMc25ActionPerformed(evt);
            }
        });
        getContentPane().add(btnMc25, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 120, 70, 20));

        btnMc3.setBackground(new java.awt.Color(102, 102, 102));
        btnMc3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc3.setName("Mc3"); // NOI18N
        getContentPane().add(btnMc3, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 160, 70, 20));

        btnMc10.setBackground(new java.awt.Color(102, 102, 102));
        btnMc10.setForeground(new java.awt.Color(102, 102, 102));
        btnMc10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc10.setName("Mc10"); // NOI18N
        getContentPane().add(btnMc10, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 320, 70, 20));

        btnMc9.setBackground(new java.awt.Color(102, 102, 102));
        btnMc9.setForeground(new java.awt.Color(102, 102, 102));
        btnMc9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc9.setName("Mc9"); // NOI18N
        getContentPane().add(btnMc9, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 297, 70, 20));

        btnMc12.setBackground(new java.awt.Color(102, 102, 102));
        btnMc12.setForeground(new java.awt.Color(102, 102, 102));
        btnMc12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc12.setName("Mc12"); // NOI18N
        getContentPane().add(btnMc12, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 360, 70, 20));

        btnMc13.setBackground(new java.awt.Color(102, 102, 102));
        btnMc13.setForeground(new java.awt.Color(102, 102, 102));
        btnMc13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc13.setName("Mc13"); // NOI18N
        getContentPane().add(btnMc13, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 390, 70, 20));

        btnMc14.setBackground(new java.awt.Color(102, 102, 102));
        btnMc14.setForeground(new java.awt.Color(102, 102, 102));
        btnMc14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc14.setName("Mc14"); // NOI18N
        getContentPane().add(btnMc14, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 410, 70, 20));

        btnMc8.setBackground(new java.awt.Color(102, 102, 102));
        btnMc8.setForeground(new java.awt.Color(102, 102, 102));
        btnMc8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc8.setName("Mc8"); // NOI18N
        getContentPane().add(btnMc8, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 270, 70, 20));

        btnMc16.setBackground(new java.awt.Color(102, 102, 102));
        btnMc16.setForeground(new java.awt.Color(102, 102, 102));
        btnMc16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc16.setName("Mc16"); // NOI18N
        getContentPane().add(btnMc16, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 450, 70, 20));

        btnMc18.setBackground(new java.awt.Color(102, 102, 102));
        btnMc18.setForeground(new java.awt.Color(102, 102, 102));
        btnMc18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc18.setName("Mc18"); // NOI18N
        getContentPane().add(btnMc18, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 500, 70, 20));

        btnMc17.setBackground(new java.awt.Color(102, 102, 102));
        btnMc17.setForeground(new java.awt.Color(102, 102, 102));
        btnMc17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc17.setName("Mc17"); // NOI18N
        getContentPane().add(btnMc17, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 480, 70, 20));

        btnMc11.setBackground(new java.awt.Color(102, 102, 102));
        btnMc11.setForeground(new java.awt.Color(102, 102, 102));
        btnMc11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc11.setName("Mc11"); // NOI18N
        getContentPane().add(btnMc11, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 340, 70, 20));

        btnMc19.setBackground(new java.awt.Color(102, 102, 102));
        btnMc19.setForeground(new java.awt.Color(102, 102, 102));
        btnMc19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc19.setName("Mc19"); // NOI18N
        getContentPane().add(btnMc19, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 520, 70, 20));

        btnMc23.setBackground(new java.awt.Color(102, 102, 102));
        btnMc23.setForeground(new java.awt.Color(102, 102, 102));
        btnMc23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc23.setName("Mc23"); // NOI18N
        getContentPane().add(btnMc23, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 610, 70, 20));

        btnMc22.setBackground(new java.awt.Color(102, 102, 102));
        btnMc22.setForeground(new java.awt.Color(102, 102, 102));
        btnMc22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc22.setName("Mc22"); // NOI18N
        getContentPane().add(btnMc22, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 580, 70, 20));

        btnMc15.setBackground(new java.awt.Color(102, 102, 102));
        btnMc15.setForeground(new java.awt.Color(102, 102, 102));
        btnMc15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc15.setName("Mc15"); // NOI18N
        getContentPane().add(btnMc15, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 430, 70, 20));

        btnMc21.setBackground(new java.awt.Color(102, 102, 102));
        btnMc21.setForeground(new java.awt.Color(102, 102, 102));
        btnMc21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc21.setName("Mc21"); // NOI18N
        getContentPane().add(btnMc21, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 560, 70, 20));

        btnMc20.setBackground(new java.awt.Color(102, 102, 102));
        btnMc20.setForeground(new java.awt.Color(102, 102, 102));
        btnMc20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc20.setName("Mc20"); // NOI18N
        getContentPane().add(btnMc20, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 540, 70, 20));

        btnMc24.setBackground(new java.awt.Color(102, 102, 102));
        btnMc24.setForeground(new java.awt.Color(102, 102, 102));
        btnMc24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc24.setName("Mc24"); // NOI18N
        getContentPane().add(btnMc24, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 630, 70, 20));

        btnMc7.setBackground(new java.awt.Color(102, 102, 102));
        btnMc7.setForeground(new java.awt.Color(102, 102, 102));
        btnMc7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc7.setName("Mc7"); // NOI18N
        getContentPane().add(btnMc7, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 250, 70, 20));

        btnMc2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc2.setBackground(new java.awt.Color(102, 102, 102));
        btnMc2.setForeground(new java.awt.Color(102, 102, 102));
        btnMc2.setName("Mc2"); // NOI18N
        getContentPane().add(btnMc2, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 140, 70, 20));

        btnMc1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/motorrmvbg (1).png"))); // NOI18N
        btnMc1.setBackground(new java.awt.Color(102, 102, 102));
        btnMc1.setForeground(new java.awt.Color(102, 102, 102));
        btnMc1.setName("Mc1"); // NOI18N
        getContentPane().add(btnMc1, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 120, 70, 20));

        parkinglotBG.setBackground(new java.awt.Color(102, 102, 102));
        parkinglotBG.setForeground(new java.awt.Color(102, 102, 102));
        parkinglotBG.setIcon(new javax.swing.ImageIcon(getClass().getResource("/parkinglot.png-1.jpg"))); // NOI18N
        parkinglotBG.setName("Mc19"); // NOI18N
        getContentPane().add(parkinglotBG, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, -1, -1));

        jLabel2.setFont(new java.awt.Font("Streetvertising", 0, 18)); // NOI18N
        jLabel2.setText("PHINMA COC PARKING AREA");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 30, -1, -1));

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cocsmaller.png"))); // NOI18N
        getContentPane().add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 20, -1, -1));

        btnBack.setFont(new java.awt.Font("Streetvertising", 0, 12)); // NOI18N
        btnBack.setText("BACK");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });
        getContentPane().add(btnBack, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 30, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCar3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCar3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCar3ActionPerformed

    private void btnMc25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMc25ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnMc25ActionPerformed

    private void btnCar9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCar9ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCar9ActionPerformed

    private void btnCar6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCar6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCar6ActionPerformed

    private void btnCar13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCar13ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCar13ActionPerformed

    private void btnCar11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCar11ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCar11ActionPerformed

    private void btnMc4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMc4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnMc4ActionPerformed

    private void btnMc6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMc6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnMc6ActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
     
    // Close current window
    this.dispose();

    // Return to login form
    java.awt.EventQueue.invokeLater(() -> {
        new Parking1().setVisible(true);
    });
    }//GEN-LAST:event_btnBackActionPerformed

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
            java.util.logging.Logger.getLogger(Map.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Map.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Map.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Map.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Map().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnCar1;
    private javax.swing.JButton btnCar10;
    private javax.swing.JButton btnCar11;
    private javax.swing.JButton btnCar12;
    private javax.swing.JButton btnCar13;
    private javax.swing.JButton btnCar14;
    private javax.swing.JButton btnCar15;
    private javax.swing.JButton btnCar16;
    private javax.swing.JButton btnCar17;
    private javax.swing.JButton btnCar18;
    private javax.swing.JButton btnCar19;
    private javax.swing.JButton btnCar2;
    private javax.swing.JButton btnCar20;
    private javax.swing.JButton btnCar21;
    private javax.swing.JButton btnCar22;
    private javax.swing.JButton btnCar23;
    private javax.swing.JButton btnCar24;
    private javax.swing.JButton btnCar25;
    private javax.swing.JButton btnCar26;
    private javax.swing.JButton btnCar27;
    private javax.swing.JButton btnCar28;
    private javax.swing.JButton btnCar29;
    private javax.swing.JButton btnCar3;
    private javax.swing.JButton btnCar30;
    private javax.swing.JButton btnCar31;
    private javax.swing.JButton btnCar32;
    private javax.swing.JButton btnCar4;
    private javax.swing.JButton btnCar5;
    private javax.swing.JButton btnCar6;
    private javax.swing.JButton btnCar7;
    private javax.swing.JButton btnCar8;
    private javax.swing.JButton btnCar9;
    private javax.swing.JButton btnMc1;
    private javax.swing.JButton btnMc10;
    private javax.swing.JButton btnMc11;
    private javax.swing.JButton btnMc12;
    private javax.swing.JButton btnMc13;
    private javax.swing.JButton btnMc14;
    private javax.swing.JButton btnMc15;
    private javax.swing.JButton btnMc16;
    private javax.swing.JButton btnMc17;
    private javax.swing.JButton btnMc18;
    private javax.swing.JButton btnMc19;
    private javax.swing.JButton btnMc2;
    private javax.swing.JButton btnMc20;
    private javax.swing.JButton btnMc21;
    private javax.swing.JButton btnMc22;
    private javax.swing.JButton btnMc23;
    private javax.swing.JButton btnMc24;
    private javax.swing.JButton btnMc25;
    private javax.swing.JButton btnMc3;
    private javax.swing.JButton btnMc4;
    private javax.swing.JButton btnMc5;
    private javax.swing.JButton btnMc6;
    private javax.swing.JButton btnMc7;
    private javax.swing.JButton btnMc8;
    private javax.swing.JButton btnMc9;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel parkinglotBG;
    // End of variables declaration//GEN-END:variables
}
