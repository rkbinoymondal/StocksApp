package com.SDE.stocksapp.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.SDE.stocksapp.R
import com.SDE.stocksapp.adapters.GenericStockAdapter
import com.SDE.stocksapp.databinding.FragmentWatchlistStocksBinding
import com.SDE.stocksapp.ui.StockViewModel
import com.SDE.stocksapp.ui.StocksActivity
import com.google.android.material.snackbar.Snackbar

class watchlistStocksFragment : Fragment(R.layout.fragment_watchlist_stocks) {

    lateinit var binding: FragmentWatchlistStocksBinding
    lateinit var viewModel: StockViewModel
    lateinit var stockAdapter: GenericStockAdapter
    val args: watchlistStocksFragmentArgs by navArgs()

    private val TAG = "watchlistStocksFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentWatchlistStocksBinding.bind(view)
        viewModel = (activity as StocksActivity).viewModel

        binding.tvWatchlistName.text = args.watchlistName

        setupRecyclerView()

        viewModel.getStocksForWatchlist(args.watchlistName)
            .observe(viewLifecycleOwner, Observer { list ->
                list.forEach {
                    stockAdapter.differ.submitList(it.stocks)
                }
            })

        stockAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("stock", it)
            }
            view.findNavController().navigate(
                R.id.action_watchlistStocksFragment_to_detailsFragment,
                bundle
            )
        }

        stockAdapter.setOnDeleteClickListener { stock, _ ->

            viewModel.deleteStockFromWatchlist(stock, args.watchlistName)

            Snackbar.make(view, "Deleted from watchlist", Snackbar.LENGTH_LONG).apply {
                setAction("Undo") {
                    viewModel.saveStockIntoWatchlists(
                        stock,
                        listOf(args.watchlistName)
                    )
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

            @SuppressLint("NotifyDataSetChanged")
            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                val position = viewHolder.adapterPosition
                val stock = stockAdapter.differ.currentList[position]

                viewModel.deleteStockFromWatchlist(stock, args.watchlistName)

                Snackbar.make(view, "Deleted from watchlist", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        viewModel.saveStockIntoWatchlists(
                            stock,
                            listOf(args.watchlistName)
                        )
                    }
                    show()
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback)
            .attachToRecyclerView(binding.rvWatchlistStocks)
    }

    private fun setupRecyclerView() {
        stockAdapter = GenericStockAdapter()

        stockAdapter.showDeleteButton = true

        binding.rvWatchlistStocks.apply {
            adapter = stockAdapter
            setHasFixedSize(true)
        }
    }
}