package com.example.aespa
import android.content.Intent
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.aespa.databinding.ActivityMainBinding
import com.google.firebase.database.FirebaseDatabase
import com.kakao.sdk.common.KakaoSdk
class MainActivity : AppCompatActivity() {
    private var userId: Long = 0L
    private var userNickname: String? = null
    //버튼 뷰 모델 사용
    val viewModel: ButtonViewModel by viewModels()
    private val databaseReference = FirebaseDatabase.getInstance().reference
    private fun saveToDatabase(nickname: String) {
        // 'nicknames' 경로 아래에 닉네임 값을 저장합니다.
        // 이 예제에서는 단순히 닉네임 문자열을 저장하지만, 실제 사용 시에는 구조화된 데이터를 저장할 수 있습니다.
        databaseReference.child("nicknames").push().setValue(nickname)
            .addOnSuccessListener {
                Log.d("Database", "Data saved successfully.")
            }
            .addOnFailureListener { e ->
                Log.d("Database", "Error saving data: ", e)
            }
    }
    //카카오 인증키 불러오기
    class SampleApp : Application() {
        override fun onCreate() {
            super.onCreate()
            KakaoSdk.init(this, getString(R.string.kakao_native_app_key))
        }
    }

    // 뷰 바인딩
    private val binding by lazy{
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        viewModel.nicknameLiveData.observe(this) { nickname ->
            userNickname = nickname
            Log.d("닉","네임$userNickname")
            val intent = Intent(this,SaveList::class.java)
            Log.d("닉2","$userNickname")
            intent.putExtra("Nickname",userNickname)
            intent.putExtra("userId",userId)
            startActivity(intent)
        }
        //로그인 인증
        viewModel.loginable.observe(this) { loginable ->
            when(loginable){
                true -> {
                    //텍스트 뷰 변환
                    binding.textView2.text = "로그인 인증 성공!!"
                    // 노트 리스트 화면으로 이동
                }
                //로그인 하지 않았을 때
                false -> binding.textView2.text = "로그인 인증 필요"
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val bundle = data?.getBundleExtra("userBundle")
            val userInfo = bundle?.getSerializable("userInfo") as? LoginEditFragment.UserInfo
            if (userInfo != null) {
                userNickname = data.getStringExtra("Nickname")
                viewModel.nicknameLiveData.value = userNickname
                saveToDatabase(userNickname ?: "")
                Log.d("아이디는@@", "$userId 이랑 $userNickname")
            }
        }
    }




}