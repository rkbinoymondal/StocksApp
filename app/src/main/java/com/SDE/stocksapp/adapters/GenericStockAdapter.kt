package com.SDE.stocksapp.adapters

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

class GenericStockAdapter :
    RecyclerView.Adapter<GenericStockAdapter.StockViewHolder>() {

    inner class StockViewHolder(val binding: ItemStockCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    var showDeleteButton = false

    private val differCallback = object : DiffUtil.ItemCallback<Stock>() {
        override fun areItemsTheSame(oldItem: Stock, newItem: Stock): Boolean {
            return oldItem.ticker == newItem.ticker
        }

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
        val binding = holder.binding

        binding.tvStockIconText.text =
            stock.ticker.take(1).uppercase(Locale.getDefault())

        binding.tvStockName.text = stock.ticker
        binding.tvCompanyName.text = stock.ticker

        binding.tvStockPrice.text = stock.price.formatPrice()
        binding.tvStockChange.text = stock.change_percentage.formatPercentage()

        val changePercent =
            stock.change_percentage.replace("%", "").toDoubleOrNull() ?: 0.0

        val colorRes = if (changePercent >= 0) {
            R.color.finance_positive
        } else {
            R.color.finance_negative
        }

        binding.tvStockChange.setTextColor(
            ContextCompat.getColor(binding.root.context, colorRes)
        )

        binding.btnDeleteStock.visibility =
            if (showDeleteButton) View.VISIBLE else View.GONE

        binding.root.setOnClickListener {
            onItemClickListener?.invoke(stock)
        }

        binding.btnDeleteStock.setOnClickListener {
            val pos = holder.adapterPosition   // ✅ FIXED LINE
            if (pos != RecyclerView.NO_POSITION) {
                onDeleteClickListener?.invoke(stock, pos)
            }
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onItemClickListener: ((Stock) -> Unit)? = null

    fun setOnItemClickListener(listener: (Stock) -> Unit) {
        onItemClickListener = listener
    }

    private var onDeleteClickListener: ((Stock, Int) -> Unit)? = null

    fun setOnDeleteClickListener(listener: (Stock, Int) -> Unit) {
        onDeleteClickListener = listener
    }
}