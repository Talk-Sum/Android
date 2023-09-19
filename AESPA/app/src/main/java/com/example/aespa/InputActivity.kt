package com.example.aespa
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.aespa.databinding.ActivityInputBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.IOException

class InputActivity : AppCompatActivity() {
    val viewModel: ButtonViewModel by viewModels()
    private val binding by lazy {
        ActivityInputBinding.inflate(layoutInflater)
    }
    val storage = Firebase.storage
    val storageRef = storage.reference
    var fn = ""
    var state = false
    private val REQUEST_VIDEO_CAPTURE = 1
    private val REQUEST_AUDIO_PICK = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.videobtn.setOnClickListener {
            dispatchTakeVideoIntent()
        }

        binding.nextbtn.setOnClickListener {
            if(state) {
                val intent = Intent(this, Custom::class.java)
                intent.putExtra("filename", fn)
                startActivity(intent)
            }
            else
                Toast.makeText(this@InputActivity, "파일을 선택해 주세요.", Toast.LENGTH_SHORT).show()
        }

        binding.soundbtn.setOnClickListener {
            dispatchTakeAudioIntent()
        }
    }
    val typeArr = arrayOf("audio/*")
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
            pickAudioIntent.type = "audio/*"
            if (pickAudioIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(pickAudioIntent, REQUEST_AUDIO_PICK)
            }
        }
    }

    private fun dispatchTakeVideoIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
            != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_VIDEO)) {
                // Show an explanation to the user *asynchronously*.
                AlertDialog.Builder(this)
                    .setTitle("권한 필요")
                    .setMessage("이 기능을 사용하려면 외부 저장소 접근 권한이 필요합니다.")
                    .setPositiveButton("확인") { _, _ ->
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_VIDEO), REQUEST_VIDEO_CAPTURE)
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
            val takeVideoIntent = Intent(Intent.ACTION_GET_CONTENT)
            takeVideoIntent.type = "video/*"
            if (takeVideoIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
            }
        }
    }





    fun sendPostRequest(url: String, data: Map<String, String>) {
        val client = OkHttpClient()

        val requestBodyBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)

        // 매개변수로 받은 데이터를 요청 몸체에 추가
        for ((key, value) in data) {
            requestBodyBuilder.addFormDataPart(key, value)
        }
        val requestBody = requestBodyBuilder.build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object: okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                // 네트워크 에러, 타임아웃, 인터럽트 등의 이유로 요청이 실패한 경우
                e.printStackTrace() // 로그에 에러 정보 출력
                // 사용자에게 에러 메시지 표시 등 추가 처리 가능
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    println("Response received: $responseBody") // 응답 로그 출력

                    // 추가적으로, JSON 파싱이나 다른 데이터 처리를 여기서 수행 가능
                } else {
                    println("Server responded with error code: ${response.code}")
                    val errorBody = response.body?.string()
                    println("Error message: $errorBody") // 서버에서 제공하는 에러 메시지 출력 (있을 경우)

                    // 사용자에게 에러 메시지 표시 등 추가 처리 가능
                }
            }
        })
    }
    //사용법
//val postData = mapOf("key1" to "value1", "key2" to "value2")
//sendPostRequest("https://example.com/your-endpoint", postData)

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
                        binding.textView.append("File Name : $fileName")
                        val storage = Firebase.storage
                        val storageRef = storage.reference
                        val videoRef = storageRef.child("videos/$fileName")

                        // Define a progress bar dialog

                        val progressDialog = Dialog(this)
                        progressDialog.setContentView(R.layout.progressbar)
                        progressDialog.setCancelable(false)

                        progressDialog.show()

                        Completable.create { emitter ->
                            val uploadTask = videoRef.putFile(it)
                            uploadTask.addOnProgressListener { taskSnapshot ->
                                val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                            }
                                .addOnSuccessListener {
                                    emitter.onComplete()
                                    progressDialog.dismiss()
                                }
                                .addOnFailureListener {
                                    emitter.onError(it)
                                    progressDialog.dismiss()
                                }
                        }
                            .subscribeOn(Schedulers.io()) // IO 스레드에서 실행
                            .observeOn(AndroidSchedulers.mainThread()) // 메인 스레드에서 결과 받음
                            .subscribe({
                                // Handle successful upload
                                Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show()
                            }, {
                                // Handle unsuccessful upload
                                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
                            })
                    }
                    cursor.close()
                }
                binding.textView.append("\nDuration : $minutes 분 $seconds 초")
                state = true
            }


        }
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
                        fn = fileName
                        binding.textView.append("File Name : $fileName")
                        val audioRef = storageRef.child("audios/$fileName")

                        // Define a progress bar dialog for audio
                        val progressDialog = Dialog(this)
                        progressDialog.setContentView(R.layout.progressbar)
                        progressDialog.setCancelable(false)

                        progressDialog.show()

                        Completable.create { emitter ->
                            val uploadTask = audioRef.putFile(it)
                            uploadTask.addOnProgressListener { taskSnapshot ->
                                val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                            }
                                .addOnSuccessListener {
                                    emitter.onComplete()
                                    progressDialog.dismiss()
                                }
                                .addOnFailureListener {
                                    emitter.onError(it)
                                    progressDialog.dismiss()
                                }
                        }
                            .subscribeOn(Schedulers.io()) // IO 스레드에서 실행
                            .observeOn(AndroidSchedulers.mainThread()) // 메인 스레드에서 결과 받음
                            .subscribe({
                                // Handle successful upload of audio
                                Toast.makeText(this, "Audio upload successful", Toast.LENGTH_SHORT).show()
                            }, {
                                // Handle unsuccessful upload of audio
                                Toast.makeText(this, "Audio upload failed", Toast.LENGTH_SHORT).show()
                            })
                    }
                    cursor.close()
                }
                binding.textView.append("\nDuration : $minutes 분 $seconds 초")
                state = true
            }
        }


    }


}