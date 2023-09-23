package com.example.aespa

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Nickname
import android.provider.MediaStore
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aespa.databinding.ActivityCustom2Binding
import com.example.aespa.databinding.ActivitySaveListBinding
import com.example.aespa.databinding.Item2Binding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class SaveList : AppCompatActivity(), ImageSelectedListener {
    private lateinit var viewModel: SaveViewModel //뷰모델 사용
    private lateinit var binding: ActivitySaveListBinding// 바인딩 객체 선언
    private val REQUEST_IMAGE_PICK =1 //갤러리에서 이미지 가져오는 코드
    var name : String? = null
    var informdata = arrayListOf<saveData>()
    private val databaseReference = FirebaseDatabase.getInstance().reference



    //Intent 요청 코드
    private val REQUEST_INTENT_CODE = 11223344
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaveListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val Nick = intent?.getStringExtra("Nickname")
        if (Nick != null) {
            fetchAllDataFromNicknameFolder(Nick)
        }

        val Nickname = intent?.getStringExtra("Nickname")
        Log.d("넘어온데이터","$Nickname")
        //플로팅 액션 버튼
        val fab:FloatingActionButton = findViewById(R.id.fab)
        viewModel = SaveViewModel()
        //리사이클러뷰
        val recyclerView = binding.menurec
        //연결 어댑터
        val adapter = SaveAdapter(viewModel, this)
        //리사이클러뷰 연결
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        //다이얼로그 클릭리스너
        val saveDialog = SaveDialog(this)
        saveDialog.setOnClickedListener(object : SaveDialog.ButtonClickListener {
            override fun onClicked(fileName: String) {
                val intent = Intent(this@SaveList,InputActivity::class.java)
                name = fileName
                startActivityForResult(intent,REQUEST_INTENT_CODE)
            }
        })
        //플로팅 버튼 클릭시 다이얼로그 띄우기
        binding.fab.setOnClickListener{
            saveDialog.myDig()
        }
        binding.menurec.adapter?.notifyDataSetChanged()
    }
    //파이어 베이스 스토리지
    val storage = Firebase.storage
    val storageRef = storage.reference
    //갤러리에서 이미지 가져오기 위한 부분
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data
            if (selectedImageUri != null) {
                onImageReceived(selectedImageUri, SaveAdapter.lastSelectedPosition)
            }
        }
        if (requestCode == REQUEST_INTENT_CODE && resultCode == Activity.RESULT_OK) {
            val contact = data?.getStringExtra("context")
            val date = data?.getStringExtra("time")
            val Nickname = intent.getStringExtra("Nickname")
            viewModel.addItem(null,"$date","$name","$Nickname","$contact")
            val sd : saveData = saveData(null,"$date","$name","$Nickname","$contact")
            binding.menurec.adapter?.notifyDataSetChanged()
            databaseReference.child("user").child("$Nickname").push().setValue(sd)
                .addOnSuccessListener {
                    Log.d("Database", "Data saved successfully.")
                }
                .addOnFailureListener { e ->
                    Log.d("Database", "Error saving data: ", e)
                }
            informdata.add(saveData(null,"$date","$name","$Nickname","$contact"))
        }
    }

    //해당 포지션의 이미지 선택
    override fun onImageSelected(position: Int) {
        val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        SaveAdapter.lastSelectedPosition = position
        startActivityForResult(pickImageIntent, REQUEST_IMAGE_PICK)
    }

    //이미지 가져와서 붙임
    override fun onImageReceived(uri: Uri, position: Int) {
        viewModel.updateItem(position, uri)
        binding.menurec.adapter?.notifyItemChanged(position)
    }
    // Firebase Realtime Database에서 닉네임 폴더의 데이터 모두 가져오기
    private fun fetchAllDataFromNicknameFolder(nickname: String) {
        databaseReference.child("user").child(nickname).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tempDataList = arrayListOf<saveData>() // 임시 데이터 리스트 생성

                for (snapshot in dataSnapshot.children) {
                    val data = snapshot.getValue(saveData::class.java)
                    data?.let {
                        tempDataList.add(it)
                    }
                }

                // 데이터 로딩 후 viewModel 업데이트
                informdata = tempDataList // 기존 데이터 대신 새로운 데이터로 바꿈
                viewModel.clearItems() // viewModel 내부 데이터 초기화
                for (i in informdata) {
                    viewModel.addItem(i.img, "${i.date}", "${i.name}", "${i.user_name}", "${i.content}")
                }

                binding.menurec.adapter?.notifyDataSetChanged() // 리사이클러뷰 데이터 변경 알림
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "fetchAllDataFromNicknameFolder:onCancelled", databaseError.toException())
            }
        })
    }


}