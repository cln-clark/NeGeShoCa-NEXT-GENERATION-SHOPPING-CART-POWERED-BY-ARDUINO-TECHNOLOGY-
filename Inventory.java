/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.posapplication;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

 class Inventory extends javax.swing.JFrame {

    public Inventory() {
        initComponents();
        
        customizeTableHeader();
        adjustColumnSizes();
        
        try {
            FirebaseInitializer.initialize();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to initialize Firebase: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        // CALL METHODS HERE
        fetchDataFromFirebase();  
        setupTableSelectionListener();
        
        // Add DocumentListener to search textfield
        search_field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onSearchChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onSearchChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onSearchChanged();
            }
        });
    }

    //----------------------------------- METHODS -----------------------------------------------------------
    
    // Fetch data from firebase
    private void fetchDataFromFirebase() {
    // Reference the "products" node in Firebase
    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("products");

    databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                // Clear the table before populating it
                DefaultTableModel model = (DefaultTableModel) product_table.getModel();
                model.setRowCount(0);

                // Iterate through each product in the "products" node
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    String productName = productSnapshot.child("productName").getValue(String.class);
                    int quantity = productSnapshot.child("qty").getValue(int.class);
                    String category = productSnapshot.child("category").getValue(String.class);
                    String productBarcode = productSnapshot.child("productBarcode").getValue(String.class);

                    // Add the data to the JTable (adjust columns accordingly)
                    model.addRow(new Object[]{productBarcode, productName, quantity, category});
                }
            } else {
                JOptionPane.showMessageDialog(null, "No products found in Firebase!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            JOptionPane.showMessageDialog(null, "Error fetching data: " + databaseError.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    });
}
    
    // Method to adjust column sizes
    private void adjustColumnSizes() {
        // Get the TableColumnModel of your JTable
        TableColumnModel columnModel1 = product_table.getColumnModel();
        TableColumnModel columnModel2 = stock_table.getColumnModel();

        // Set the width for each column
        // Index 0
        columnModel1.getColumn(0).setPreferredWidth(110); // product barcode
        columnModel2.getColumn(0).setPreferredWidth(110); // product barcode

        // Index 1
        columnModel1.getColumn(1).setPreferredWidth(380); // name
        columnModel2.getColumn(1).setPreferredWidth(380); // name
        
        // Index 2
        columnModel1.getColumn(2).setPreferredWidth(70); // quantity
        columnModel2.getColumn(2).setPreferredWidth(380); // added stock
        
        // Index 3
        columnModel1.getColumn(3).setPreferredWidth(100); // category
        columnModel2.getColumn(3).setPreferredWidth(100); // sold stock
        
        // Index 4 
        columnModel2.getColumn(3).setPreferredWidth(100); // current stock

        // Optional: Ensure columns are not resizable
        for (int i = 0; i < columnModel1.getColumnCount(); i++) {
            columnModel1.getColumn(i).setResizable(false);
        }
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER); // Center align 
        product_table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); 
        
        // Stock tracking table
        stock_table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); 
        stock_table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); 
        stock_table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); 
    }
    
    // Customized Table Header
    private void customizeTableHeader() {
        // Get the table header
        JTableHeader tableHeader = product_table.getTableHeader();

        // Set the font and size of the header
        tableHeader.setFont(new Font("SansSerif", Font.BOLD, 20)); // Larger font size

        // Set the preferred height of the header
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 35)); // Adjust header height

        // Custom renderer for the header
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER); // Center-align header text

    }
    
    // Selection Listener for Product Management Table
    private void setupTableSelectionListener() {
        // Add a ListSelectionListener to the JTable
        product_table.getSelectionModel().addListSelectionListener(event -> {
            // Check if a row is actually selected
            if (!event.getValueIsAdjusting() && product_table.getSelectedRow() != -1) {
                int selectedRow = product_table.getSelectedRow();

                // Fetch data from the selected row
                String barcodeNumber = product_table.getValueAt(selectedRow,0).toString();
                String productName = product_table.getValueAt(selectedRow, 1).toString();
                String productQuantity = product_table.getValueAt(selectedRow, 2).toString();
                String productCategory = product_table.getValueAt(selectedRow, 3).toString();               

                // Populate text fields
                barcode_field.setText(barcodeNumber);
                name_field.setText(productName); // Product Name text field
                qty_field.setText(productQuantity); // Product Price text field
                category_field.setText(productCategory); // Product Quantity text field
            }
        });
       
        
}
    
    // Filter the table by search
    private void onSearchChanged(){
        String searchText = search_field.getText().toLowerCase(); // Get the search text and convert to lowercase for case-insensitive search
        DefaultTableModel model = (DefaultTableModel) product_table.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        product_table.setRowSorter(sorter);

        // Create a filter
        RowFilter<DefaultTableModel, Object> rowFilter = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(RowFilter.Entry<? extends DefaultTableModel, ? extends Object> entry) {
                for (int i = 0; i < entry.getValueCount(); i++) {
                    String value = entry.getStringValue(i).toLowerCase();
                    if (value.contains(searchText)) { // Check if the value contains the search text
                        return true;
                    }
                }
                return false;
            }
        };

        sorter.setRowFilter(rowFilter); // Apply the filter to the sorter
    }


    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        product_table = new javax.swing.JTable();
        search_field = new javax.swing.JTextField();
        name_field = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        qty_field = new javax.swing.JTextField();
        category_field = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        barcode_field = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        stock_table = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        tabbedPane.setBackground(new java.awt.Color(255, 255, 255));
        tabbedPane.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        tabbedPane.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        product_table.setAutoCreateRowSorter(true);
        product_table.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        product_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Product Barcode Number", "Name", "Quantity", "Category"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        product_table.setIntercellSpacing(new java.awt.Dimension(0, 8));
        product_table.setMinimumSize(new java.awt.Dimension(60, 800));
        product_table.setRowHeight(45);
        jScrollPane1.setViewportView(product_table);
        if (product_table.getColumnModel().getColumnCount() > 0) {
            product_table.getColumnModel().getColumn(0).setResizable(false);
            product_table.getColumnModel().getColumn(0).setPreferredWidth(150);
            product_table.getColumnModel().getColumn(1).setResizable(false);
            product_table.getColumnModel().getColumn(1).setPreferredWidth(400);
            product_table.getColumnModel().getColumn(2).setResizable(false);
            product_table.getColumnModel().getColumn(2).setPreferredWidth(50);
            product_table.getColumnModel().getColumn(3).setResizable(false);
            product_table.getColumnModel().getColumn(3).setPreferredWidth(150);
        }

        search_field.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        search_field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                search_fieldActionPerformed(evt);
            }
        });

        name_field.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel1.setText("Product Name:");

        qty_field.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        category_field.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel2.setText("Quantity:");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel3.setText("Category:");

        barcode_field.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel4.setText("Barcode Number:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 906, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(search_field)
                            .addComponent(name_field)
                            .addComponent(category_field)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(qty_field, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3)
                                    .addComponent(barcode_field, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 43, Short.MAX_VALUE)))
                        .addGap(69, 69, 69))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(search_field, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(62, 62, 62)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(barcode_field, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(name_field, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(52, 52, 52)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(qty_field, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(category_field, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(425, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        tabbedPane.addTab("PRODUCT MANAGEMENT", jPanel2);

        stock_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Product Barcode Number", "Name", "Added Stock", "Sold Stock", "Current Stock"
            }
        ));
        jScrollPane2.setViewportView(stock_table);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 907, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(371, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1044, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("STOCK TRACKING", jPanel3);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1284, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1056, Short.MAX_VALUE)
        );

        tabbedPane.addTab("SALES MANAGEMENT", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 1491, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void search_fieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_search_fieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_search_fieldActionPerformed

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
            java.util.logging.Logger.getLogger(Inventory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Inventory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Inventory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Inventory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Inventory().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField barcode_field;
    private javax.swing.JTextField category_field;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField name_field;
    private javax.swing.JTable product_table;
    private javax.swing.JTextField qty_field;
    private javax.swing.JTextField search_field;
    private javax.swing.JTable stock_table;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
}
