package com.SDE.stocksapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.SDE.stocksapp.R
import com.SDE.stocksapp.adapters.WatchlistCheckboxAdapter
import com.SDE.stocksapp.databinding.FragmentDetailsBinding
import com.SDE.stocksapp.databinding.DialogAddToWatchlistBinding
import com.SDE.stocksapp.ui.StockViewModel
import com.SDE.stocksapp.ui.StocksActivity
import com.SDE.stocksapp.util.Constants
import com.SDE.stocksapp.util.Resource
import com.SDE.stocksapp.util.formatPercentage
import com.SDE.stocksapp.util.formatPrice
import com.SDE.stocksapp.ui.fragments.DetailsFragmentArgs

class DetailsFragment : Fragment(R.layout.fragment_details) {

    lateinit var binding : FragmentDetailsBinding
    lateinit var viewModel : StockViewModel
    val args : DetailsFragmentArgs by navArgs()

    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var dialogBinding: DialogAddToWatchlistBinding
    private val checkboxAdapter by lazy { WatchlistCheckboxAdapter() }

    private val TAG = "detailsFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetailsBinding.bind(view)
        viewModel = (activity as StocksActivity).viewModel
        val symbol = args.stock.ticker

        viewModel.getStockDetails(symbol)

        viewModel.stockDetails.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { stockDetailsResponse ->
                        if(stockDetailsResponse.Symbol != null) {
                            binding.apply {
                                tvCompanyName.text = stockDetailsResponse.Name
                                tvStockSymbol.text = stockDetailsResponse.Symbol
                                tvStockType.text = stockDetailsResponse.AssetType
                                tvExchange.text = stockDetailsResponse.Exchange
                                tvCurrentPrice.text = args.stock.price.formatPrice()
                                tvPriceChange.text = args.stock.change_percentage.formatPercentage()
                                tvAboutTitle.text = "About ${stockDetailsResponse.Name}"
                                tvAboutDescription.text = stockDetailsResponse.Description
                                tvIndustryTag.text = stockDetailsResponse.Industry
                                tvSectorTag.text = stockDetailsResponse.Sector
                                tvLowPrice.text = stockDetailsResponse.`52WeekLow`.formatPrice()
                                tvHighPrice.text = stockDetailsResponse.`52WeekHigh`.formatPrice()
                                tvCurrentPriceStat.text = "Current Price"
                                tvCurrentPriceValue.text = args.stock.price.formatPrice()
                                tvMarketCapValue.text = stockDetailsResponse.MarketCapitalization
                                tvPERatioValue.text = stockDetailsResponse.PERatio
                                tvBetaValue.text = stockDetailsResponse.Beta
                                tvDividendYieldValue.text = stockDetailsResponse.DividendYield.formatPercentage()
                                tvProfitMarginValue.text = stockDetailsResponse.ProfitMargin.formatPercentage()
                                
                                val changePercent = args.stock.change_percentage.replace("%", "").toDoubleOrNull() ?: 0.0
                                tvPriceChange.setTextColor(
                                    ContextCompat.getColor(requireContext(),
                                        if (changePercent >= 0) R.color.finance_positive else R.color.finance_negative)
                                )
                            }
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

        binding.ivBookmark.setOnClickListener {
            showAddToWatchlistSheet()
        }

        setupChartButtons()
        viewModel.chartData.observe(viewLifecycleOwner) { entries ->
            Log.d(TAG, "Observing ${entries.size} entries")
            drawChart(entries)
        }
    }

    private fun showAddToWatchlistSheet() {
        dialogBinding = DialogAddToWatchlistBinding.inflate(layoutInflater)
        bottomSheetDialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogBinding.root)
            (dialogBinding.root.parent as View).setBackgroundResource(R.drawable.bottom_dialog_background)
        }

        dialogBinding.rvExistingWatchlists.apply {
            adapter = checkboxAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.watchlists.observe(viewLifecycleOwner) { lists ->
            checkboxAdapter.differ.submitList(lists)
        }

        dialogBinding.btnAddWatchlist.setOnClickListener {
            val name = dialogBinding.etNewWatchlistName.text.toString().trim()
            if (name.isNotEmpty()) {
                viewModel.saveWatchlist(name)
                dialogBinding.etNewWatchlistName.text?.clear()
            } else {
                Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show()
            }
        }

        val selectedLists = mutableSetOf<String>()

        checkboxAdapter.setOnItemClickListener { watchlist ->
            if (selectedLists.contains(watchlist.watchlistName)) {
                selectedLists.remove(watchlist.watchlistName)
            } else {
                selectedLists.add(watchlist.watchlistName)
            }
        }

        bottomSheetDialog.show()

        dialogBinding.btnAddStock.setOnClickListener {
            if (selectedLists.isEmpty()) {
                Toast.makeText(requireContext(),
                    "Select at least one watchlist",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.saveStockIntoWatchlists(args.stock, selectedLists.toList())
            Toast.makeText(requireContext(),
                "${args.stock.ticker} added to ${selectedLists.joinToString()}",
                Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }
    }

    private fun setupChartButtons() = binding.run {
        btn1W.setOnClickListener { viewModel.fetchTimeSeries(args.stock.ticker, Constants.Companion.TimePeriod.ONE_DAY) }
        btn1M.setOnClickListener { viewModel.fetchTimeSeries(args.stock.ticker, Constants.Companion.TimePeriod.ONE_WEEK) }
        btn3M.setOnClickListener { viewModel.fetchTimeSeries(args.stock.ticker, Constants.Companion.TimePeriod.ONE_MONTH) }
        btn6M.setOnClickListener { viewModel.fetchTimeSeries(args.stock.ticker, Constants.Companion.TimePeriod.THREE_MONTHS) }
        btn1Y.setOnClickListener { viewModel.fetchTimeSeries(args.stock.ticker, Constants.Companion.TimePeriod.ONE_YEAR) }
    }

    private fun drawChart(entries: List<Entry>) = binding.lineChart.apply {
        clear()
        data?.clearValues()

        val dataSet = LineDataSet(entries, "Price")
            .apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                lineWidth = 2f
                setDrawCircles(false)
                setDrawValues(false)
            }

        // 3) Set up axes & disable description
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        axisRight.isEnabled = false
        description.isEnabled = false

        // 4) Assign data & refresh
        this.data = LineData(dataSet)
        animateX(500)
        invalidate()
    }

}