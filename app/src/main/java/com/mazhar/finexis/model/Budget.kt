package com.mazhar.finexis.model

data class Budget(
    val id: String = "",
    val userId: String = "",
    val monthlyLimit: Double = 27800.0,
    val foodLimit: Double = 6950.0,
    val transportLimit: Double = 6950.0,
    val shoppingLimit: Double = 6950.0,
    val otherLimit: Double = 6950.0
)
