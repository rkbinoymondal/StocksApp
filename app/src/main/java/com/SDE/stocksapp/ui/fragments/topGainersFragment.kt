package com.SDE.stocksapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.SDE.stocksapp.R
import com.SDE.stocksapp.adapters.GenericStockAdapter
import com.SDE.stocksapp.databinding.FragmentTopGainersBinding
import com.SDE.stocksapp.models.Stock
import com.SDE.stocksapp.ui.StockViewModel
import com.SDE.stocksapp.ui.StocksActivity
import com.SDE.stocksapp.util.Resource

class TopGainersFragment : Fragment(R.layout.fragment_top_gainers) {

    lateinit var binding: FragmentTopGainersBinding
    lateinit var viewModel: StockViewModel
    lateinit var stockAdapter: GenericStockAdapter

    private val TAG = "topGainersFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTopGainersBinding.bind(view)
        viewModel = (activity as StocksActivity).viewModel

        setupRecyclerView()

        var gainers: MutableList<Stock>

        viewModel.topGainersLosers.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { gainersLosersResponse ->
                        if (gainersLosersResponse.top_gainers.isNotEmpty()) {
                            gainers = gainersLosersResponse.top_gainers.map { topGainer ->
                                Stock(
                                    ticker = topGainer.ticker,
                                    change_amount = topGainer.change_amount,
                                    change_percentage = topGainer.change_percentage,
                                    price = topGainer.price,
                                    volume = topGainer.volume,
//                                    urlToImage = null
                                )
                            } as MutableList<Stock>
                            stockAdapter.differ.submitList(gainers)
                        }
                    }
                }

                is Resource.Error -> {
                    response.message?.let { message ->
                        Log.e(TAG, "An error occurred: $message")
                    }
                }

                is Resource.Loading -> {
                    Log.d(TAG, "Loading...")
                }
            }
        })

        stockAdapter.setOnItemClickListener {
            val action = TopGainersFragmentDirections.actionTopGainersFragmentToDetailsFragment(it)
            view.findNavController().navigate(action)
        }
    }

    fun setupRecyclerView() {
        stockAdapter = GenericStockAdapter()
        binding.rvTopGainers.apply {
            adapter = stockAdapter
            setHasFixedSize(true)
        }
    }
}