package com.jlillioja.tastytracker.Watchlist

/**
 * Created by jacob on 9/17/17.
 */
data class Stock(
        val stockSymbol: String,
        val bidPrice: String,
        val askPrice: String,
        val lastPrice: String)