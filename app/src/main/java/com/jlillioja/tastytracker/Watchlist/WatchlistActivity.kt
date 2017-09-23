package com.jlillioja.tastytracker.Watchlist

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
    private val DEFAULT_STOCKS = setOf("AAPL","MSFT","ES")
    private val ADD_STOCK_RESULT_CODE = 1

    private val facebook = FacebookNetworkWrapper()
    private val yahoo = YahooNetworkWrapper()

    private var watchlist = PublishSubject.create<String>()
    private var currentWatchlist: String? = null
    lateinit private var watchlistAdapter: WatchlistAdapter

    private val stocks = PublishSubject.create<List<String>>()
    private var currentStocks: List<String> = emptyList()
    lateinit private var stockListAdapter: StockArrayAdapter

    lateinit private var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watchlist)
        sharedPreferences = getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)

        initializeWatchlist()

        setUpWatchlistSpinner()
        setUpStocklist()

        setUpSubscriptions()

        startFirstWatchlist()
    }

    private fun initializeWatchlist() {
        if (sharedPreferences.getStringSet(WATCHLISTS_KEY, emptySet()).isEmpty()) {
            sharedPreferences.edit().putStringSet(WATCHLISTS_KEY, setOf("first list")).apply()
        }
    }

    private fun setUpWatchlistSpinner() {
        watchlistAdapter = WatchlistAdapter(this)
        watchlistAdapter.addLists(sharedPreferences.getStringSet(WATCHLISTS_KEY, emptySet()))
        watchlistSpinner.adapter = watchlistAdapter

        facebook.fetchUser().observeOn(AndroidSchedulers.mainThread()).subscribe { user ->
            watchlistAdapter.setUsername(user?.firstName ?: "Nobody")
        }

        watchlistSpinner.onItemSelectedListener = object: OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, index: Int, id: Long) {
                watchlist.onNext(watchlistAdapter.getList(index))
            }
        }
    }

    private fun setUpStocklist() {
        stockListAdapter = StockArrayAdapter(this)
        stockList.adapter = stockListAdapter

        stockList.setOnItemLongClickListener { _, view, _, _ ->
            removeStock(view.findViewById<TextView>(R.id.symbol)?.text?.toString()) //TODO better
            true
        }
    }

    private fun setUpSubscriptions() {
        watchlist.subscribe {
            currentWatchlist = it
            stocks.onNext(sharedPreferences.getStringSet(it, DEFAULT_STOCKS).toList())
        }
        stocks.subscribe {
            currentStocks = it
            sharedPreferences
                    .edit()
                    .putStringSet(currentWatchlist, it.toSet())
                    .apply()
        }

        Observable
                .merge(
                        stocks,
                        Observable.interval(5, TimeUnit.SECONDS).map { currentStocks })
                .flatMap { if (it.isNotEmpty()) yahoo.fetchData(it) else Observable.just(emptyList()) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    stockListAdapter.clear()
                    stockListAdapter.addAll(it)
                    stockListAdapter.notifyDataSetInvalidated()
                }
    }

    private fun startFirstWatchlist() {
        watchlist.onNext(sharedPreferences.getStringSet(WATCHLISTS_KEY, setOf("first list")).first())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_watchlist, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.addStock -> {
                val intent = Intent(this, AddStockActivity::class.java)
                startActivityForResult(intent, ADD_STOCK_RESULT_CODE)
                return true
            }
            R.id.addList -> {
                AddWatchlistDialog().show(fragmentManager, "Add Watchlist Dialog")
                return true
            }
            R.id.removeList -> {
                removeList(currentWatchlist)
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ADD_STOCK_RESULT_CODE) {
            val newStock = data?.getStringExtra("SYMBOL")
            addStock(newStock)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onListAdded(name: String) {
        watchlistAdapter.addList(name)
        val existingWatchlists = sharedPreferences.getStringSet(WATCHLISTS_KEY, emptySet())
        sharedPreferences.edit().putStringSet(WATCHLISTS_KEY, existingWatchlists.plus(name).toSet()).apply()
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

    private fun removeList(list: String?) {
        val newLists = sharedPreferences.getStringSet(WATCHLISTS_KEY, emptySet()).filter { it != list }
        sharedPreferences.edit().putStringSet(WATCHLISTS_KEY, newLists.toSet()).apply()
        watchlistAdapter.removeList(list)
        watchlist.onNext(newLists.first())
    }
}