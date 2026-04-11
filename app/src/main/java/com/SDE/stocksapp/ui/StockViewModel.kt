package com.SDE.stocksapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.SDE.stocksapp.StocksApplication
import com.SDE.stocksapp.models.DailyResponse
import com.SDE.stocksapp.models.GainerLoserApiResponse
import com.SDE.stocksapp.models.GlobalQuoteResponse
import com.SDE.stocksapp.models.IntradayResponse
import com.SDE.stocksapp.models.Stock
import com.SDE.stocksapp.models.StockDetailsResponse
import com.SDE.stocksapp.models.Watchlist
import com.SDE.stocksapp.models.WatchlistStockCrossRef
import com.SDE.stocksapp.models.WeeklyResponse
import com.SDE.stocksapp.repository.StockRepository
import com.SDE.stocksapp.util.Constants
import com.SDE.stocksapp.util.Resource
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class StockViewModel(
    app: Application,
    val repository: StockRepository
) : AndroidViewModel(app) {

    val topGainersLosers: MutableLiveData<Resource<GainerLoserApiResponse>> = MutableLiveData()
    val stockDetails: MutableLiveData<Resource<StockDetailsResponse>> = MutableLiveData()
    val watchlists: LiveData<List<Watchlist>> = getAllWatchlists()
    private val _chartData = MutableLiveData<List<Entry>>()
    val chartData: LiveData<List<Entry>> = _chartData

    private val _search = MutableStateFlow("")
    val searchQuery = _search.asStateFlow()

    val searchResult: MutableLiveData<Resource<GlobalQuoteResponse>> = MutableLiveData()

    init {
        getTopGainersLosers()
        observeSearchQuery()
    }

    private fun observeSearchQuery() {
        _search
            .debounce(500L)
            .distinctUntilChanged()
            .filter { it.isNotBlank() }
            .onEach { query ->
                searchResult.postValue(Resource.Loading())
                safeGetGlobalQuoteCall(query)
            }
            .launchIn(viewModelScope)
    }

    fun searchStock(query: String) {
        _search.value = query
    }

    private suspend fun safeGetGlobalQuoteCall(symbol: String) {
        try {
            if (hasInternetConnection()) {
                val response = repository.getGlobalQuote(symbol)
                searchResult.postValue(handleGlobalQuoteResponse(response))
            } else {
                searchResult.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchResult.postValue(Resource.Error("Network Failure"))
                else -> searchResult.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private fun handleGlobalQuoteResponse(response: Response<GlobalQuoteResponse>) : Resource<GlobalQuoteResponse> {
        if(response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun getTopGainersLosers() = viewModelScope.launch {
        topGainersLosers.postValue(Resource.Loading())
        safeGetTopGainersLosersCall()
    }

    fun getStockDetails(symbol: String) = viewModelScope.launch {
        stockDetails.postValue(Resource.Loading())
        safeGetStockDetailsCall(symbol)
    }

    private fun handleTopGainersLosersResponse(response: Response<GainerLoserApiResponse>) : Resource<GainerLoserApiResponse> {
        if(response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        Log.e("ViewModel", "API response body is null despite successful HTTP status. Actual response: ${response.errorBody()?.string() ?: response.raw().toString()}")
        return Resource.Error(response.message())
    }

    private fun handleStockDetailsResponse(response: Response<StockDetailsResponse>) : Resource<StockDetailsResponse> {
        if(response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private suspend fun safeGetTopGainersLosersCall() {
        topGainersLosers.postValue(Resource.Loading())
        try {
            if(hasInternetConnection()) {
                val response = repository.getTopGainersLosers()
                topGainersLosers.postValue(handleTopGainersLosersResponse(response))
            } else {
                topGainersLosers.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when(t) {
                is IOException -> topGainersLosers.postValue(Resource.Error("Network Failure"))
                else -> topGainersLosers.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private suspend fun safeGetStockDetailsCall(symbol: String) {
        stockDetails.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = repository.getStockDetails(symbol)
                stockDetails.postValue(handleStockDetailsResponse(response))
            } else {
                stockDetails.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> stockDetails.postValue(Resource.Error("Network Failure"))
                else -> stockDetails.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private fun hasInternetConnection() : Boolean {
        val connectivityManager = getApplication<StocksApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }

    fun saveStockIntoWatchlists(stock: Stock, watchlists: List<String>) = viewModelScope.launch {
        repository.insertStock(stock)
        watchlists.forEach { watchlistName ->
            repository.insertWatchlistStockCrossRef(WatchlistStockCrossRef(watchlistName, stock.ticker))
        }
    }

    fun saveWatchlist(watchlistName: String) = viewModelScope.launch {
        repository.insertWatchlist(Watchlist(watchlistName))
    }

    fun getAllWatchlists() = repository.getWatchlists()

    fun getStocksForWatchlist(watchlistName: String) = repository.getStocksForWatchlist(watchlistName)

    fun deleteWatchlist(watchlist: Watchlist) = viewModelScope.launch {
        repository.deleteWatchlist(watchlist)
    }

    fun deleteStockFromWatchlist(stock: Stock, watchlistName: String) = viewModelScope.launch {
        repository.deleteStockWatchlists(stock.ticker, watchlistName)
    }

    fun fetchTimeSeries(symbol: String, period: Constants.Companion.TimePeriod) = viewModelScope.launch {
        val resp = when (period) {
            Constants.Companion.TimePeriod.ONE_DAY -> repository.getIntraday(symbol)
            Constants.Companion.TimePeriod.ONE_WEEK, Constants.Companion.TimePeriod.ONE_MONTH,
            Constants.Companion.TimePeriod.THREE_MONTHS, Constants.Companion.TimePeriod.SIX_MONTHS -> repository.getDaily(symbol)
            Constants.Companion.TimePeriod.ONE_YEAR -> repository.getWeekly(symbol)
        }
        if (resp.isSuccessful) {
            val entries = when (val body = resp.body()) {
                is IntradayResponse -> {
                    body.timeSeries.entries
                        .sortedBy { it.key }
                        .mapIndexed { idx, (_, candle) ->
                            Entry(idx.toFloat(), candle.close.toFloat())
                        }
                }
                is DailyResponse -> {
                    body.timeSeries.entries
                        .sortedBy { it.key }
                        .mapIndexed { idx, (_, candle) ->
                            Entry(idx.toFloat(), candle.close.toFloat())
                        }
                }
                is WeeklyResponse -> {
                    body.timeSeries.entries
                        .sortedBy { it.key }
                        .mapIndexed { idx, (_, candle) ->
                            Entry(idx.toFloat(), candle.close.toFloat())
                        }
                }
                else -> emptyList()
            }
            _chartData.postValue(entries)
        } else {
            Log.e("VM", "TS error: ${resp.errorBody()?.string()}")
        }
    }
}