package com.jlillioja.tastytracker.Watchlist

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.TableRow
import android.widget.TextView

/**
 * Created by jacob on 9/17/17.
 */
class WatchlistRow(val mContext: Context, val stockSymbol: String, val bidPrice: String, val askPrice: String, val lastPrice: String) : TableRow(mContext) {
    init {
        addView(TextView(mContext).apply {
            layout()
            text = stockSymbol
        })
        addView(TextView(mContext).apply {
            layout()
            text = bidPrice
        })
        addView(TextView(mContext).apply {
            layout()
            text = askPrice
        })
        addView(TextView(mContext).apply {
            layout()
            text = lastPrice
        })
    }

    private fun TextView.layout() {
        this.gravity = Gravity.CENTER
        this.setPadding(10,10,10,10)
        this.setTextColor(android.graphics.Color.BLACK)
        this.textSize = 24f
    }
}