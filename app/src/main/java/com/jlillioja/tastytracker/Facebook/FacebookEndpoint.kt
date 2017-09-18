package com.jlillioja.tastytracker.Facebook

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import rx.Observable

interface FacebookEndpoint {
    @GET("me?fields=first_name")
    fun getUser(@Query("access_token") token: String): Observable<User>
}