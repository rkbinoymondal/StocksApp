package com.SDE.stocksapp.api

import com.SDE.stocksapp.models.DailyResponse
import com.SDE.stocksapp.models.GainerLoserApiResponse
import com.SDE.stocksapp.models.GlobalQuoteResponse
import com.SDE.stocksapp.models.IntradayResponse
import com.SDE.stocksapp.models.StockDetailsResponse
import com.SDE.stocksapp.models.WeeklyResponse
import com.SDE.stocksapp.util.Constants.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface StocksAPI {

    @GET("query?function=TOP_GAINERS_LOSERS")
    suspend fun getGainersLosers(
        @Query("apikey")
        apiKey: String = API_KEY
    ) : Response<GainerLoserApiResponse>

    @GET("query?function=OVERVIEW")
    suspend fun getStockDetails(
        @Query("symbol")
        symbol: String,
        @Query("apikey")
        apiKey: String = API_KEY
    ) : Response<StockDetailsResponse>

    @GET("query?function=TIME_SERIES_INTRADAY")
    suspend fun getIntraday(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "5min",
        @Query("apikey") apiKey: String = API_KEY
    ): Response<IntradayResponse>

    @GET("query?function=TIME_SERIES_DAILY")
    suspend fun getDaily(
        @Query("symbol") symbol: String,
        @Query("outputsize") outputSize: String = "compact",
        @Query("apikey") apiKey: String = API_KEY
    ): Response<DailyResponse>

    @GET("query?function=TIME_SERIES_WEEKLY")
    suspend fun getWeekly(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String = API_KEY
    ): Response<WeeklyResponse>

    @GET("query?function=GLOBAL_QUOTE")
    suspend fun getGlobalQuote(
        @Query("symbol")
        symbol: String,
        @Query("apikey")
        apiKey: String = API_KEY
    ): Response<GlobalQuoteResponse>
}