package com.jlillioja.tastytracker.Watchlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.jlillioja.tastytracker.R

import kotlinx.android.synthetic.main.watchlist_row.view.*

class WatchlistRow(context: Context, parent: ViewGroup) {
    var view = LayoutInflater.from(context).inflate(R.layout.watchlist_row, parent) as LinearLayout

    fun displaying(stock: Stock): View {
        view.symbol.text = stock.stockSymbol
        view.bidPrice.text = stock.bidPrice
        view.askPrice.text = stock.askPrice
        view.lastPrice.text = stock.lastPrice

        return view
    }
}