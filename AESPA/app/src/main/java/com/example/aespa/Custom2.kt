package com.example.aespa

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.aespa.databinding.ActivityCustom2Binding
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
        val currentDate = LocalDate.now()
        val currentDateTime = LocalDateTime.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDate.format(dateFormatter)
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formattedDateTime = currentDateTime.format(dateTimeFormatter)
        val content = "hello" //전송받은 요약내용 담을 변수
        binding.savabtn.setOnClickListener{
            val resultIntent = Intent()
            resultIntent.putExtra("use", true)
            resultIntent.putExtra("context", content)
            resultIntent.putExtra("time",formattedDateTime)
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
