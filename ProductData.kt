package com.example.negeshoca

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductData(
    val productBarcode: String = "",
    val productName: String = "",
    val productPrice: Double = 0.00,
    val qty : Int = 1
): Parcelable


