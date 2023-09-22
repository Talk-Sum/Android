    package com.example.aespa
    import android.content.Intent
    import android.app.Activity
    import android.app.Application
    import android.os.Bundle
    import android.util.Log
    import androidx.activity.viewModels
    import androidx.appcompat.app.AppCompatActivity
    import com.example.aespa.databinding.ActivityMainBinding
    import com.kakao.sdk.common.KakaoSdk
    class MainActivity : AppCompatActivity() {
        private var userId: Long = 0L
        private var userNickname: String? = null

        //버튼 뷰 모델 사용
        val viewModel: ButtonViewModel by viewModels()


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
                    viewModel.nicknameLiveData.value = userNickname  // LiveData를 갱신합니다.
                    Log.d("아이디는@@", "$userId 이랑 $userNickname")
                }
            }
        }

    }
