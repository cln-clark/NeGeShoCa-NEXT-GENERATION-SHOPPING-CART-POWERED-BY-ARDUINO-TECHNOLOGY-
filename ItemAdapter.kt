package com.example.negeshoca

import android.content.Context
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.FirebaseDatabase

class ItemAdapter(private var items: MutableList<ProductData>,
                  private val context: Context,
                  private val isEditable: Boolean = true
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNameTextView: TextView = itemView.findViewById(R.id.txtView_itemName)
        val itemPriceTextView: TextView = itemView.findViewById(R.id.txtView_Price)
        val qtyTextView: TextView = itemView.findViewById(R.id.txtView_qty)

        val pricePerQty: TextView? = itemView.findViewById(R.id.txtView_totalPricePerItem)

        val deleteButton: ImageButton? = itemView.findViewById(R.id.btn_delete)
        val addQtyBtn: Button? = itemView.findViewById(R.id.add_btn)
        val minQtyBtn: Button? = itemView.findViewById(R.id.min_btn)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutID = if(isEditable) R.layout.item_layout else R.layout.revieworder_layout
        val view = LayoutInflater.from(parent.context).inflate(layoutID, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val product = items[position]
        holder.itemNameTextView.text = product.productName
        holder.itemPriceTextView.text = "₱${product.productPrice}"
        holder.qtyTextView.text = "${product.qty}"

        val totalPricePerItem = product.productPrice * product.qty


        if (!isEditable) {
            holder.deleteButton?.visibility = View.GONE
            holder.addQtyBtn?.visibility = View.GONE
            holder.minQtyBtn?.visibility = View.GONE
            holder.pricePerQty?.visibility = View.GONE
            // Only set the pricePerQty if it is non-null
            holder.pricePerQty?.apply {
                visibility = View.VISIBLE // Make it visible in the review layout
                text = "₱${totalPricePerItem}"
            }
        } else {
            // hide total price of each item per quantity
            holder.pricePerQty?.visibility = View.GONE
            // increment quantity
            holder.addQtyBtn?.setOnClickListener {
                val newQuantity = product.qty + 1
                updateQuantityInFirebase(
                    product.productBarcode,
                    newQuantity
                ) // Call the update function
            }
            // decrement quantity
            holder.minQtyBtn?.setOnClickListener {
                if (product.qty > 1) {
                    val newQuantity = product.qty - 1
                    updateQuantityInFirebase(product.productBarcode, newQuantity)
                } else {
                    Toast.makeText(context, "Quantity cannot be less than 1", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            // delete item in recyclerview
            holder.deleteButton?.setOnClickListener {

                AlertDialog.Builder(context)
                    .setMessage("Are you sure you want to remove this item?")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, id ->
                        // Ensure this position is valid before proceeding
                        if (position >= 0 && position < items.size) {
                            deleteItem(position)
                        }
                    }
                    .setNegativeButton("No") { dialog, id ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()

            }

        }
    }

    override fun getItemCount(): Int = items.size

// ------------------------------------------------( FUNCTIONS )----------------------------------------------------------------------------------

    // update quantity in firebase function
    private fun updateQuantityInFirebase(productBarcode: String, newQuantity: Int){
        val itemRef = FirebaseDatabase.getInstance().getReference("scannedItems").child(productBarcode)
        itemRef.child("qty").setValue(newQuantity)
            .addOnSuccessListener {
                Log.d("ItemAdapter", "Quantity updated successfully in Firebase")
                Toast.makeText(context, "Quantity updated to $newQuantity", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e("ItemAdapter", "Failed to update quantity: ${exception.message}")
                Toast.makeText(context, "Failed to update quantity", Toast.LENGTH_SHORT).show()
            }

    }

    // deleting item function
    private fun deleteItem(position: Int) {
        if (position < 0 || position >= items.size) return

        Log.d("ItemAdapter", "Before remove: ${items.size}, Position: $position")

        val product = items[position]
        val productBarcode = product.productBarcode

        // Remove the item from the list first, then notify the adapter
        items.removeAt(position)
        Log.d("ItemAdapter", "After remove: ${items.size}")

        // Now notify the RecyclerView that the item has been removed
        notifyItemRemoved(position)

        // Remove from Firebase separately after the RecyclerView update
        val itemRef = FirebaseDatabase.getInstance().getReference("scannedItems").child(productBarcode)

        itemRef.removeValue()
            .addOnSuccessListener {
                // Successful Firebase removal, if needed you can log success or update UI further.
                Log.d("ItemAdapter", "Item removed from Firebase successfully.")
            }
            .addOnFailureListener { exception ->
                // Handle any failure during Firebase removal.
                Log.e("ItemAdapter", "Failed to remove item from Firebase: ${exception.message}")
                Toast.makeText(context, "Failed to remove item from Firebase", Toast.LENGTH_SHORT).show()
            }

        (context as MainActivity).updateTotal()
    }

    // calculate total on quantity function
    fun calculateTotal(): Double {
        var total = 0.00
        for (item in items) {
            total += item.productPrice * item.qty
        }
        return total
    }

}
