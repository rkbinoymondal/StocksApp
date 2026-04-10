package com.SDE.stocksapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.SDE.stocksapp.R
import com.SDE.stocksapp.databinding.ItemStockCardBinding
import com.SDE.stocksapp.models.Stock
import com.SDE.stocksapp.util.formatPercentage
import com.SDE.stocksapp.util.formatPrice
import java.util.Locale

class GenericStockAdapter(
    private val showDeleteButton: Boolean = false,
    private val onDeleteClick: ((Stock) -> Unit)? = null
) : RecyclerView.Adapter<GenericStockAdapter.StockViewHolder>() {

    inner class StockViewHolder(val binding: ItemStockCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<Stock>() {
        override fun areItemsTheSame(oldItem: Stock, newItem: Stock): Boolean {
            return oldItem.ticker == newItem.ticker
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Stock, newItem: Stock): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val binding = ItemStockCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val stock = differ.currentList[position]

        holder.binding.apply {

            tvStockIconText.text = stock.ticker.take(1).uppercase(Locale.getDefault())
            tvStockName.text = stock.ticker
            tvCompanyName.text = stock.ticker

            tvStockPrice.text = stock.price.formatPrice()
            tvStockChange.text = stock.change_percentage.formatPercentage()

            val changePercent =
                stock.change_percentage.replace("%", "").toDoubleOrNull() ?: 0.0

            val colorRes = if (changePercent >= 0) {
                R.color.finance_positive
            } else {
                R.color.finance_negative
            }

            tvStockChange.setTextColor(ContextCompat.getColor(root.context, colorRes))

            // normal click
            root.setOnClickListener {
                onItemClickListener?.invoke(stock)
            }

            btnDeleteStock.visibility =
                if (showDeleteButton) View.VISIBLE else View.GONE

            //DELETE BUTTON FEATURE ADDED
            btnDeleteStock.setOnClickListener {
                onDeleteClick?.invoke(stock)
            }
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onItemClickListener: ((Stock) -> Unit)? = null

    fun setOnItemClickListener(listener: (Stock) -> Unit) {
        onItemClickListener = listener
    }
}