package com.jlillioja.tastytracker.Watchlist

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class WatchlistAdapter(context: Context) : ArrayAdapter<String>(context, 0) {
    private var username: String = "Somebody"
    private var lists = mutableListOf<String>()

    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val resultView = if (convertView == null) LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false) else convertView

        resultView.findViewById<TextView>(android.R.id.text1).text = getItem(position)
        resultView.findViewById<TextView>(android.R.id.text1).setTextColor(Color.BLACK)

        return resultView
    }

    fun setUsername(newUsername: String) {
        username = newUsername
        reset()
    }

    fun addLists(moreLists: Collection<String>) {
        lists.addAll(moreLists)
        reset()
    }

    fun getList(index: Int) : String {
        return lists[index]
    }

    fun addList(list: String) {
        lists.add(list)
        reset()
    }

    fun removeList(list: String?) {
        lists.remove(list)
        reset()
    }

    private fun reset() {
        clear()
        addAll(lists.map { "$username's $it" })
        notifyDataSetChanged()
    }
}