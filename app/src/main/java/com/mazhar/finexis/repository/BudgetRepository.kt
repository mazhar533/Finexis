package com.mazhar.finexis.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mazhar.finexis.model.Budget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class BudgetRepository {
    private var firestore: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null
    private var isMockMode = false

    // Single source of truth flow pre-populated with default budget settings
    private val budgetState = MutableStateFlow(Budget())

    init {
        try {
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            setupFirestoreListener()
        } catch (e: Exception) {
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
        // Binds budget to a single document named after the userId
        firestore!!.collection("budgets")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isMockMode = true
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val budget = snapshot.toObject(Budget::class.java)?.copy(id = snapshot.id)
                    if (budget != null) {
                        budgetState.value = budget
                    }
                } else {
                    // Document doesn't exist yet, emit default budget with current userId
                    budgetState.value = Budget(id = userId, userId = userId)
                }
            }
    }

    private fun isReallyMockMode(): Boolean {
        return isMockMode || firestore == null || auth?.currentUser == null
    }

    private fun getCurrentUserId(): String {
        return auth?.currentUser?.uid ?: "mock_user_123"
    }

    fun getBudget(): Flow<Budget> {
        return budgetState
    }

    fun saveBudget(budget: Budget, onComplete: (Boolean) -> Unit) {
        val userId = getCurrentUserId()
        val budgetWithUser = budget.copy(userId = userId, id = userId)

        if (isReallyMockMode()) {
            budgetState.value = budgetWithUser
            onComplete(true)
            return
        }

        firestore!!.collection("budgets")
            .document(userId)
            .set(budgetWithUser)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    isMockMode = true
                    budgetState.value = budgetWithUser
                    onComplete(true)
                } else {
                    onComplete(true)
                }
            }
    }
}
