package com.jlillioja.tastytracker.Watchlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.jlillioja.tastytracker.R

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