package com.mazhar.finexis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mazhar.finexis.model.Expense
import com.mazhar.finexis.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel : ViewModel() {
    private val repository = ExpenseRepository()

    val expenses: StateFlow<List<Expense>> = repository.getExpenses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addExpense(
        amount: Double,
        category: String,
        date: Long,
        paymentMethod: String,
        description: String,
        isIncome: Boolean = false,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val expense = Expense(
            amount = amount,
            category = category,
            date = date,
            paymentMethod = paymentMethod,
            description = description,
            isIncome = isIncome
        )
        viewModelScope.launch {
            repository.addExpense(expense, onComplete)
        }
    }

    fun editExpense(
        id: String,
        amount: Double,
        category: String,
        date: Long,
        paymentMethod: String,
        description: String,
        isIncome: Boolean = false,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val expense = Expense(
            id = id,
            amount = amount,
            category = category,
            date = date,
            paymentMethod = paymentMethod,
            description = description,
            isIncome = isIncome
        )
        viewModelScope.launch {
            repository.editExpense(expense, onComplete)
        }
    }

    fun deleteExpense(id: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteExpense(id, onComplete)
        }
    }
}
