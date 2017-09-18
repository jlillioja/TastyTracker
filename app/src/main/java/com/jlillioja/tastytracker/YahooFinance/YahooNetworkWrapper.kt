package com.jlillioja.tastytracker.YahooFinance

import android.util.Log
import com.facebook.AccessToken
import com.jlillioja.tastytracker.Facebook.FacebookEndpoint
import com.jlillioja.tastytracker.Facebook.User
import com.jlillioja.tastytracker.Watchlist.Stock
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observable
import rx.schedulers.Schedulers

/**
 * Created by jacob on 9/17/17.
 */
class YahooNetworkWrapper {
    val BASE_URL = "http://finance.yahoo.com/d/"

    val yahooService = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
            .client(OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor()
                            .also { it.level = HttpLoggingInterceptor.Level.BODY })
                    .build())
            .build()
            .create(YahooEndpoint::class.java)

    private val LOG_TAG: String = "YahooNetworkWrapper"

    fun fetchData(stocks: List<String>) : Observable<List<Stock>> {
        val stockSymbols = stocks.fold("") { stock, others -> "$others+$stock"}

        return yahooService.getStockData(stockSymbols, ALL_OPTIONS).map {
            it.string()
                    .split('\n')
                    .map { it.split(',') }
                    .filter { it.size == 4 }
                    .map { Stock(it[0].trim('\"'), it[1], it[2], it[3]) }
        }
    }

    private val SYMBOL_OPTION = "s"
    private val BID_PRICE_OPTION = "b"
    private val ASK_PRICE_OPTION = "a"
    private val LAST_PRICE_OPTION = "l1"
    private val ALL_OPTIONS = SYMBOL_OPTION+BID_PRICE_OPTION+ASK_PRICE_OPTION+LAST_PRICE_OPTION
}