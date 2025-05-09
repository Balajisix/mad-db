package com.example.dbapp

import android.os.Bundle
import android.view.LayoutInflater
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dbapp.databinding.ActivityMainBinding
import com.example.dbapp.databinding.DialogAddExpenseBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : AppCompatActivity(), ExpenseAdapter.OnItemClick {

    private lateinit var binding: ActivityMainBinding
    private val db = FirebaseFirestore.getInstance()
    private val colRef = db.collection("expenses")
    private val expenseList = mutableListOf<Expense>()
    private lateinit var adapter: ExpenseAdapter
    private var listenerReg: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)

        adapter = ExpenseAdapter(expenseList, this)
        binding.rvExpenses.layoutManager = LinearLayoutManager(this)
        binding.rvExpenses.adapter = adapter

        binding.fabAdd.setOnClickListener { showAddDialog() }
        fetchExpenses()
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerReg?.remove()
    }

    private fun fetchExpenses() {
        listenerReg = colRef.addSnapshotListener { snaps, err ->
            if (err != null) {
                Log.e("FB_FIRE", "Snapshot listener failed", err)
                return@addSnapshotListener
            }
            expenseList.clear()
            snaps?.documents?.forEach { doc ->
                doc.toObject(Expense::class.java)?.apply {
                    id = doc.id
                    expenseList.add(this)
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun showAddDialog(expense: Expense? = null) {
        val bind = DialogAddExpenseBinding.inflate(LayoutInflater.from(this))
        AlertDialog.Builder(this)
            .setTitle(if (expense == null) "Add Expense" else "Edit Expense")
            .setView(bind.root)
            .setPositiveButton("Save") { _, _ ->
                val title = bind.etTitle.text.toString()
                val amount = bind.etAmount.text.toString().toDoubleOrNull() ?: 0.0
                val date = bind.etDate.text.toString()

                if (expense == null) {
                    colRef.add(Expense("", title, amount, date))
                        .addOnSuccessListener { docRef ->
                            Log.d("FB_FIRE", "Created ID=${docRef.id}")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FB_FIRE", "Create failed", e)
                        }
                } else {
                    colRef.document(expense.id)
                        .set(Expense(expense.id, title, amount, date))
                        .addOnSuccessListener {
                            Log.d("FB_FIRE", "Updated ${expense.id}")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FB_FIRE", "Update failed", e)
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onEdit(expense: Expense) = showAddDialog(expense)

    override fun onDelete(expense: Expense) {
        colRef.document(expense.id)
            .delete()
            .addOnSuccessListener {
                Log.d("FB_FIRE", "Deleted ${expense.id}")
            }
            .addOnFailureListener { e ->
                Log.e("FB_FIRE", "Delete failed", e)
            }
    }
}
