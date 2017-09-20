package com.jlillioja.tastytracker.Tastyworks

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by jacob on 9/19/17.
 */
interface TastyworksEndpoint {
    @GET("symbol_search/search/{symbol}")
    fun getSymbolCompletions(@Path("symbol") symbol: String) : Observable<List<List<String>>>
}