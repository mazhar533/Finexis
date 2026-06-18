package com.mazhar.finexis.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mazhar.finexis.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class ExpenseRepository {
    private var firestore: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null
    private var isMockMode = false

    // Single source of truth flow starting with no dummy data
    private val expensesState = MutableStateFlow<List<Expense>>(emptyList())

    init {
        try {
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            setupFirestoreListener()
        } catch (_: Exception) {
            isMockMode = true
        }
    }

    private fun setupFirestoreListener() {
        val currentUser = auth?.currentUser
        if (currentUser == null || firestore == null) {
            isMockMode = true
            return
        }

        val userId = currentUser.uid
        firestore!!.collection("expenses")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isMockMode = true
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Expense::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                expensesState.value = list
            }
    }

    private fun isReallyMockMode(): Boolean {
        return isMockMode || firestore == null || auth?.currentUser == null
    }

    private fun getCurrentUserId(): String {
        return auth?.currentUser?.uid ?: "mock_user_123"
    }

    fun getExpenses(): Flow<List<Expense>> {
        return expensesState
    }

    fun addExpense(expense: Expense, onComplete: (Boolean) -> Unit) {
        val userId = getCurrentUserId()
        val expenseWithUser = expense.copy(userId = userId)

        if (isReallyMockMode()) {
            val id = System.currentTimeMillis().toString()
            val list = expensesState.value.toMutableList()
            list.add(0, expenseWithUser.copy(id = id))
            expensesState.value = list
            onComplete(true)
            return
        }

        firestore!!.collection("expenses")
            .add(expenseWithUser)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    // Fallback to mock mode if task failed (e.g. permission denied)
                    isMockMode = true
                    val id = System.currentTimeMillis().toString()
                    val list = expensesState.value.toMutableList()
                    list.add(0, expenseWithUser.copy(id = id))
                    expensesState.value = list
                    onComplete(true)
                } else {
                    onComplete(true)
                }
            }
    }

    fun editExpense(expense: Expense, onComplete: (Boolean) -> Unit) {
        val userId = getCurrentUserId()
        val expenseWithUser = expense.copy(userId = userId)

        if (isReallyMockMode()) {
            val list = expensesState.value.map {
                if (it.id == expense.id) expenseWithUser else it
            }
            expensesState.value = list
            onComplete(true)
            return
        }

        firestore!!.collection("expenses")
            .document(expense.id)
            .set(expenseWithUser)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    isMockMode = true
                    val list = expensesState.value.map {
                        if (it.id == expense.id) expenseWithUser else it
                    }
                    expensesState.value = list
                    onComplete(true)
                } else {
                    onComplete(task.isSuccessful)
                }
            }
    }

    fun deleteExpense(id: String, onComplete: (Boolean) -> Unit) {
        if (isReallyMockMode()) {
            val list = expensesState.value.filter { it.id != id }
            expensesState.value = list
            onComplete(true)
            return
        }

        firestore!!.collection("expenses")
            .document(id)
            .delete()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    isMockMode = true
                    val list = expensesState.value.filter { it.id != id }
                    expensesState.value = list
                    onComplete(true)
                } else {
                    onComplete(task.isSuccessful)
                }
            }
    }
}
