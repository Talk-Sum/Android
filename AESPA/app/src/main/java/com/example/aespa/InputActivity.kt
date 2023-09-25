package com.example.aespa
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.aespa.databinding.ActivityInputBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class InputActivity : AppCompatActivity() {
    private val compositeDisposable = CompositeDisposable()
    private val REQUEST_INTENT_CODE = 11223345
    //뷰모델
    val viewModel: ButtonViewModel by viewModels()
    //뷰 바인딩
    private val binding by lazy {
        ActivityInputBinding.inflate(layoutInflater)
    }
    //파이어베이스
    val storage = Firebase.storage
    val storageRef = storage.reference
    //인텐트 값 가저오기
    //비디오, 오디오 요청 상수
    private val REQUEST_VIDEO_CAPTURE = 1
    private val REQUEST_AUDIO_PICK = 2
    //로그인 상태 정의 변수
    var state = false
    var summary : String? =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        //비디오 버튼 클릭 리스너
        binding.videobtn.setOnClickListener {
            showYoutubeLinkDialog()
        }
        //오디오 버튼 클릭 리스너
        binding.soundbtn.setOnClickListener {
            dispatchTakeAudioIntent()
        }
        //다음 버튼 클릭 리스너
        binding.nextbtn.setOnClickListener {
            Log.d("서버ㄴ","$state")
            if(state) {
                val intent = Intent(this, Custom::class.java)
                intent.putExtra("summary","$summary")
                startActivityForResult(intent, REQUEST_INTENT_CODE)
            }
            else
                Toast.makeText(this@InputActivity, "파일을 선택해 주세요.", Toast.LENGTH_SHORT).show()
        }
    }
    //오디오 가저오는 함수
    private fun dispatchTakeAudioIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_AUDIO)) {
                // Show an explanation to the user *asynchronously*.
                AlertDialog.Builder(this)
                    .setTitle("권한 필요")
                    .setMessage("이 기능을 사용하려면 외부 저장소 접근 권한이 필요합니다.")
                    .setPositiveButton("확인") { _, _ ->
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_AUDIO), REQUEST_AUDIO_PICK)
                    }
                    .setNegativeButton("취소", null)
                    .create()
                    .show()
            } else {
                // "Never ask again" selected, guide user to settings
                AlertDialog.Builder(this)
                    .setTitle("권한 설정")
                    .setMessage("외부 저장소 접근 권한이 필요합니다. 설정 메뉴로 이동하여 권한을 활성화해주세요.")
                    .setPositiveButton("설정으로 이동") { _, _ ->
                        startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)))
                    }
                    .setNegativeButton("취소", null)
                    .create()
                    .show()
            }
        } else {
            val pickAudioIntent = Intent(Intent.ACTION_GET_CONTENT)
            pickAudioIntent.type = "audio/wav" // WAV 파일을 선택하도록 변경
            if (pickAudioIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(pickAudioIntent, REQUEST_AUDIO_PICK)
            }
        }

    }


    private fun showYoutubeLinkDialog() {
        val dialogView = layoutInflater.inflate(R.layout.youtube_link_dialog, null)
        val editTextLink = dialogView.findViewById<EditText>(R.id.eddit)
        val saveButton = dialogView.findViewById<ImageButton>(R.id.btnsave)

        val languageSpinner = dialogView.findViewById<Spinner>(R.id.languageSpinner)
        val languages = arrayOf("한국어", "영어", "일본어", "중국어")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(dialogView)
        val alertDialog = alertDialogBuilder.create()

        saveButton.setOnClickListener {
            val youtubeLink = editTextLink.text.toString()
            var selectedLanguage = languages[languageSpinner.selectedItemPosition]
            when (selectedLanguage) {
                "한국어" -> selectedLanguage = "ko-KR"
                "영어" -> selectedLanguage = "en-US"
                "일본어" -> selectedLanguage = "ja-JP"
                "중국어" -> selectedLanguage = "cmn-Hans-CN"
            }
            Log.d("링크", "$youtubeLink")
            if (youtubeLink.isNotEmpty()) {
                // 다이얼로그를 숨김
                alertDialog.dismiss()
                // 프로그레스 바를 표시
                binding.progressBar.visibility = View.VISIBLE

                val disposable: Disposable = Observable.fromCallable {
                    sendLinkToServer(youtubeLink, selectedLanguage)
                }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        // 서버 요청이 완료되면 프로그레스 바를 숨김
                        binding.progressBar.visibility = View.GONE
                    }, { error ->
                        error.printStackTrace()
                        // 요청 실패 시 프로그레스 바를 숨김
                        binding.progressBar.visibility = View.GONE
                    })

                compositeDisposable.add(disposable)
            } else {
                Toast.makeText(this@InputActivity, "유튜브 링크를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
        alertDialog.show()
    }

    private fun sendLinkToServer(youtubeLink: String, selectedLanguage: String) {
        val client = OkHttpClient.Builder()
            .connectTimeout(300, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .build()
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = """
    {
        "url": "$youtubeLink",
        "language": "$selectedLanguage"
    }
    """.trimIndent().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("https://a98ab72c0f8b.ngrok.app/api/uploadLink")
            .post(requestBody)
            .build()
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                Log.d("서버 응답 데이터", "$responseBody")
                summary = responseBody
                Log.d("서버 응답 데이터", "$summary")

                // 서버 응답을 받은 후에 state 값을 변경
                runOnUiThread {
                    binding.textView.text = "$youtubeLink"
                    state = true
                }
            } else {
                Log.d("실패", "진짜울고싶다.")
            }
        } catch (e: IOException) {
            Log.e("네트워크 오류", e.message ?: "Unknown error")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }


    private fun uploadMediaToServer(fileUri: Uri, type: String, fileName: String) {
        // ProgressBar 표시 시작
        binding.progressBar.visibility = View.VISIBLE
        val observable: Observable<Response> = Observable.create(ObservableOnSubscribe<Response> { emitter ->
            val file = File(fileUri.path)
            val fileRequestBody = RequestBody.create("audio/wav".toMediaType(), file) // WAV 파일로 설정
            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(type, fileName, fileRequestBody)
                .build()
            val request = Request.Builder()
                .url("https://a98ab72c0f8b.ngrok.app/api/uploadLink")
                .post(multipartBody)
                .build()
            val client = OkHttpClient()
            val response = client.newCall(request).execute()
            if (!emitter.isDisposed) {
                emitter.onNext(response)
                emitter.onComplete()
            }
        })
        val disposable = observable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                if (response.isSuccessful) {
                    // 성공 처리
                    Log.d("성공","")
                } else {
                    // 실패 처리
                    Log.d("실패","")
                }
                // ProgressBar 숨기기
                binding.progressBar.visibility = View.GONE
            }, { error ->
                // 에러 처리
                error.printStackTrace()
                // ProgressBar 숨기기
                binding.progressBar.visibility = View.GONE
            })

        compositeDisposable.add(disposable)
    }
    //비디오 가저오는 함수

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            val videoUri: Uri? = data?.data
            // videoUri를 사용하여 동영상을 가져옵니다.
            videoUri?.let {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(this, it)
                val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt()
                val minutes = duration?.div((1000 * 60))
                val seconds = (duration?.div(1000))?.rem(60)
                binding.textView.text = ""
                val cursor = contentResolver.query(videoUri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        val fileName = cursor.getString(nameIndex)
                        uploadMediaToServer(videoUri, "video", fileName)
                        binding.textView.append("$fileName")
                    }
                    cursor.close()
                }
                binding.textView.append("\nDuration : $minutes 분 $seconds 초")
                state = true
            }
        }
        //오디오 요청 코드
        else if (requestCode == REQUEST_AUDIO_PICK && resultCode == RESULT_OK) {
            val audioUri: Uri? = data?.data
            // audioUri를 사용하여 오디오를 가져옵니다.
            audioUri?.let {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(this, it)
                val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt()
                val minutes = duration?.div(1000 * 60)
                val seconds = (duration?.div(1000))?.rem(60)
                binding.textView.text = "" // 이 TextView는 오디오 정보를 표시하기 위한 것입니다. 레이아웃에 적절한 ID를 가진 TextView를 사용하세요.
                val cursor = contentResolver.query(audioUri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        val fileName = cursor.getString(nameIndex)
                        binding.textView.append("$fileName")
                        uploadMediaToServer(audioUri, "audio", fileName)
                    }
                    cursor.close()
                }
                binding.textView.append("\nDuration : $minutes 분 $seconds 초")
                state = true
            }
        }
        if (requestCode == REQUEST_INTENT_CODE && resultCode == RESULT_OK) {
            // C로부터 받은 결과를 그대로 A로
            val contact = data?.getStringExtra("context")
            Log.d("input값은2","${contact}")
            setResult(RESULT_OK, data)
            finish()
        }

    }
}