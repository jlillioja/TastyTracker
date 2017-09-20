package com.jlillioja.tastytracker.YahooFinance

import com.jlillioja.tastytracker.Watchlist.Stock
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class YahooNetworkWrapper {
    val BASE_URL = "http://finance.yahoo.com/d/"

    val yahooService = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
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