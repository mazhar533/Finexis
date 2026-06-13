package com.mazhar.finexis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mazhar.finexis.model.Budget
import com.mazhar.finexis.repository.BudgetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BudgetViewModel : ViewModel() {
    private val repository = BudgetRepository()

    val budget: StateFlow<Budget> = repository.getBudget()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Budget()
        )

    fun saveBudget(
        monthlyLimit: Double,
        foodLimit: Double,
        transportLimit: Double,
        shoppingLimit: Double,
        otherLimit: Double,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val updatedBudget = Budget(
            monthlyLimit = monthlyLimit,
            foodLimit = foodLimit,
            transportLimit = transportLimit,
            shoppingLimit = shoppingLimit,
            otherLimit = otherLimit
        )
        viewModelScope.launch {
            repository.saveBudget(updatedBudget, onComplete)
        }
    }
}
