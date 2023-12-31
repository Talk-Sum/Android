package com.example.aespa

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aespa.databinding.ItemBinding

class case1 : Fragment() , ImageSelectedListener{
    private val viewModel by activityViewModels<ButtonViewModel>()
    private lateinit var binding:ItemBinding// 바인딩 객체 선언
    private val REQUEST_IMAGE_PICK =1
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ItemBinding.inflate(inflater, container, false) // 뷰 바인딩 초기화
        val recyclerView = binding.todoList
        val adapter = CustomAdapter(viewModel,this)
        recyclerView.adapter = CustomAdapter(viewModel,this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        val sum = arguments?.getString("sum")
        viewModel.cot = sum!!

        binding.editTextText3.setText(sum)


        return binding.root // 뷰 계층 구조 반환
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data
            if (selectedImageUri != null) {
                onImageReceived(selectedImageUri, CustomAdapter2.lastSelectedPosition)
            }
        }
    }

    override fun onImageSelected(position: Int) {
        val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        CustomAdapter2.lastSelectedPosition = position
        startActivityForResult(pickImageIntent, REQUEST_IMAGE_PICK)
    }



    override fun onImageReceived(uri: Uri, position: Int) {
        viewModel.updateItem(position, uri)
        binding.todoList.adapter?.notifyItemChanged(position)
    }

}