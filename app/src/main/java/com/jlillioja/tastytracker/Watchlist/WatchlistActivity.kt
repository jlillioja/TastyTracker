package com.jlillioja.tastytracker.Watchlist

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import com.jlillioja.tastytracker.Facebook.FacebookNetworkWrapper
import com.jlillioja.tastytracker.R
import com.jlillioja.tastytracker.YahooFinance.YahooNetworkWrapper

import kotlinx.android.synthetic.main.activity_watchlist.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import android.widget.TextView
import rx.subjects.BehaviorSubject
import android.R.menu
import android.view.*


class WatchlistActivity : AppCompatActivity() {

    val LOG_TAG = "Tracker Activity"

    val facebook = FacebookNetworkWrapper()
    val yahoo = YahooNetworkWrapper()

    val stocks = BehaviorSubject.create(mutableListOf("AAPL", "MSFT", "ES"))

    lateinit var stockAdapter: StockAdapter

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_watchlist, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.getItemId()

        return if (id == R.id.addStock) {
            true
        } else super.onOptionsItemSelected(item)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watchlist)

        stockAdapter = StockAdapter(this)

        watchlist.adapter = stockAdapter
        watchlist.setOnItemLongClickListener { adapterView, view, index, id ->
            stocks.onNext(stocks.value.apply { removeAt(index) })
            true
        }

        facebook.fetchUser().subscribe { user ->
            watchlistTitle.text = "${user?.firstName ?: "Somebody"} first list"
        }


        Observable.merge(
                stocks.asObservable(),
                Observable.interval(5, TimeUnit.SECONDS).withLatestFrom(stocks) { _, stocks -> stocks }
        )
                .flatMap { yahoo.fetchData(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    stockAdapter.clear()
                    stockAdapter.addAll(it)
                    stockAdapter.notifyDataSetInvalidated()
                }
    }

    class StockAdapter(context: Context) : ArrayAdapter<Stock>(context, 0) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val stock = getItem(position)

            val resultView = if (convertView == null) LayoutInflater.from(context).inflate(R.layout.watchlist_row, parent, false) else convertView

            resultView.findViewById<TextView>(R.id.symbol).text = stock.stockSymbol
            resultView.findViewById<TextView>(R.id.bidPrice).text = stock.bidPrice
            resultView.findViewById<TextView>(R.id.askPrice).text = stock.askPrice
            resultView.findViewById<TextView>(R.id.lastPrice).text = stock.lastPrice

            resultView.isLongClickable = true

            return resultView
        }
    }
}

