package com.SDE.stocksapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
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
            .observe(viewLifecycleOwner, Observer { stocks ->
                stocks.forEach { stock ->
                    stockAdapter.differ.submitList(stock.stocks)
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
    }

    fun setupRecyclerView() {

        stockAdapter = GenericStockAdapter(
            showDeleteButton = true
        ) { stock ->

            viewModel.deleteStockFromWatchlist(stock, args.watchlistName)

            Snackbar.make(requireView(), "Stock removed", Snackbar.LENGTH_LONG)
                .setAction("Undo") {
                    viewModel.saveStockIntoWatchlists(
                        stock,
                        listOf(args.watchlistName)
                    )
                }
                .show()
        }

        binding.rvWatchlistStocks.apply {
            adapter = stockAdapter
            setHasFixedSize(true)
        }
    }
}