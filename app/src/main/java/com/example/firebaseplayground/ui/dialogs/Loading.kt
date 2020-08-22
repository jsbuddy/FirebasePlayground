package com.example.firebaseplayground.ui.dialogs

import android.app.Activity
import android.app.AlertDialog
import com.example.firebaseplayground.R

class LoadingDialog(private val activity: Activity) {
    private lateinit var dialog: AlertDialog

    fun start() {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.loading, null))
        builder.setCancelable(false)

        dialog = builder.create()
        dialog.show()
    }

    fun stop() {
        dialog.dismiss()
    }
}