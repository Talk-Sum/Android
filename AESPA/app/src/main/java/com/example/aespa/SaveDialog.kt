package com.example.aespa

import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton

class SaveDialog(context: Context) {
    private val dialog = Dialog(context)

    fun myDig() {
        dialog.setContentView(R.layout.nextsavedialog)
        val edit = dialog.findViewById<EditText>(R.id.editTextText5)
        val btnOk = dialog.findViewById<ImageButton>(R.id.savebutton)

        btnOk.setOnClickListener {
            onClickedListener?.onClicked(edit.text.toString())
            dialog.dismiss()
        }

        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()
    }
    interface ButtonClickListener {
        fun onClicked(fileName: String)
    }

    private var onClickedListener: ButtonClickListener? = null


    fun setOnClickedListener(listener: ButtonClickListener) {
        onClickedListener = listener
    }
}