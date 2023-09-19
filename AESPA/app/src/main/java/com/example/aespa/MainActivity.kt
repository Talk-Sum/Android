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
        val viewModel: ButtonViewModel by viewModels()

        class SampleApp : Application() {
            override fun onCreate() {
                super.onCreate()
                KakaoSdk.init(this, getString(R.string.kakao_native_app_key))
            }
        }


        private val binding by lazy{
            ActivityMainBinding.inflate(layoutInflater)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(binding.root)
            viewModel.loginable.observe(this) { loginable ->
                when(loginable){
                    true -> {
                        binding.textView2.text = "로그인 인증 성공!!"
                        val intent = Intent(this,InputActivity::class.java)
                        startActivity(intent)
                    }
                    false -> binding.textView2.text = "로그인 인증 필요"
                }
            }
        }



    }
