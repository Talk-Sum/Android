package com.example.aespa

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.example.aespa.databinding.ActivityCustom2Binding

class Custom2 : AppCompatActivity() {
    private val viewModel by viewModels<ButtonViewModel>()

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


        saveDialog.setOnClickedListener(object : SaveDialog.ButtonClickListener {
            override fun onClicked(fileName: String) {
                val intent = Intent(this@Custom2,SaveList::class.java)
                intent.putExtra("name",fileName)
                startActivity(intent)
            }
        })


        binding.savabtn.setOnClickListener{
            saveDialog.myDig()
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