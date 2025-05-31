package com.example.negeshoca

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.TextView
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream
import android.graphics.Color
import android.view.View
import java.util.*

class ProceedPayment : AppCompatActivity() {

    private lateinit var payItems: MutableList<ProductData>
    private lateinit var qrCodeImageView: ImageView
    private lateinit var payingIDTextView: TextView
    private lateinit var database: FirebaseDatabase
    private lateinit var payingSessionRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.paying_main)

        // Fullscreen mode
        enableFullscreenMode()

        // Initialize Firebase
        database = FirebaseDatabase.getInstance() // This must happen before using `database`
        payingSessionRef = database.getReference("payingSession")

        // Retrieve data passed from ReviewOrder
        val passedItems: ArrayList<ProductData>? = intent.getParcelableArrayListExtra("reviewItems")
        val total = intent.getDoubleExtra("total", 0.0)

        // Validate and initialize data
        if (passedItems.isNullOrEmpty()) {
            Toast.makeText(this, "No items to process", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        qrCodeImageView = findViewById(R.id.qrImage)
        payingIDTextView = findViewById(R.id.txtView_payingID)

        // Retrieve passed items
        payItems = passedItems.toMutableList()

        // Generate unique Payment ID using UUID
        val payingID = generatePaymentID()

        // Generate QR Code
        val qrCodeByteArray = generateQRCode("$payingID")

        if (qrCodeByteArray.isNotEmpty()) {
            // Display QR Code in ImageView
            qrCodeImageView.setImageBitmap(BitmapFactory.decodeByteArray(qrCodeByteArray, 0, qrCodeByteArray.size))

        } else {
            Toast.makeText(this, "Failed to generate QR Code", Toast.LENGTH_SHORT).show()
        }

        // Save data to Firebase
        saveItemsToFirebase(payingID, payItems, total)

        // Display the Payment ID
        payingIDTextView.text = payingID

        // Listen for transaction completion
        listenForTransactionCompletion()

    }

    // -------------------(functions)----------------------------------------------------------------------

    // Listen for transaction completion
    private fun listenForTransactionCompletion() {
        val scannedItemsRef = database.getReference("payingSession")

        // Add a listener to check if both `payingSession` and `scannedItems` are deleted
        payingSessionRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    // Check if scannedItems is also deleted
                    scannedItemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                // Both nodes are deleted; navigate to StartUp activity
                                navigateToStartUp()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@ProceedPayment, "Error checking transaction status", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProceedPayment, "Error checking transaction status", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToStartUp(){
        val intent = Intent(this@ProceedPayment, StartUp::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish() // Close the current activity
    }

    // Generate Payment ID using UUID
    private fun generatePaymentID(): String {
        val digits = (1..5).map { (0..9).random() }.joinToString("") // Generate 5 random digits
        val letters = (1..5).map { ('A'..'Z').random() }.joinToString("") // Generate 5 random uppercase letters
        return "PYD-$digits-$letters"
    }

    // Generate QR Code and convert to ByteArray
    private fun generateQRCode(data: String): ByteArray {
        return try {
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 400, 400)
            val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.RGB_565)

            for (x in 0 until 400) {
                for (y in 0 until 400) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            // Convert Bitmap to ByteArray
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            byteArrayOutputStream.toByteArray()
        } catch (e: WriterException) {
            e.printStackTrace()
            ByteArray(0) // Return empty array if an error occurs
        }
    }

    // Save Items to Firebase
    private fun saveItemsToFirebase(payingID: String, items: List<ProductData>, total: Double) {
        val database = FirebaseDatabase.getInstance()
        val payingSessionRef = database.getReference("payingSession")

        // Prepare the data to save
        val paymentData = mapOf(
            "items" to items.map { item ->
                mapOf(
                    "productBarcode" to item.productBarcode,
                    "productName" to item.productName,
                    "productPrice" to item.productPrice,
                    "productQty" to item.qty,
                    "totalPricePerItem" to item.productPrice * item.qty
                )
            },
            "total" to total
        )

        // Save to Firebase
        payingSessionRef.child(payingID).setValue(paymentData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Proceed to Cashier", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Retry", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // for fullscreen mode
    private fun enableFullscreenMode(){
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // Keeps immersive mode on
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Hides navigation bar
                        or View.SYSTEM_UI_FLAG_FULLSCREEN // Hides status bar
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableFullscreenMode()
        }
    }
}
