package com.mazhar.finexis.model

data class Budget(
    val id: String = "",
    val userId: String = "",
    val monthlyLimit: Double = 0.0,
    val foodLimit: Double = 0.0,
    val transportLimit: Double = 0.0,
    val shoppingLimit: Double = 0.0,
    val otherLimit: Double = 0.0
)
