package com.jlillioja.tastytracker.Facebook

import com.facebook.AccessToken
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import rx.Observable
import rx.schedulers.Schedulers


class FacebookNetworkWrapper {
    val BASE_URL = "https://graph.facebook.com/"

    val facebookService = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build().create(FacebookEndpoint::class.java)

    fun fetchUser() : Observable<User> {
        return facebookService.getUser(AccessToken.getCurrentAccessToken().token)
    }
}