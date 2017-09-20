package com.jlillioja.tastytracker.YahooFinance

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface YahooEndpoint {
    @GET("quotes.csv")
    fun getStockData(@Query("s") symbols: String, @Query("f") data: String): Observable<ResponseBody>
}