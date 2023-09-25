package com.example.aespa

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.aespa.databinding.ActivityCustom2Binding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Custom2 : AppCompatActivity() {
    private val viewModel by viewModels<ButtonViewModel>()
    private val REQUEST_INTENT_CODE = 11223347
    private val STORAGE_PERMISSION_CODE = 98995
    private val binding by lazy { ActivityCustom2Binding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.root.post {
            // 화면의 뷰가 레이아웃에 배치된 후의 작업을 수행합니다.
            setupViews()
            handleIntentExtras()
        }
    }

    private fun setupViews() {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formattedDateTime = LocalDateTime.now().format(dateTimeFormatter)
        val content = intent.getStringExtra("summary")

        binding.savabtn.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("use", true)
                putExtra("context", content)
                putExtra("time", formattedDateTime)
                Log.d("콘","$content")
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun handleIntentExtras() {
        val imgId = intent.getIntExtra("imgId", 0)
        val itemId = intent.getIntExtra("itemId", 0)
        val sum = intent.getStringExtra("summary")
        val fragment = when (itemId) {
            3 -> {
                val bundle = Bundle()
                bundle.putString("sum","$sum") // 전달할 값 설정
                val fragment = case3() // YourFragment는 프래그먼트 클래스명으로 변경해야 합니다.
                fragment.arguments = bundle
                fragment // 이 부분을 수정했습니다.
            }
            2 -> {
                val bundle = Bundle()
                bundle.putString("sum","$sum") // 전달할 값 설정
                Log.d("전달","$sum")
                val fragment = case1() // YourFragment는 프래그먼트 클래스명으로 변경해야 합니다.
                fragment.arguments = bundle
                fragment // 이 부분을 수정했습니다.
            }
            1 -> {
                val bundle = Bundle()
                bundle.putString("sum","$sum") // 전달할 값 설정
                Log.d("전달","$sum")
                val fragment = case2() // YourFragment는 프래그먼트 클래스명으로 변경해야 합니다.
                fragment.arguments = bundle
                fragment // 이 부분을 수정했습니다.
            }
            else -> {
                Log.e("에러", "에러")
                return
            }
        }

        insertFragment(R.id.fragment4, fragment)

        val outputPath = File(getDownloadFolderPath(), "activity_content${itemId}.pdf").absolutePath
        checkPermissionAndSave(outputPath)
    }

    private fun insertFragment(containerId: Int, fragment: Fragment) {
        supportFragmentManager.beginTransaction().add(containerId, fragment).commit()
    }

    fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.background?.draw(canvas) ?: canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return bitmap
    }

    private fun getDownloadFolderPath(): File {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    }

    private fun checkPermissionAndSave(outputPath: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 이상에서는 MANAGE_EXTERNAL_STORAGE 권한이 필요하지 않습니다.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                saveActivityContentAsPdf(this, outputPath)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
            }
        } else {
            // Android 10 이하에서는 WRITE_EXTERNAL_STORAGE 권한만 요청합니다.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                saveActivityContentAsPdf(this, outputPath)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 부여된 경우 작업 진행
                handleIntentExtras()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // 사용자가 권한을 거부하고 권한 요청 다이얼로그를 다시 표시
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
                } else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun saveBitmapAsPdf(bitmap: Bitmap, pdfFile: File) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val canvas = page.canvas
        canvas.drawBitmap(bitmap, 0f, 0f, Paint())
        pdfDocument.finishPage(page)

        try {
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        pdfDocument.close()
    }

    fun saveActivityContentAsPdf(activity: Activity, outputPath: String) {
        val bitmap = getBitmapFromView(activity.window.decorView.rootView)
        val pdfFile = File(outputPath)
        saveBitmapAsPdf(bitmap, pdfFile)

        // 파일 경로 로그로 출력
        Log.d("PDF Save", "PDF saved to: $outputPath")
    }

}
