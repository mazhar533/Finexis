package com.mazhar.finexis.model

import com.google.firebase.firestore.PropertyName

data class Expense(
    val id: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val date: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Cash",
    val description: String = "",
    val userId: String = "",
    @get:PropertyName("income")
    @PropertyName("income")
    val isIncome: Boolean = false
)
