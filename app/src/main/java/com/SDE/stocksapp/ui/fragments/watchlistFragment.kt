package com.SDE.stocksapp.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.SDE.stocksapp.R
import com.SDE.stocksapp.adapters.WatchlistAdapter
import com.SDE.stocksapp.databinding.FragmentWatchlistBinding
import com.SDE.stocksapp.models.WatchlistWithStock
import com.SDE.stocksapp.ui.StockViewModel
import com.SDE.stocksapp.ui.StocksActivity
import com.google.android.material.snackbar.Snackbar

class watchlistFragment : Fragment(R.layout.fragment_watchlist) {

    lateinit var binding: FragmentWatchlistBinding
    lateinit var viewModel: StockViewModel
    lateinit var watchlistAdapter: WatchlistAdapter

    private val TAG = "watchlistFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentWatchlistBinding.bind(view)
        viewModel = (activity as StocksActivity).viewModel

        setupRecyclerView()

        watchlistAdapter.setOnItemClickListener {
            view.findNavController().navigate(
                R.id.action_watchlistFragment_to_watchlistStocksFragment,
                Bundle().apply {
                    putString("watchlistName", it.watchlistName)
                }
            )
        }

        watchlistAdapter.setOnDeleteClickListener { watchlist, _ ->

            val stocks: LiveData<List<WatchlistWithStock>> =
                viewModel.getStocksForWatchlist(watchlist.watchlistName)

            viewModel.deleteWatchlist(watchlist)

            Snackbar.make(view, "Deleted watchlist", Snackbar.LENGTH_LONG).apply {
                setAction("Undo") {
                    viewModel.saveWatchlist(watchlist.watchlistName)

                    stocks.observe(viewLifecycleOwner, Observer { list ->
                        list.forEach { item ->
                            item.stocks.forEach { stock ->
                                viewModel.saveStockIntoWatchlists(
                                    stock,
                                    listOf(watchlist.watchlistName)
                                )
                            }
                        }
                    })
                }
                show()
            }
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            @SuppressLint("ShowToast")
            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                val position = viewHolder.adapterPosition
                val watchlist = watchlistAdapter.differ.currentList[position]

                val stocks: LiveData<List<WatchlistWithStock>> =
                    viewModel.getStocksForWatchlist(watchlist.watchlistName)

                viewModel.deleteWatchlist(watchlist)

                Snackbar.make(view, "Deleted watchlist", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        viewModel.saveWatchlist(watchlist.watchlistName)

                        stocks.observe(viewLifecycleOwner, Observer { list ->
                            list.forEach { item ->
                                item.stocks.forEach { stock ->
                                    viewModel.saveStockIntoWatchlists(
                                        stock,
                                        listOf(watchlist.watchlistName)
                                    )
                                }
                            }
                        })
                    }
                    show()
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback)
            .attachToRecyclerView(binding.rvWatchlist)

        viewModel.getAllWatchlists().observe(viewLifecycleOwner, Observer { watchlists ->
            watchlistAdapter.differ.submitList(watchlists)

            if (watchlists.isEmpty()){
                binding.emptyData.visibility = View.VISIBLE
                binding.rvWatchlist.visibility = View.GONE
            }
            else{
                binding.emptyData.visibility = View.GONE
                binding.rvWatchlist.visibility = View.VISIBLE
            }
        })
    }

    private fun setupRecyclerView() {
        watchlistAdapter = WatchlistAdapter()
        binding.rvWatchlist.apply {
            adapter = watchlistAdapter
            setHasFixedSize(true)
        }
    }
}