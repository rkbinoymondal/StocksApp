package com.SDE.stocksapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.SDE.stocksapp.databinding.WatchlistCardBinding
import com.SDE.stocksapp.models.Watchlist

class WatchlistAdapter :
    RecyclerView.Adapter<WatchlistAdapter.WatchlistViewHolder>() {

    inner class WatchlistViewHolder(val binding: WatchlistCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<Watchlist>() {
        override fun areItemsTheSame(oldItem: Watchlist, newItem: Watchlist): Boolean {
            return oldItem.watchlistName == newItem.watchlistName
        }

        override fun areContentsTheSame(oldItem: Watchlist, newItem: Watchlist): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchlistViewHolder {
        val binding = WatchlistCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WatchlistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WatchlistViewHolder, position: Int) {
        val watchlist = differ.currentList[position]
        val binding = holder.binding

        binding.tvWatchlistName.text = watchlist.watchlistName

        binding.root.setOnClickListener {
            onItemClickListener?.invoke(watchlist)
        }

        binding.btnDeleteWatchlist.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onDeleteClickListener?.invoke(watchlist, pos)
            }
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onItemClickListener: ((Watchlist) -> Unit)? = null
    fun setOnItemClickListener(listener: (Watchlist) -> Unit) {
        onItemClickListener = listener
    }

    private var onDeleteClickListener: ((Watchlist, Int) -> Unit)? = null
    fun setOnDeleteClickListener(listener: (Watchlist, Int) -> Unit) {
        onDeleteClickListener = listener
    }
}