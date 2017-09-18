package com.jlillioja.tastytracker.Watchlist

import android.os.Bundle
import android.support.annotation.MainThread
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TableLayout
import com.jlillioja.tastytracker.Facebook.FacebookNetworkWrapper
import com.jlillioja.tastytracker.R
import com.jlillioja.tastytracker.YahooFinance.YahooNetworkWrapper

import kotlinx.android.synthetic.main.activity_watchlist.*
import rx.Observable
import rx.Scheduler
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class WatchlistActivity : AppCompatActivity() {

    val LOG_TAG = "Tracker Activity"

    val facebook = FacebookNetworkWrapper()
    val yahoo = YahooNetworkWrapper()

    val stocks = listOf("AAPL", "MSFT", "ES")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watchlist)

        facebook.fetchUser().subscribe { user ->
            watchlistTitle.text = "${user?.firstName ?: "Somebody"} first list"
        }

        table.addRow("SYMBOL", "BID", "ASK", "LAST")

        Observable.interval(5, TimeUnit.SECONDS)
                .flatMap { yahoo.fetchData(stocks) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    table.removeAllViews()
                    it.forEach {
                        table.addRow(it)
                    }
                }
    }

    private fun TableLayout.addRow(stock: Stock) {
        this.addRow(stock.stockSymbol, stock.bidPrice, stock.askPrice, stock.lastPrice)
    }

    private fun TableLayout.addRow(stockSymbol: String, bidPrice: String, askPrice: String, lastPrice: String) {
        this.addView(WatchlistRow(this@WatchlistActivity, stockSymbol, bidPrice, askPrice, lastPrice))
    }
}
