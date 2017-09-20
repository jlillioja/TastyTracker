package com.jlillioja.tastytracker.Watchlist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jlillioja.tastytracker.R
import com.jlillioja.tastytracker.Tastyworks.TastyworksNetworkWrapper
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_add_symbol.*

class AddStockActivity : AppCompatActivity() {
    private val LOG_TAG = "ADD STOCK ACTIVITY"

    val tastyworksNetworkWrapper = TastyworksNetworkWrapper()

    lateinit var completionsAdapter: CompletionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_symbol)

        completionsAdapter = CompletionsAdapter(this)
        autocompleteList.adapter = completionsAdapter

        RxTextView.textChanges(symbolEntry).skipInitialValue()
                .flatMap { symbol ->
                    tastyworksNetworkWrapper.fetchData(symbol.toString())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { offerCompletions(it) }

        autocompleteList.setOnItemClickListener { adapterView, view, index, id ->
            setResult(Activity.RESULT_OK, Intent().apply { putExtra("SYMBOL", (view as TextView).text.toString()) })
            finish()
        }
    }

    private fun offerCompletions(completions: List<String>?) {
        completionsAdapter.run {
            clear()
            addAll(completions)
        }
    }

    class CompletionsAdapter(context: Context) : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1) {

    }
}