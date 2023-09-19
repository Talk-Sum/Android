package com.example.aespa

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aespa.databinding.ActivityCustom2Binding
import com.example.aespa.databinding.ActivitySaveListBinding
import com.example.aespa.databinding.Item2Binding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class SaveList : AppCompatActivity(), ImageSelectedListener {

    private lateinit var viewModel: SaveViewModel
    private lateinit var binding: ActivitySaveListBinding// 바인딩 객체 선언
    private val REQUEST_IMAGE_PICK =1
    lateinit var fileName: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaveListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = SaveViewModel()
        fileName = intent.getStringExtra("name").toString()

        val recyclerView = binding.menurec
        val adapter = SaveAdapter(viewModel, this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        viewModel.addItem(null, "",fileName,"","")
        binding.menurec.adapter?.notifyDataSetChanged()
    }
    val storage = Firebase.storage
    val storageRef = storage.reference
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data
            if (selectedImageUri != null) {
                onImageReceived(selectedImageUri, SaveAdapter.lastSelectedPosition)
            }
        }
    }
    override fun onImageSelected(position: Int) {
        val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        SaveAdapter.lastSelectedPosition = position
        startActivityForResult(pickImageIntent, REQUEST_IMAGE_PICK)
    }

    override fun onImageReceived(uri: Uri, position: Int) {
        viewModel.updateItem(position, uri)
        binding.menurec.adapter?.notifyItemChanged(position)
    }



}