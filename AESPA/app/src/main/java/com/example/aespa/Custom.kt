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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val summary = intent?.getStringExtra("summary")
        Log.d("Custom값은", "$summary")

        // 프래그먼트를 생성하고 Bundle을 전달
        val bundle = Bundle()
        bundle.putString("summary", summary)

        val fragment = BlankFragment3()
        fragment.arguments = bundle

        // 프래그먼트를 표시하기 위한 코드 추가
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment3, fragment) // R.id.fragment_container는 프래그먼트를 표시할 레이아웃의 ID입니다.
            .commit()
    }

    fun onResultFromFragment(data: Intent?) {
        setResult(Activity.RESULT_OK, data)
        val contact = data?.getStringExtra("context")

        finish()
    }


}