package com.example.dbapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dbapp.databinding.ItemExpenseBinding

class ExpenseAdapter(
    private val items: MutableList<Expense>,
    private val listener: OnItemClick
) : RecyclerView.Adapter<ExpenseAdapter.VH>() {

    interface OnItemClick {
        fun onEdit(expense: Expense)
        fun onDelete(expense: Expense)
    }

    inner class VH(val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(exp: Expense) {
            binding.tvTitle.text = exp.title
            binding.tvAmount.text = "$${exp.amount}"
            binding.tvDate.text = exp.date

            binding.root.setOnClickListener { listener.onEdit(exp) }
            binding.root.setOnLongClickListener {
                listener.onDelete(exp)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}