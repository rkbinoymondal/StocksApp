package com.SDE.stocksapp.repository

import com.SDE.stocksapp.api.RetrofitInstance
import com.SDE.stocksapp.db.StockDatabase
import com.SDE.stocksapp.models.GlobalQuoteResponse
import com.SDE.stocksapp.models.Stock
import com.SDE.stocksapp.models.Watchlist
import com.SDE.stocksapp.models.WatchlistStockCrossRef
import retrofit2.Response

class StockRepository(
    private val db: StockDatabase
) {

    suspend fun insertStock(stock: Stock) = db.getStockDao().insertStock(stock)

    suspend fun insertWatchlist(watchlist: Watchlist) = db.getStockDao().insertWatchlist(watchlist)

    suspend fun insertWatchlistStockCrossRef(watchlistStockCrossRef: WatchlistStockCrossRef) =
        db.getStockDao().insertWatchlistStockCrossRef(watchlistStockCrossRef)

    fun getWatchlistsForStock(ticker: String) = db.getStockDao().getWatchlistsForStock(ticker)

    fun getStocksForWatchlist(watchlistName: String) =
        db.getStockDao().getStocksForWatchlist(watchlistName)

    suspend fun deleteWatchlist(watchlist: Watchlist){
        db.getStockDao().deleteWatchlist(watchlist)
        db.getStockDao().deleteWatchlistStocks(watchlist.watchlistName)
    }

    suspend fun deleteStock(stock: Stock) {
        db.getStockDao().deleteStock(stock)
        db.getStockDao().deleteStockWatchlists(stock.ticker)
    }

    suspend fun deleteWatchlistStocks(watchlistName: String) {
        db.getStockDao().deleteWatchlistStocks(watchlistName)
    }

    suspend fun deleteStockWatchlists(ticker: String, watchlistName: String) {
        db.getStockDao().deleteStockFromWatchlist(ticker,watchlistName)
    }

    suspend fun getTopGainersLosers() = RetrofitInstance.api.getGainersLosers()

    suspend fun getStockDetails(symbol: String) = RetrofitInstance.api.getStockDetails(symbol)

    fun getWatchlists() = db.getStockDao().getWatchlists()

    suspend fun getIntraday(symbol: String) = RetrofitInstance.api.getIntraday(symbol)

    suspend fun getDaily(symbol: String) = RetrofitInstance.api.getDaily(symbol)

    suspend fun getWeekly(symbol: String) = RetrofitInstance.api.getWeekly(symbol)

    suspend fun getGlobalQuote(symbol: String) = RetrofitInstance.api.getGlobalQuote(symbol)

}