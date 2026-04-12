package com.SDE.stocksapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.SDE.stocksapp.R
import com.SDE.stocksapp.adapters.GenericStockAdapter
import com.SDE.stocksapp.databinding.FragmentTopLosersBinding
import com.SDE.stocksapp.models.Stock
import com.SDE.stocksapp.ui.StockViewModel
import com.SDE.stocksapp.ui.StocksActivity
import com.SDE.stocksapp.util.Resource

class TopLosersFragment : Fragment(R.layout.fragment_top_losers) {

    lateinit var binding: FragmentTopLosersBinding
    lateinit var viewModel: StockViewModel
    lateinit var stockAdapter: GenericStockAdapter

    private val TAG = "topLosersFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTopLosersBinding.bind(view)
        viewModel = (activity as StocksActivity).viewModel

        setupRecyclerView()

        var losers: MutableList<Stock>

        viewModel.topGainersLosers.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { gainersLosersResponse ->
                        if (!gainersLosersResponse.top_losers.isNullOrEmpty()) {
                            losers = gainersLosersResponse.top_losers.map { topLoser ->
                                Stock(
                                    ticker = topLoser.ticker,
                                    change_amount = topLoser.change_amount,
                                    change_percentage = topLoser.change_percentage,
                                    price = topLoser.price,
                                    volume = topLoser.volume,
                                )
                            } as MutableList<Stock>
                            stockAdapter.differ.submitList(losers)
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
            val action = TopLosersFragmentDirections.actionTopLosersFragmentToDetailsFragment(it)
            view.findNavController().navigate(action)
        }
    }

    fun setupRecyclerView() {
        stockAdapter = GenericStockAdapter()
        binding.rvTopLosers.apply {
            adapter = stockAdapter
            setHasFixedSize(true)
        }
    }

}