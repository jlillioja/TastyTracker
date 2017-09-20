package com.jlillioja.tastytracker.Facebook

import com.facebook.AccessToken
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory


class FacebookNetworkWrapper {
    val BASE_URL = "https://graph.facebook.com/"

    val facebookService = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build().create(FacebookEndpoint::class.java)

    fun fetchUser() : Observable<User> {
        return facebookService.getUser(AccessToken.getCurrentAccessToken().token)
    }
}