package com.example.aespa

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.example.aespa.databinding.ActivityCustom2Binding

class Custom2 : AppCompatActivity() {
    private val viewModel by viewModels<ButtonViewModel>()
    private val REQUEST_INTENT_CODE = 11223347
    private fun insertFragment(containerId: Int, fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .add(containerId,fragment)
            .commit()
    }


    private val binding by lazy{
    ActivityCustom2Binding.inflate(layoutInflater)
}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val saveDialog = SaveDialog(this)
        setContentView(binding.root)

        val content = "hello" //전송받은 요약내용 담을 변수


        binding.savabtn.setOnClickListener{
            val resultIntent = Intent()
            resultIntent.putExtra("use", true)
            resultIntent.putExtra("context", content)
            setResult(RESULT_OK, resultIntent)
            finish()
        }





        val imgId = intent.getIntExtra("imgId",0)
        val itemId = intent.getIntExtra("itemId",0)
        when(itemId){
            3 -> insertFragment(R.id.fragment4,case3())
            2 -> insertFragment(R.id.fragment4,case1())
            1 -> insertFragment(R.id.fragment4,case2())
            else -> Log.e("에러","에러")
        }
    }
}