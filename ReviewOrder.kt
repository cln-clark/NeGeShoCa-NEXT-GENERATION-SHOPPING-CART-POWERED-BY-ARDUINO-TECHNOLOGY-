package com.example.negeshoca

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ReviewOrder : AppCompatActivity() {

    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ItemAdapter
    private lateinit var reviewItems: MutableList<ProductData>
    private lateinit var reviewTotalTextView: TextView
    private lateinit var placeOrderButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.revieworder_main)

        // Initialize views
        reviewRecyclerView = findViewById(R.id.review_recyclerView)
        reviewTotalTextView = findViewById(R.id.txtView_Total)
        placeOrderButton = findViewById(R.id.placeOrder_btn)

        // Get the items passed from MainActivity
        reviewItems = intent.getParcelableArrayListExtra("reviewItems") ?: mutableListOf()

        // Set up RecyclerView
        reviewAdapter = ItemAdapter(reviewItems, this, isEditable = false)
        reviewRecyclerView.layoutManager = LinearLayoutManager(this)
        reviewRecyclerView.adapter = reviewAdapter



        // Find the back button ImageView
        val backButton: ImageView = findViewById(R.id.backToCheckout)

        // Set up click listener for back navigation
        backButton.setOnClickListener {
            // Go back to the ItemMain activity (or fragment)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()  // Optional: Finish the current activity to remove it from the back stack
        }

        // Calculate and display the total
        updateTotal()

        // Handle final checkout button
        placeOrderButton.setOnClickListener {
            Toast.makeText(this, "Proceeding to checkout...", Toast.LENGTH_SHORT).show()
            callProceedPayment()
        }

        // Fullscreen Mode
        enableFullscreenMode()

    }


    //---------------------------------------------------------------------------------(functions)-------------------------------------------------------------------

    private fun updateTotal() {
        val total = reviewAdapter.calculateTotal()
        reviewTotalTextView.text = "₱%.2f".format(total)
    }

    private fun callProceedPayment(){
        // Calculate total price
        val total = reviewAdapter.calculateTotal()

        // Start ProceedPayment activity
        val intent = Intent(this, ProceedPayment::class.java)
        intent.putParcelableArrayListExtra("reviewItems", ArrayList(reviewItems)) // Pass the list of items
        intent.putExtra("total", total) // Pass the total price
        startActivity(intent)
        finish() // Close ReviewOrder if necessary
    }

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




