package com.jlillioja.tastytracker.Watchlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import com.jlillioja.tastytracker.Facebook.FacebookNetworkWrapper
import com.jlillioja.tastytracker.R
import com.jlillioja.tastytracker.YahooFinance.YahooNetworkWrapper

import kotlinx.android.synthetic.main.activity_watchlist.*
import java.util.concurrent.TimeUnit
import android.widget.TextView
import android.view.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject

class WatchlistActivity : AppCompatActivity() {
    val LOG_TAG = "WatchlistActivity"

    val facebook = FacebookNetworkWrapper()
    val yahoo = YahooNetworkWrapper()

    val stocks = BehaviorSubject.createDefault(listOf("AAPL", "MSFT", "ES"))

    lateinit var stockListAdapter: StockArrayAdapter
    lateinit var watchlistAdapter: WatchlistAdapter

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_watchlist, menu)
        return true
    }

    private val ADD_STOCK_CODE: Int = 1

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.addStock -> {
                val intent = Intent(this, AddStockActivity::class.java)
                startActivityForResult(intent, ADD_STOCK_CODE)
                return true
            }
            R.id.addList -> {
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ADD_STOCK_CODE) {
            val newStock = data?.getStringExtra("SYMBOL")
            addStock(newStock)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watchlist)

        stockListAdapter = StockArrayAdapter(this)
        stockList.adapter = stockListAdapter

        watchlistAdapter = WatchlistAdapter(this)
        watchlistSpinner.adapter = watchlistAdapter

        stockList.setOnItemLongClickListener { adapterView, view, index, id ->
            removeStock(view.findViewById<TextView>(R.id.symbol)?.text?.toString())
            true
        }

        facebook.fetchUser().observeOn(AndroidSchedulers.mainThread()).subscribe { user ->
            watchlistAdapter.username = user?.firstName ?: "Somebody"
        }


        Observable.merge(
                stocks,
                Observable.interval(5, TimeUnit.SECONDS).map { stocks.value }
        )
                .filter { it.isNotEmpty() }
                .flatMap { yahoo.fetchData(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    stockListAdapter.clear()
                    stockListAdapter.addAll(it)
                    stockListAdapter.notifyDataSetInvalidated()
                }
    }

    private fun addStock(symbol: String?) {
        if (symbol != null) {
            stocks.onNext(stocks.value.plus(symbol))
        }
    }

    private fun removeStock(symbol: String?) {
        stocks.onNext(stocks.value.filter { it != symbol })
    }

    class StockArrayAdapter(context: Context) : ArrayAdapter<Stock>(context, 0) {
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

    class WatchlistAdapter(context: Context) : ArrayAdapter<String>(context, 0) {

        var username: String = "Somebody"
        set(value) {
            clear()
            addAll(lists.map { value + it })
        }

        val lists = mutableListOf("'s first list")

        init {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val list = getItem(position)

            val resultView = if (convertView == null) LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false) else convertView

            resultView.findViewById<TextView>(android.R.id.text1).text = list

            return resultView
        }
    }
}



