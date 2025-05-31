package com.mycompany.posapplication;

import com.google.firebase.database.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

public class POSapp extends javax.swing.JFrame {
    
    public POSapp() {
        initComponents();
        
        try {
            FirebaseInitializer.initialize();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to initialize Firebase: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
          
        // CALL METHODS HERE
        setupTableSelectionListener();
        
        adjustColumnSizes();
           
        customizeTableHeader();
        
        startAssistanceListener();
        
        // Set initial focus to the payid_text field
        SwingUtilities.invokeLater(payid_text::requestFocusInWindow);
        
        // Add DocumentListener to cash_field
        cash_field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                computeChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                computeChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                computeChange();
            }
        });
        
        // Add an ActionListener to the text field
        payid_text.addActionListener(e -> fetchDataFromFirebase(payid_text.getText().trim()));
     
    }
    
    // ----------------------------------------- METHODS ----------------------------------------------------
    
    public String getPayID(){ 
        String ID = payid_text.getText().trim();   
        System.out.println("PAYID RETRIEVED: "+ ID);
        return ID;       
    }
    
    // Listener method for assistance
    private void startAssistanceListener() {
        try {
            
            // Reference to the assistanceRequests node
            DatabaseReference assistanceRef = FirebaseDatabase.getInstance().getReference("assistanceRequests/pos1");

            // Add a real-time listener
            assistanceRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    // Check if a request has been made
                    Boolean requested = snapshot.child("requested").getValue(Boolean.class);
                    String message = snapshot.child("message").getValue(String.class);

                    if (requested != null && requested) {
                        // Trigger an alert in the POS system
                        JOptionPane.showMessageDialog(null, message, "Assistance Requested!", JOptionPane.INFORMATION_MESSAGE);
                        
                        assistanceRef.child("requested").setValue(false, new DatabaseReference.CompletionListener(){
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference){
                                if(databaseError != null){
                                     JOptionPane.showMessageDialog(null, "Failed Calling Assistance, please try again. " + databaseError.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                } else{
                                    System.out.println("Assistance is on the way!");
                                }
                            }
                        });
                        
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Handle listener cancellation
                    JOptionPane.showMessageDialog(null, "Error: " + error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        } catch (Exception e) {
            // Handle initialization errors
            JOptionPane.showMessageDialog(null, "Error initializing Firebase: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
     
    // Selection Listener
    private void setupTableSelectionListener() {
        // Add a ListSelectionListener to the JTable
        itemTable.getSelectionModel().addListSelectionListener(event -> {
            // Check if a row is actually selected
            if (!event.getValueIsAdjusting() && itemTable.getSelectedRow() != -1) {
                int selectedRow = itemTable.getSelectedRow();

                // Fetch data from the selected row
                String productName = itemTable.getValueAt(selectedRow, 0).toString();
                String productPrice = itemTable.getValueAt(selectedRow, 1).toString();
                String productQty = itemTable.getValueAt(selectedRow, 2).toString();
                
                double price = Double.parseDouble(productPrice);

                // Populate text fields
                product_text.setText(productName); // Product Name text field
                price_text.setText(String.format("%.2f", price)); // Product Price text field
                qty_field.setText(productQty); // Product Quantity text field

                // Make the quantity field editable
                product_text.setEditable(false);
                price_text.setEditable(false);
                qty_field.setEditable(true); // Only Quantity is editable

            }
        });
       
        
}

    // Fetch data from firebase
    private void fetchDataFromFirebase(String payid) {
    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("payingSession");
    
    // Query the Firebase node for the given Pay ID
    databaseRef.child(payid).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                // Clear the table before populating
                DefaultTableModel model = (DefaultTableModel) itemTable.getModel();
                model.setRowCount(0);
                

                // Fetch and display item details
                DataSnapshot itemsSnapshot = dataSnapshot.child("items");
                for (DataSnapshot itemSnapshot : itemsSnapshot.getChildren()) {
                    String productName = itemSnapshot.child("productName").getValue(String.class);
                    Double productPrice = itemSnapshot.child("productPrice").getValue(Double.class);
                    Integer productQty = itemSnapshot.child("productQty").getValue(Integer.class);
                    Double totalPricePerItem = itemSnapshot.child("totalPricePerItem").getValue(Double.class);

                    // Add a row to the JTable
                    model.addRow(new Object[]{productName, productPrice, productQty, totalPricePerItem});
                }
                
                // Apply the custom cell renderer for 'productPrice' column (index 1)
                itemTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                        if (value instanceof Double) {
                            // Format the price value to two decimal places
                            value = String.format("%.2f", value);
                        }
                        setText(value.toString());
                        return comp;
                    }
                });

                // Apply the custom cell renderer for 'Subtotal' column (index 3)
                itemTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                        if (value instanceof Double) {
                            // Format the total price per item to two decimal places
                            value = String.format("%.2f", value);
                        }
                        setText(value.toString());
                        return comp;
                    }
                });
               
                setupTableSelectionListener();
                
                // Fetch and display the total
                Double total = dataSnapshot.child("total").getValue(Double.class);
                total_field.setText("" + total); // Assuming totalLabel is your JLabel
            } else {
                JOptionPane.showMessageDialog(null, "Pay ID not doesn't exist", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            JOptionPane.showMessageDialog(null, "Error fetching data: " + databaseError.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    });
}
    
    // Method to update total price upon action
    private void updateTotalPrice(DatabaseReference itemsRef) {
    itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            double overallTotal = 0;

            // Check if the items node exists
            if (dataSnapshot.exists()) {
                // Loop through all items in Firebase and calculate the total
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    Double totalPrice = itemSnapshot.child("totalPricePerItem").getValue(Double.class);
                    if (totalPrice != null) {
                        overallTotal += totalPrice;
                    }
                }

                // Update the total field in Firebase under the main session node
                DatabaseReference totalRef = itemsRef.getParent().child("total");
                totalRef.setValue(overallTotal, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            JOptionPane.showMessageDialog(null, "Error updating total in Firebase: " + databaseError.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            System.out.println("Overall total updated successfully in Firebase.");
                        }
                    }
                });

                // Update the total in the UI (e.g., in a JTextField or JLabel)
                total_field.setText(String.format("%.2f", overallTotal));
            } else {
                JOptionPane.showMessageDialog(null, "No items found to calculate the total.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            JOptionPane.showMessageDialog(null, "Error reading items for total calculation: " + databaseError.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }); 
}
    
    // Method to adjust column sizes
    private void adjustColumnSizes() {
        // Get the TableColumnModel of your JTable
        TableColumnModel columnModel = itemTable.getColumnModel();

        // Set the width for each column
        // Index 0: Product Name column (make it wider)
        columnModel.getColumn(0).setPreferredWidth(450); // Wider size for product name

        // Index 1: Price column
        columnModel.getColumn(1).setPreferredWidth(100); // Same size for price

        // Index 2: Quantity column
        columnModel.getColumn(2).setPreferredWidth(70); // Same size for quantity

        // Index 3: Subtotal column
        columnModel.getColumn(3).setPreferredWidth(100); // Same size for subtotal

        // Optional: Ensure columns are not resizable
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setResizable(false);
        }
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER); // Center align
        itemTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); 
        itemTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); 
        itemTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); 
    }

    // Customized Table Header
    private void customizeTableHeader() {
        // Get the table header
        JTableHeader tableHeader = itemTable.getTableHeader();

        // Set the font and size of the header
        tableHeader.setFont(new Font("SansSerif", Font.BOLD, 24)); // Larger font size

        // Set the preferred height of the header
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 40)); // Adjust header height

        // Custom renderer for the header
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER); // Center-align header text

    }
    
    // Dynamically changes of customer's change
    private void computeChange(){
        try {
            // Get total amount and cash input
            String totalText = total_field.getText().trim();
            String cashText = cash_field.getText().trim();

            // Validate the input before parsing
            if (totalText.isEmpty() || cashText.isEmpty()) {
                change_field.setText("0.00"); // Reset change display
                return;
            }

            double totalAmount = Double.parseDouble(totalText);
            double cashReceived = Double.parseDouble(cashText);

            // Validate if cash received is sufficient
            if (cashReceived < totalAmount) {
                change_field.setText("Insufficient cash!");
                return;
            }

            // Calculate the change
            double change = cashReceived - totalAmount;

            // Display the change in the JLabel
            change_field.setText(String.format("%.2f", change)); // Properly formatted string
        } catch (NumberFormatException ex) {
            // Handle invalid inputs
            change_field.setText("Invalid input");
        }
    }
    
    // Update Quantity dynamically
    private void updateQuantity() {        
        String input = qty_field.getText().trim(); // Get the input text

        // Check if the input is empty
        if (input.isEmpty()) {
            return; // Do nothing for empty input
        }

        try {
            // Parse the input into an integer (quantity)
            int quantity = Integer.parseInt(input);

            // Ensure the quantity is greater than 0
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(null, "Quantity cannot be 0 or negative.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Get the selected row index from JTable and save
            int selectedRow = itemTable.getSelectedRow();
            if (selectedRow != -1) {
                // Preserve the selected product's details
                String productName = itemTable.getValueAt(selectedRow, 0).toString(); // Product name in column 0
                double productPrice = Double.parseDouble(itemTable.getValueAt(selectedRow, 1).toString()); // Price in column 1

                // Firebase reference to the items node
                String payID = payid_text.getText();
                DatabaseReference itemsRef = FirebaseDatabase.getInstance()
                        .getReference("payingSession/" + payID + "/items");

                // Query Firebase to find and update the item
                itemsRef.orderByChild("productName").equalTo(productName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean found = false;

                        for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                            String firebaseProductName = itemSnapshot.child("productName").getValue(String.class);

                            if (firebaseProductName != null && firebaseProductName.equals(productName)) {
                                found = true;

                                // Update the quantity in Firebase
                                itemSnapshot.getRef().child("productQty").setValue(quantity, (databaseError, databaseReference) -> {
                                    if (databaseError != null) {
                                        System.out.println("Error updating product quantity: " + databaseError.getMessage());
                                    } else {
                                        System.out.println("Product quantity updated successfully.");
                                    }
                                });

                                // Update the total price for the item
                                double totalPrice = quantity * productPrice;
                                itemSnapshot.getRef().child("totalPricePerItem").setValue(totalPrice, (databaseError, databaseReference) -> {
                                    if (databaseError != null) {
                                        System.out.println("Error updating total price: " + databaseError.getMessage());
                                    } else {
                                        System.out.println("Total price updated successfully.");
                                    }
                                });

                                break; // Exit loop once item is found and updated
                            }
                        }

                        if (!found) {
                            JOptionPane.showMessageDialog(null, "Product not found in Firebase!", "Error", JOptionPane.ERROR_MESSAGE);
                        }

                        // Refresh the table data after updating Firebase
                        fetchDataFromFirebase(payid_text.getText().trim());

                        // Update the total price after refreshing the data
                        updateTotalPrice(itemsRef);
                        
                        
                        
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        JOptionPane.showMessageDialog(null, "Error updating quantity: " + databaseError.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });                
                               
            } else {
                JOptionPane.showMessageDialog(null, "Please select a product to update.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Please enter a valid quantity.", "Invalid input", JOptionPane.WARNING_MESSAGE);
        }
        
        
    }
  
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        product_text = new javax.swing.JTextField();
        JScrollPane = new javax.swing.JScrollPane();
        itemTable = new javax.swing.JTable();
        price_text = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        updateQty_btn = new javax.swing.JButton();
        total_label = new javax.swing.JLabel();
        finishTransaction = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        payid_text = new javax.swing.JTextField();
        remove_btn = new javax.swing.JButton();
        qty_field = new javax.swing.JTextField();
        addItem_btn = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        cash_field = new javax.swing.JTextField();
        total_field = new javax.swing.JTextField();
        label_of_change = new javax.swing.JLabel();
        change_field = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(1920, 1000));

        product_text.setEditable(false);
        product_text.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        product_text.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                product_textActionPerformed(evt);
            }
        });

        itemTable.setAutoCreateRowSorter(true);
        itemTable.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        itemTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Price", "Quantity", "Subtotal"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, true, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        itemTable.setPreferredSize(new java.awt.Dimension(300, 840));
        itemTable.setRequestFocusEnabled(false);
        itemTable.setRowHeight(40);
        itemTable.setRowMargin(8);
        itemTable.setShowHorizontalLines(true);
        JScrollPane.setViewportView(itemTable);

        price_text.setEditable(false);
        price_text.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel2.setText("Product:");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel3.setText("Price:");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel4.setText("Quantity:");

        updateQty_btn.setBackground(new java.awt.Color(51, 153, 255));
        updateQty_btn.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        updateQty_btn.setForeground(new java.awt.Color(255, 255, 255));
        updateQty_btn.setText("UPDATE QTY");
        updateQty_btn.setFocusable(false);
        updateQty_btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                updateQty_btnMouseClicked(evt);
            }
        });
        updateQty_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateQty_btnActionPerformed(evt);
            }
        });

        total_label.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        total_label.setText("Total: ");

        finishTransaction.setBackground(new java.awt.Color(0, 204, 102));
        finishTransaction.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        finishTransaction.setForeground(new java.awt.Color(255, 255, 255));
        finishTransaction.setText("FINISH TRANSACTION");
        finishTransaction.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                finishTransactionMouseClicked(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel5.setText("PAY-ID");

        payid_text.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        payid_text.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                payid_textActionPerformed(evt);
            }
        });
        payid_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                payid_textKeyPressed(evt);
            }
        });

        remove_btn.setBackground(new java.awt.Color(255, 102, 102));
        remove_btn.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        remove_btn.setForeground(new java.awt.Color(255, 255, 255));
        remove_btn.setText("REMOVE ITEM");
        remove_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                remove_btnActionPerformed(evt);
            }
        });

        qty_field.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N

        addItem_btn.setBackground(new java.awt.Color(102, 153, 255));
        addItem_btn.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        addItem_btn.setForeground(new java.awt.Color(255, 255, 255));
        addItem_btn.setText("ADD ITEM");
        addItem_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addItem_btnActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel7.setText("Cash:");

        cash_field.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N

        total_field.setEditable(false);
        total_field.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N

        label_of_change.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        label_of_change.setText("Change:");

        change_field.setEditable(false);
        change_field.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        change_field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                change_fieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(JScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 920, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(product_text, javax.swing.GroupLayout.PREFERRED_SIZE, 382, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(price_text, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(total_field, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(total_label)
                                    .addComponent(cash_field, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7))
                                .addGap(36, 36, 36)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(addItem_btn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(updateQty_btn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(remove_btn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel4)
                                .addComponent(jLabel5)
                                .addComponent(payid_text, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(qty_field, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(52, 52, 52)))
                    .addComponent(finishTransaction, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(label_of_change, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(change_field, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(282, 524, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(598, 598, 598)
                    .addComponent(jLabel1)
                    .addContainerGap(1322, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(JScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 828, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(payid_text, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(44, 44, 44)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(product_text, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(price_text, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(15, 15, 15)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(qty_field, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(updateQty_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(total_label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(total_field, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(23, 23, 23)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cash_field, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(addItem_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(150, 150, 150)
                                .addComponent(remove_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(change_field, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label_of_change, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 81, Short.MAX_VALUE)
                        .addComponent(finishTransaction, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(132, 132, 132))))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(231, 231, 231)
                    .addComponent(jLabel1)
                    .addContainerGap(403, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1016, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void product_textActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_product_textActionPerformed
        
    }//GEN-LAST:event_product_textActionPerformed

    private void payid_textKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_payid_textKeyPressed
    
    }//GEN-LAST:event_payid_textKeyPressed

    private void updateQty_btnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_updateQty_btnMouseClicked
        updateQuantity();
    }//GEN-LAST:event_updateQty_btnMouseClicked

    private void finishTransactionMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_finishTransactionMouseClicked
        
    String raw_cash = cash_field.getText();
    
    if(raw_cash.isEmpty()){
             JOptionPane.showMessageDialog(null, "Please input the cash of the customer.", "Error", JOptionPane.WARNING_MESSAGE);
             return;
        }
        
    try {

        // Try to parse the input to a double
        double cash = Double.parseDouble(raw_cash);
        
        // Check if the table has any rows to clear
        if (itemTable.getRowCount() > 0) {
            // Confirm the action with the user
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to finish the transaction?", "Finish Transaction", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // Firebase logic to save the transaction under "toPrintReceipt"
                String payID = payid_text.getText();
                DatabaseReference sessionRef = FirebaseDatabase.getInstance().getReference("payingSession/" + payID);
                DatabaseReference toPrintReceiptRef = FirebaseDatabase.getInstance().getReference("toPrintReceipt/" + payID);
                DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("products");

                // Copy the entire session node to the "toPrintReceipt" node
                sessionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        
                        if (dataSnapshot.exists()) {
                            toPrintReceiptRef.setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener() {
                                
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    
                                    if (databaseError != null) {                                                                               
                                        JOptionPane.showMessageDialog(null, "Error saving transaction: " + databaseError.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                    
                                    } else {
                                        // Decrement the product quantities
                                        for (DataSnapshot itemSnapshot : dataSnapshot.child("items").getChildren()) {
                                            String productBarcode = itemSnapshot.child("productBarcode").getValue(String.class);
                                            Long purchasedQty = itemSnapshot.child("productQty").getValue(Long.class);

                                            // Fetch the product data to update its quantity
                                            productsRef.child(productBarcode).addListenerForSingleValueEvent(new ValueEventListener() {
                                                
                                                @Override
                                                public void onDataChange(DataSnapshot productSnapshot) {
                                                    if (productSnapshot.exists()) {
                                                        Long currentQty = productSnapshot.child("qty").getValue(Long.class);
                                                        
                                                        if (currentQty != null && purchasedQty != null) {
                                                            // Decrement the quantity
                                                            long newQty = currentQty - purchasedQty;
                                                            
                                                            productsRef.child(productBarcode).child("qty").setValue(newQty, new DatabaseReference.CompletionListener(){
                                                                @Override
                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference){
                                                                    if (databaseError == null) {
                                                                        System.out.println("Successfully updated quantity for " + productBarcode);
                                                                    } else {
                                                                        System.err.println("Failed to update quantity for " + productBarcode + ": " + databaseError.getMessage());
                                                                    }
                                                                }
                                                                
                                                            });
                                                            
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    System.err.println("Error fetching product data: " + databaseError.getMessage());
                                                }
                                            });
                                        }

                                        JOptionPane.showMessageDialog(null, "Transaction completed successfully!");

                                        // Reset the table and UI components
                                        DefaultTableModel model = (DefaultTableModel) itemTable.getModel();
                                        model.setRowCount(0); 
                                        payid_text.setText("");
                                        product_text.setText("");
                                        price_text.setText("");
                                        qty_field.setText("");
                                        total_field.setText("");
                                        cash_field.setText("");
                                        change_field.setText("");
                                        
                                        payid_text.requestFocusInWindow();
                                    }
                                }
                            });
                        }else{
                            JOptionPane.showMessageDialog(null, "No Items found to finish the transaction.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        JOptionPane.showMessageDialog(null, "Error accessing Firebase: " + databaseError.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        } else {
            // If the table is already empty
            JOptionPane.showMessageDialog(null, "The table is already empty!", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(null, "Please Enter a valid input", "Error", JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_finishTransactionMouseClicked

    private void addItem_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addItem_btnActionPerformed
        AddItemWindow addWindow = new AddItemWindow(this);
        addWindow.setVisible(true);
        addWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Add a WindowListener to detect when the second window is closed
        addWindow.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                fetchDataFromFirebase(payid_text.getText().trim());
            }
        });
    }//GEN-LAST:event_addItem_btnActionPerformed

    private void remove_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remove_btnActionPerformed
         try {
        // Get the selected row
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow != -1) {
            // Get the product name from the selected row
            String productName = itemTable.getValueAt(selectedRow, 0).toString();

            // Remove the selected row from the table
            DefaultTableModel model = (DefaultTableModel) itemTable.getModel();
            model.removeRow(selectedRow);

            // Remove the item from Firebase
            String payID = payid_text.getText();
            DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("payingSession/" + payID + "/items");

            // Query Firebase to find the selected item based on productName
            itemsRef.orderByChild("productName").equalTo(productName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean found = false;

                    for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                        String firebaseProductName = itemSnapshot.child("productName").getValue(String.class);

                        // If the product names match, remove it from Firebase
                        if (firebaseProductName != null && firebaseProductName.equals(productName)) {
                            found = true;

                            // Remove the item
                            itemSnapshot.getRef().removeValue(new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError != null) {
                                        JOptionPane.showMessageDialog(null, "Failed to remove item from Firebase: " + databaseError.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                    } else {
                                        JOptionPane.showMessageDialog(null, "Item removed successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                                    }
                                     updateTotalPrice(itemsRef);
                                    
                                }
                            });
                            break; // Exit loop once the item is found and removed
                        }
                    }

                    if (!found) {
                        JOptionPane.showMessageDialog(null, "Item not found in Firebase!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    
                   
                 
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    JOptionPane.showMessageDialog(null, "Failed to access Firebase: " + databaseError.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        } else {
            JOptionPane.showMessageDialog(this, "Please select a product to remove.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error removing item: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_remove_btnActionPerformed

    private void payid_textActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_payid_textActionPerformed
        
    }//GEN-LAST:event_payid_textActionPerformed

    private void updateQty_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateQty_btnActionPerformed
        updateQuantity();
    }//GEN-LAST:event_updateQty_btnActionPerformed

    private void change_fieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_change_fieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_change_fieldActionPerformed

    public static void main(String args[]) {
 
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new POSapp().setVisible(true);
            }
        });
        
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane JScrollPane;
    private javax.swing.JButton addItem_btn;
    private javax.swing.JTextField cash_field;
    private javax.swing.JTextField change_field;
    private javax.swing.JButton finishTransaction;
    private javax.swing.JTable itemTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel label_of_change;
    private javax.swing.JTextField payid_text;
    private javax.swing.JTextField price_text;
    private javax.swing.JTextField product_text;
    private javax.swing.JTextField qty_field;
    private javax.swing.JButton remove_btn;
    private javax.swing.JTextField total_field;
    private javax.swing.JLabel total_label;
    private javax.swing.JButton updateQty_btn;
    // End of variables declaration//GEN-END:variables
}
