package com.SDE.stocksapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.SDE.stocksapp.R
import com.SDE.stocksapp.adapters.GenericStockAdapter
import com.SDE.stocksapp.databinding.FragmentHomeBinding
import com.SDE.stocksapp.models.Stock
import com.SDE.stocksapp.ui.StockViewModel
import com.SDE.stocksapp.ui.StocksActivity
import com.SDE.stocksapp.util.Resource

class HomeFragment : Fragment(R.layout.fragment_home) {

    lateinit var binding: FragmentHomeBinding
    lateinit var viewModel: StockViewModel
    lateinit var stockAdapterLoser: GenericStockAdapter
    lateinit var stockAdapterGainer: GenericStockAdapter

    private val TAG = "homeFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        viewModel = (activity as StocksActivity).viewModel

        setupRecyclerViewGainers()
        setupRecyclerViewLosers()
        setupSearchView()

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getTopGainersLosers()
        }

        viewModel.topGainersLosers.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    binding.swipeRefreshLayout.isRefreshing = false
                    response.data?.let { gainersLosersResponse ->
                        if (gainersLosersResponse.top_gainers.isNotEmpty()) {
                            val gainers = gainersLosersResponse.top_gainers.take(3).map { topGainer ->
                                Stock(
                                    ticker = topGainer.ticker,
                                    change_amount = topGainer.change_amount,
                                    change_percentage = topGainer.change_percentage,
                                    price = topGainer.price,
                                    volume = topGainer.volume
                                )
                            }
                            stockAdapterGainer.differ.submitList(gainers)
                        }
                        
                        if (gainersLosersResponse.top_losers.isNotEmpty()) {
                            val losers = gainersLosersResponse.top_losers.take(3).map { topLoser ->
                                Stock(
                                    ticker = topLoser.ticker,
                                    change_amount = topLoser.change_amount,
                                    change_percentage = topLoser.change_percentage,
                                    price = topLoser.price,
                                    volume = topLoser.volume
                                )
                            }
                            stockAdapterLoser.differ.submitList(losers)
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    binding.swipeRefreshLayout.isRefreshing = false
                    response.message?.let { message ->
                        Log.e(TAG, "An error occurred: $message")
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })

        viewModel.searchResult.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.globalQuote?.let { quote ->
                        if (quote.symbol != null) {
                            binding.cvSearchResult.visibility = View.VISIBLE
                            val stock = Stock(
                                ticker = quote.symbol,
                                price = quote.price ?: "0.0",
                                change_amount = quote.change ?: "0.0",
                                change_percentage = quote.changePercent ?: "0.0%",
                                volume = "0"
                            )
                            binding.searchResultItem.tvStockName.text = quote.symbol
                            binding.searchResultItem.tvCompanyName.text = "" // Global Quote doesn't have name
                            binding.searchResultItem.tvStockIconText.text = quote.symbol.take(1)
                            binding.searchResultItem.tvStockPrice.text = quote.price
                            binding.searchResultItem.tvStockChange.text = quote.changePercent
                            
                            binding.cvSearchResult.setOnClickListener {
                                val action = HomeFragmentDirections.actionHomeFragmentToDetailsFragment(stock)
                                view.findNavController().navigate(action)
                            }
                        } else {
                            binding.cvSearchResult.visibility = View.GONE
                        }
                    } ?: run {
                        binding.cvSearchResult.visibility = View.GONE
                    }
                }
                is Resource.Error -> {
                    binding.cvSearchResult.visibility = View.GONE
                }
                is Resource.Loading -> {
                    // Optionally show a small loader
                }
            }
        })

        binding.tvGainersViewAll.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToTopGainersFragment()
            view.findNavController().navigate(action)
        }
        binding.tvLosersViewAll.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToTopLosersFragment()
            view.findNavController().navigate(action)
        }
        
        stockAdapterGainer.setOnItemClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToDetailsFragment(it)
            view.findNavController().navigate(action)
        }
        
        stockAdapterLoser.setOnItemClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToDetailsFragment(it)
            view.findNavController().navigate(action)
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchStock(newText ?: "")
                if (newText.isNullOrBlank()) {
                    binding.cvSearchResult.visibility = View.GONE
                }
                return true
            }
        })
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        if (stockAdapterGainer.differ.currentList.isEmpty() && stockAdapterLoser.differ.currentList.isEmpty()) {
            binding.paginationProgressBar.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerViewGainers() {
        stockAdapterGainer = GenericStockAdapter()
        binding.rvGainersHome.apply {
            adapter = stockAdapterGainer
        }
    }

    private fun setupRecyclerViewLosers() {
        stockAdapterLoser = GenericStockAdapter()
        binding.rvLosersHome.apply {
            adapter = stockAdapterLoser
        }
    }
}