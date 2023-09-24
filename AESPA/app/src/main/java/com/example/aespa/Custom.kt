package com.example.aespa

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.example.aespa.databinding.ActivityCustomBinding

class Custom : AppCompatActivity() {
    val viewModel by viewModels<ButtonViewModel>()
    private val REQUEST_INTENT_CODE = 11223346
    private val binding by lazy {
        ActivityCustomBinding.inflate(layoutInflater)
    }
    val summary = intent.getStringExtra("summary")

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }
    fun onResultFromFragment(data: Intent?) {
        setResult(Activity.RESULT_OK, data)
        val contact = data?.getStringExtra("context")
        Log.d("Custom값은","${contact},${summary}")
        finish()
    }


}