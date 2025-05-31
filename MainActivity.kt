package com.example.negeshoca

import android.content.Intent
import android.widget.Toast
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemAdapter
    private lateinit var items: MutableList<ProductData>
    private lateinit var database: DatabaseReference
    private lateinit var database2: DatabaseReference
    private lateinit var totalTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_main)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        items = mutableListOf()
        adapter = ItemAdapter(items, this, isEditable = true)
        recyclerView.adapter = adapter

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference("scannedItems")
        database2 = FirebaseDatabase.getInstance().getReference("isProductExisting")

        // Fetch product data from Firebase
        fetchScannedItems()

        isProductExist()

        // checkout button
        val checkoutButton: Button = findViewById(R.id.checkout_btn)
        checkoutButton.setOnClickListener {
            if (items.isEmpty()) {
                Toast.makeText(this, "No items to checkout!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Pass the current list of items to the ReviewOrderActivity
            val intent = Intent(this, ReviewOrder::class.java)
            intent.putParcelableArrayListExtra("reviewItems", ArrayList(items))
            startActivity(intent)
        }

        // help Button
        val helpButton: Button = findViewById(R.id.helpButton)
        helpButton.setOnClickListener {
            // Show a confirmation dialog
            AlertDialog.Builder(this)
                .setTitle("Request Assistance")
                .setMessage("Please confirm if you need an assistance")
                .setPositiveButton("Confirm") { _, _ ->
                    // Proceed to request assistance
                    val assistanceRef = FirebaseDatabase.getInstance().getReference("assistanceRequests/pos1")
                    val assistanceData = mapOf(
                        "requested" to true,
                        "message" to "Customer needs help at aisle 3"
                    )

                    assistanceRef.setValue(assistanceData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Assistance requested successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // Dismiss the dialog
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        val backButton: ImageView = findViewById(R.id.backToStart)
        backButton.setOnClickListener(){
            // Show a confirmation dialog
            AlertDialog.Builder(this)
                .setTitle("Go to Start Page")
                .setMessage("Please confirm if you want to go back")
                .setPositiveButton("Confirm") { _, _ ->
                    // Proceed to request assistance
                    val databaseRef = FirebaseDatabase.getInstance().getReference("scannedItems")
                    databaseRef.removeValue() // Deletes all items in the "scannedItems" node

                        .addOnSuccessListener{
                            // Navigate to StartUp Activity
                            val intent = Intent(this, StartUp::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) // Clears activity stack
                            startActivity(intent)
                            finish() // Ends current activity
                    }
                        .addOnFailureListener{ error ->
                            // Handle error if resetting scanned items fails
                            Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss() // Dismiss the dialog if the user cancels
                }
                .create()
                .show()
        }

        // Display the total
        totalTextView = findViewById(R.id.txtview_Total)

        // Fullscreen Mode
        enableFullscreenMode()

    }



// ------------------------------------------------(functions)-----------------------------------------------------------------

    fun updateTotal(){
        val total = adapter.calculateTotal()
        totalTextView.text = "₱%.2f".format(total)
    }

    private fun fetchScannedItems() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                // Temporary list to hold fetched items
                val newItems = mutableListOf<ProductData>()

                // Loop through snapshot children to fetch product data
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(ProductData::class.java)
                    product?.let { newItems.add(it) }  // Add fetched product to the list
                }

                // If there are new items, we need to update the adapter
                val addedItems = mutableListOf<ProductData>()

                for(newItem in newItems){
                    // If the item is not already in the list, add it at the top
                    if(!items.any { it.productBarcode == newItem.productBarcode }){
                        items.add(0, newItem)  // Add new item to the top
                        addedItems.add(newItem) // Keep track of added items
                    }
                }

                // Notify the adapter about new item insertions (if any)
                if (addedItems.isNotEmpty()) {
                    adapter.notifyItemRangeInserted(0, addedItems.size)
                    recyclerView.scrollToPosition(0)  // Scroll to the top
                }

                Log.d("MainActivity", "Fetched items: $items")

                updateTotal()

                toggleRecyclerViewVisibility()

            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MainActivity", "loadPost:onCancelled", error.toException())
            }
        })
    }

    private fun toggleRecyclerViewVisibility() {
        if (items.isEmpty()) {
            recyclerView.visibility = View.GONE
            Toast.makeText(this@MainActivity, "Your cart is empty", Toast.LENGTH_SHORT).show()
        } else {
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun isProductExist(){
        val productFlag: Boolean = true

        database2.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isProductInFirebase = snapshot.child("isExisting").getValue(Boolean::class.java)

                if(isProductInFirebase != productFlag){
                    Toast.makeText(this@MainActivity, "Product doesn't exist", Toast.LENGTH_SHORT).show()

                    database2.child("isExisting").setValue(true)
                        .addOnCompleteListener{ task ->
                            if (task.isSuccessful) {
                                Log.d("MainActivity", "Flag were set back to true")
                            } else {
                                Log.d("MainActivity", "Failed setting back to true")
                            }

                        }

                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MainActivity", "loadPost:onCancelled", error.toException())
            }
        })
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



