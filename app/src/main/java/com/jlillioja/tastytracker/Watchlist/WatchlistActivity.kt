package com.jlillioja.tastytracker.Watchlist

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import com.jlillioja.tastytracker.Facebook.FacebookNetworkWrapper
import com.jlillioja.tastytracker.R
import com.jlillioja.tastytracker.YahooFinance.YahooNetworkWrapper

import kotlinx.android.synthetic.main.activity_watchlist.*
import java.util.concurrent.TimeUnit
import android.widget.TextView
import android.view.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject

class WatchlistActivity : AppCompatActivity(), AddWatchlistDialog.Listener {

    private val LOG_TAG = "WatchlistActivity"
    private val WATCHLISTS_KEY = "watchlists"
    private val PREFERENCES_KEY = "TastyTracker"

    val facebook = FacebookNetworkWrapper()
    val yahoo = YahooNetworkWrapper()

    val stocks = PublishSubject.create<List<String>>()
    var watchlist = PublishSubject.create<String>()

    data class Watchlist(var name: String, var stocks: List<Stock>)

    lateinit var stockListAdapter: StockArrayAdapter
    lateinit var watchlistAdapter: WatchlistAdapter

    var currentWatchlist: String? = null
    var currentStocks: List<String> = emptyList()

    private val defaultStocks = setOf("AAPL","MSFT","AMZN")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watchlist)

        setUpWatchlistSpinner()
        setUpStocklist()

        watchlist.subscribe {
            currentWatchlist = it
            stocks.onNext(getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE).getStringSet(it, defaultStocks).toList())
        }
        stocks.subscribe {
            currentStocks = it
            getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
                    .edit()
                    .putStringSet(currentWatchlist, it.toSet())
                    .apply()
        }

        Observable
                .merge(
                        stocks,
                        Observable.interval(5, TimeUnit.SECONDS).map { currentStocks })
//                .filter { it.isNotEmpty() }
                .flatMap { if (it.isNotEmpty()) yahoo.fetchData(it) else Observable.just(emptyList()) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    stockListAdapter.clear()
                    stockListAdapter.addAll(it)
                    stockListAdapter.notifyDataSetInvalidated()
                }

        watchlist.onNext(getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE).getString("default watchlist", "first list"))
    }

    private fun setUpStocklist() {
        stockListAdapter = StockArrayAdapter(this)
        stockList.adapter = stockListAdapter

        stockList.setOnItemLongClickListener { adapterView, view, index, id ->
            removeStock(view.findViewById<TextView>(R.id.symbol)?.text?.toString()) //TODO better
            true
        }
    }

    private fun setUpWatchlistSpinner() {
        watchlistAdapter = WatchlistAdapter(this)
        watchlistAdapter.lists.addAll(getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE).getStringSet(WATCHLISTS_KEY, emptySet()))
        watchlistSpinner.adapter = watchlistAdapter

        facebook.fetchUser().observeOn(AndroidSchedulers.mainThread()).subscribe { user ->
            watchlistAdapter.username = user?.firstName ?: "Somebody"
        }

        watchlistSpinner.onItemSelectedListener = object: OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, index: Int, id: Long) {
                watchlist.onNext(watchlistAdapter.getItem(index))
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

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
                AddWatchlistDialog().show(fragmentManager, "Add Watchlist Dialog")
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

    override fun onListAdded(name: String?) {
        watchlistAdapter.add(name)
        val existingWatchlists = getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE).getStringSet(WATCHLISTS_KEY, emptySet())
        getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE).edit().putStringSet(WATCHLISTS_KEY, existingWatchlists.plus(name).toSet()).apply()
    }

    private fun addStock(symbol: String?) {
        if (symbol != null) {
            val newStocks = currentStocks.plus(symbol)
            stocks.onNext(newStocks)
        }
    }

    private fun removeStock(symbol: String?) {
        stocks.onNext(currentStocks.filter { it != symbol })
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
                addAll(lists.map { "$value's $it" })
            }

        val lists = mutableListOf<String>()

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



