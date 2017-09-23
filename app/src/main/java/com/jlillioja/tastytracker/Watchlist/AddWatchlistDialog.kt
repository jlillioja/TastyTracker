package com.jlillioja.tastytracker.Watchlist

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.widget.EditText

import com.jlillioja.tastytracker.R


class AddWatchlistDialog() : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val watchlistName = EditText(activity).apply { setPadding(5,5,5,5) }

        val builder = AlertDialog.Builder(activity)
        builder.setMessage("Add Watchlist")
                .setView(watchlistName)
                .setPositiveButton("OK", { _, _ ->
                    listener?.onListAdded(watchlistName.text.toString())
                    dismiss()
                })
                .setNegativeButton("Cancel", { _, _ ->
                    dismiss()
                })
        return builder.create()
    }

    interface Listener {
        fun onListAdded(name: String)
    }
    var listener: Listener? = null

    override fun onAttach(context: Context?) {
        if (context is Listener) {
            listener = context
        }

        super.onAttach(context)
    }
}
