package com.laondruk.parcel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.googlecode.tesseract.android.TessBaseAPI
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    init {
        Log.i(TAG_IMAGE_PROCESSING, "OpenCV Started: ${OpenCVLoader.initDebug()}")

        cameraExecutor = Executors.newSingleThreadExecutor()
        captureExecutor = Executors.newSingleThreadExecutor()
        imageProcessingExecutor = Executors.newSingleThreadExecutor()
        dataSearchExecutor = Executors.newSingleThreadExecutor()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        tessBaseAPI = TessBaseAPI()

        if (!File("$dataDir/tessdata").exists()) File("$dataDir/tessdata").mkdirs()
        if (!File("$dataDir/tessdata/kor.traineddata").exists()) FileOutputStream("$dataDir/tessdata/kor.traineddata").write(
            assets.open("tessdata/kor.traineddata").readBytes()
        )
        if (!File("$dataDir/tessdata/eng.traineddata").exists()) FileOutputStream("$dataDir/tessdata/eng.traineddata").write(
            assets.open("tessdata/eng.traineddata").readBytes()
        )

        Log.d(
            TAG_IMAGE_PROCESSING,
            "Tesseract API initated: ${tessBaseAPI.init("$dataDir", "kor+eng")}"
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()


        // 카메라 권한
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        findViewById<ImageButton>(R.id.CaptureButton).setOnClickListener { takePhoto() }

        outputDirectory = getOutputDirectory()
        database = getInstance(applicationContext).studentDao()
    }


    // 사진 촬영하기
    @RequiresApi(Build.VERSION_CODES.N)
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val imagePreView = findViewById<LinearLayout>(R.id.ImagePreView)
        val scrollView = findViewById<ScrollView>(R.id.ScrollView)

        val context = this


        val fileName = System.currentTimeMillis().toString() + ".jpg"
        val photoFile = File(outputDirectory, fileName)

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(photoFile)
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(baseContext, "${Uri.fromFile(photoFile)}", Toast.LENGTH_SHORT)
                        .show()

                    val bitmapImage = BitmapFactory.decodeFile("$outputDirectory/$fileName")

                    val imageView = ImageView(context)
                    val frameLayout = FrameLayout(context)

                    val imageOnLine = ImageOnLine(context, frameLayout)

                    imageView.setImageBitmap(bitmapImage)
                    imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    imageView.adjustViewBounds = true


                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 0, 0, 8)
                    imageView.layoutParams = params

                    frameLayout.addView(imageView)

                    imagePreView.addView(frameLayout)

                    scrollView.post {
                        scrollView.smoothScrollTo(0, imagePreView.bottom)
                    }

                    imageOnLine.setProcessingStatIcon()
                    imageView.setOnClickListener {
                        startActivity(
                            Intent(this@MainActivity, DataSearchActivity::class.java)
                                .putExtra("imagePath", "$outputDirectory/$fileName")
                        )
                    }

                    Log.i(TAG_IMAGE_PROCESSING, "Image Processing Started")
                    thread {
                        val mat = Mat()
                        Utils.bitmapToMat(bitmapImage, mat)
                        val text = ImageProcessing().detect(mat)
                        Log.i(TAG_IMAGE_PROCESSING, "Processed $text")
                        val texts = mutableListOf<String>()

                        val _text = text.replace("\n", " ").split(" ")
                        for (i in _text) {
                            if (!(i == "")) {
                                texts.add(i)
                            }
                        }

                        TODO("데이터 검색")
//                        for (i in texts) {
//                                } catch (e: StringIndexOutOfBoundsException) {
//                                }
//                            }
//                        }
//
                        val result = arrayOf(Student(10124, 1, 1, 1, "홍길동"))

                        Log.i(TAG_IMAGE_PROCESSING, "data search done")

                        if (result.size == 1) {
                            runOnUiThread {
                                imageView.setOnClickListener {
                                    startActivity(
                                        Intent(this@MainActivity, DataSearchActivity::class.java)
                                            .putExtra("imagePath", "$outputDirectory/$fileName")
                                            .putExtra("grade", result[0].Grade)
                                            .putExtra("klass", result[0].Klass)
                                            .putExtra("number", result[0].Number)
                                            .putExtra("name", result[0].Name)
                                            .putExtra("nameAnnotation", result[0].NameAnnotation)
                                    )
                                }
                                imageOnLine.setProcessingStatIcon(0)
                            }
                        } else if (result.size >= 1) {
                                runOnUiThread {
                                    imageView.setOnClickListener {
                                        startActivity(
                                            android.content.Intent(
                                                this@MainActivity,
                                                com.laondruk.parcel.DataSearchActivity::class.java
                                            )
                                                .putExtra(
                                                    "imagePath",
                                                    "${com.laondruk.parcel.MainActivity.Companion.outputDirectory}/$fileName"
                                                )
                                                .putExtra("grade", result.Grade)
                                                .putExtra("klass", result.Klass)
                                                .putExtra("number", result.Number)
                                                .putExtra("name", result.Name)
                                                .putExtra("nameAnnotation", result.NameAnnotation)
                                        )
                                    }
                                }
                            } else {
                            runOnUiThread {
                                imageView.setOnClickListener {
                                    startActivity(
                                        Intent(this@MainActivity, DataSearchActivity::class.java)
                                            .putExtra("imagePath", "$outputDirectory/$fileName")
                                    )
                                }
                                imageOnLine.setProcessingStatIcon(-1)
                            }
                        }

                    }

                    Log.i(TAG_IMAGE_PROCESSING, "Image Processing Ended")
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.i(
                        TAG_IMAGE_PROCESSING,
                        "Photo capture failed: ${exception.message}",
                        exception
                    )
                }
            }
        )
    }


    // 카메라 미리보기 세팅
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val viewFinder = findViewById<PreviewView>(R.id.viewFinder)

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG_CAMERA, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    // 권한 관련
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        captureExecutor.shutdown()
        imageProcessingExecutor.shutdown()
        dataSearchExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        const val TAG_IMAGE_PROCESSING = "ImageProcessing"
        const val TAG_CAMERA = "CAMERA"

        private lateinit var outputDirectory: File

        lateinit var tessBaseAPI: TessBaseAPI

        private var imageCapture: ImageCapture? = null
        private lateinit var cameraExecutor: ExecutorService
        private lateinit var captureExecutor: ExecutorService
        private lateinit var imageProcessingExecutor: ExecutorService
        lateinit var dataSearchExecutor: ExecutorService

        val imageQueue: Queue<String> = LinkedList()
        private lateinit var database: StudentDao
    }
}