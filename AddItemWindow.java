package com.mycompany.posapplication;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


public class AddItemWindow extends javax.swing.JFrame {
    
    private POSapp mainWindow;

    public AddItemWindow(POSapp mainWindow) {
        initComponents();
        this.mainWindow = mainWindow;
        
        try {
            FirebaseInitializer.initialize();          
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to initialize Firebase: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }        
        
        // Set initial focus to the barcode field
        SwingUtilities.invokeLater(barcodeField::requestFocusInWindow);
        
        
        // Add an ActionListener to the text field
        barcodeField.addActionListener(e -> fetchDataFromFirebase(barcodeField.getText().trim()));
        
    }
    
    //------------------------------- methods here ---------------------------------------------

    private void fetchDataFromFirebase(String barcode) {
    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("products");

    // Query the Firebase node for the products
    databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            boolean barcodeFound = false;

            // Loop through all products
            for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                // Access barcode as a child key of each product
                String productBarcode = productSnapshot.getKey(); // This is the child key representing the barcode

                // Check if the scanned barcode matches the product's barcode
                if (productBarcode != null && productBarcode.equals(barcode)) {
                    String productName = productSnapshot.child("productName").getValue(String.class);
                    Double productPrice = productSnapshot.child("productPrice").getValue(Double.class);
                    Integer productQty = 1;  // Default quantity for each scan
                    
                    double total = productPrice * productQty;

                    productName_field.setText(productName);
                    productPrice_field.setText(String.format("%.2f",productPrice));
                    productQty_field.setText(String.valueOf(productQty));                                  
                    total_field.setText(String.format("%.2f",total));

                    barcodeFound = true;
                    break;   
                }
            }
            
            // Show error if barcode is not found
            if (!barcodeFound) {
                JOptionPane.showMessageDialog(null, "Barcode does not exist", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            JOptionPane.showMessageDialog(null, "Error fetching data: " + databaseError.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    });
}
    
      
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        barcodeField = new javax.swing.JTextField();
        addButton = new javax.swing.JButton();
        productName_field = new javax.swing.JTextField();
        productQty_field = new javax.swing.JTextField();
        productPrice_field = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        total_field = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        barcodeField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        barcodeField.setForeground(java.awt.Color.gray);
        barcodeField.setText("Enter item barcode or Scan Barcode");
        barcodeField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                barcodeFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                barcodeFieldFocusLost(evt);
            }
        });

        addButton.setBackground(new java.awt.Color(0, 204, 102));
        addButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        addButton.setForeground(new java.awt.Color(255, 255, 255));
        addButton.setText("ADD");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        productName_field.setEditable(false);
        productName_field.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        productQty_field.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        productQty_field.setText("1");

        productPrice_field.setEditable(false);
        productPrice_field.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        productPrice_field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                productPrice_fieldActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jLabel1.setText("Quantity:");

        total_field.setEditable(false);
        total_field.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        total_field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                total_fieldActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jLabel2.setText("Total:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(productName_field)
                        .addGap(131, 131, 131))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(productQty_field, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(addButton, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                    .addComponent(productPrice_field)
                    .addComponent(total_field))
                .addGap(31, 31, 31))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(132, 132, 132)
                .addComponent(barcodeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 168, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(barcodeField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(productName_field, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(productPrice_field, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(productQty_field, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(total_field, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(43, 43, 43)
                .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void barcodeFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_barcodeFieldFocusGained
        if(barcodeField.getText().equals("Enter item barcode or Scan Barcode")){
            barcodeField.setText("");
            barcodeField.setForeground(Color.BLACK);
        }
    }//GEN-LAST:event_barcodeFieldFocusGained

    private void barcodeFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_barcodeFieldFocusLost
        if(barcodeField.getText().equals("")){
            barcodeField.setText("Enter item barcode or Scan Barcode");
            barcodeField.setForeground(Color.GRAY);
        }
    }//GEN-LAST:event_barcodeFieldFocusLost

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
                                                 
        String productBarcode = barcodeField.getText();
        String productName = productName_field.getText();
        String productPriceStr = productPrice_field.getText();
        String productQtyStr = productQty_field.getText();

        if (productBarcode.isEmpty() || productName.isEmpty() || productPriceStr.isEmpty() || productQtyStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("payingSession/");

        try {
            // Log to check if databaseReference is null
            System.out.println("Database Reference: " + databaseReference);

            // Parse numeric values
            double productPrice = Double.parseDouble(productPriceStr);
            int productQty = Integer.parseInt(productQtyStr);
            double totalPricePerItem = productPrice * productQty;

            // Get the payID from main window
            String payID = mainWindow.getPayID(); 

            // Log the payID and mainWindow
            System.out.println("Main Window: " + mainWindow); 
            System.out.println("Pay ID: " + payID);  // Log payID

            if (payID == null || payID.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Error: Invalid Pay ID.");
                return;
            }

            // Reference to the payID node
            DatabaseReference payIDRef = databaseReference.child(payID);

            // Log payIDRef to check if it is null
            System.out.println("payIDRef: " + payIDRef);

            if (payIDRef == null) {
                JOptionPane.showMessageDialog(this, "Error: Database reference is null.");
                return;
            }

            // Add the new item to the "items" array
            Map<String, Object> newItem = new HashMap<>();
            newItem.put("productBarcode", productBarcode);
            newItem.put("productName", productName);
            newItem.put("productPrice", productPrice);
            newItem.put("productQty", productQty);
            newItem.put("totalPricePerItem", totalPricePerItem);

            // Log the Firebase path
            System.out.println("Firebase Path: " + payIDRef.child("items").toString());

            payIDRef.child("items").push().setValueAsync(newItem);

            // Update the total price
            DatabaseReference totalRef = payIDRef.child("/total");
            totalRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    double currentTotal = snapshot.exists() ? snapshot.getValue(Double.class) : 0.0;
                    double newTotal = currentTotal + totalPricePerItem;
                    totalRef.setValueAsync(newTotal);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    JOptionPane.showMessageDialog(AddItemWindow.this, "Error updating total: " + error.getMessage());
                }
            });

            JOptionPane.showMessageDialog(this, "Data saved successfully!");

            // Clear fields after saving
            barcodeField.setText("");
            productName_field.setText("");  // Ensure this is also cleared
            productPrice_field.setText("");  // Ensure this is cleared only once
            productQty_field.setText("");

        } catch (Exception ex) {
            ex.printStackTrace();  // Log the full stack trace for debugging
            JOptionPane.showMessageDialog(this, "Error occurred. Check the console for details.");
        }

        this.dispose();
    }//GEN-LAST:event_addButtonActionPerformed

    private void total_fieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_total_fieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_total_fieldActionPerformed

    private void productPrice_fieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_productPrice_fieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_productPrice_fieldActionPerformed

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
            java.util.logging.Logger.getLogger(AddItemWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddItemWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddItemWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddItemWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                POSapp mainWindow = new POSapp();
                new AddItemWindow(mainWindow).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JTextField barcodeField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField productName_field;
    private javax.swing.JTextField productPrice_field;
    private javax.swing.JTextField productQty_field;
    private javax.swing.JTextField total_field;
    // End of variables declaration//GEN-END:variables
}
