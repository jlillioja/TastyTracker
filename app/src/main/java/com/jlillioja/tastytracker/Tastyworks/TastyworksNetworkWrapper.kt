package com.jlillioja.tastytracker.Tastyworks

import android.util.Log
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class TastyworksNetworkWrapper {
    private val LOG_TAG: String = "TastyworkNetworkWrapper"
    val BASE_URL = "https://trade.tastyworks.com/"

    val tastyworksService = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(TastyworksEndpoint::class.java)


    fun fetchData(symbol: String): Observable<List<String>> {
        return tastyworksService.getSymbolCompletions(symbol.toUpperCase())
                .map { stocksList ->
                    stocksList.map { stockDetails -> stockDetails[0] }
                }.onErrorReturnItem(emptyList())
    }
}