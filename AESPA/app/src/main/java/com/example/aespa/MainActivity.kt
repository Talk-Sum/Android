    package com.example.aespa
    import android.content.Intent
    import android.content.pm.PackageManager
    import android.Manifest
    import android.app.AlertDialog
    import android.app.Application
    import android.app.Dialog
    import android.content.ContentValues.TAG
    import android.media.MediaMetadataRetriever
    import android.net.Uri
    import android.os.Bundle
    import android.provider.OpenableColumns
    import android.provider.Settings.Global.getString
    import android.util.Log
    import android.widget.Toast
    import androidx.activity.result.ActivityResultLauncher
    import androidx.activity.viewModels
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.app.ActivityCompat
    import androidx.core.content.ContextCompat
    import androidx.core.view.isGone
    import androidx.fragment.app.activityViewModels
    import androidx.lifecycle.ViewModelProvider
    import com.example.aespa.databinding.ActivityMainBinding
    import com.google.firebase.ktx.Firebase
    import com.google.firebase.storage.ktx.storage
    import com.kakao.sdk.common.KakaoSdk
    import io.reactivex.Completable
    import io.reactivex.android.schedulers.AndroidSchedulers
    import io.reactivex.schedulers.Schedulers
    import com.kakao.sdk.common.util.Utility
    import com.kakao.sdk.user.UserApiClient
    class MainActivity : AppCompatActivity() {


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

            //로그인 인증
            viewModel.loginable.observe(this) { loginable ->
                when(loginable){
                    true -> {

                        //텍스트 뷰 변환

                        binding.textView2.text = "로그인 인증 성공!!"



                        // 노트 리스트 화면으로 이동

                        val intent = Intent(this,SaveList::class.java)
                        startActivity(intent)


                    }

                    //로그인 하지 않았을 때

                    false -> binding.textView2.text = "로그인 인증 필요"
                }
            }



        }
    }
