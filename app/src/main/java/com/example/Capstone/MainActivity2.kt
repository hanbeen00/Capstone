package com.example.Capstone

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.soundcloud.android.crop.Crop
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat


// storage 및 카메라 권한 처리에 필요한 변수
val CAMERA = arrayOf(Manifest.permission.CAMERA)
val STORAGE1 = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE)


// 버전 높을 경우 storage 권한
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
val STORAGE2 = arrayOf(Manifest.permission.READ_MEDIA_IMAGES)

// 각 기능 코드
val CAMERA_CODE = 98
val STORAGE_CODE = 99


//////////////////////// 이미지 파일 바이트 배열로 저장  /////////////////////
var BYTE_CODE: ByteArray? = null
/////////////////////////////////////////////////////////////////////////

//@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Suppress("DEPRECATION")
class MainActivity2 : AppCompatActivity() {

    // 텍스트 뷰 선언
    lateinit var textView: TextView
    private val TAG = javaClass.simpleName
    private lateinit var mMyAPI: MyAPI

    private var last = ""    // PATCH 할 경우 이용할 변수(맨 뒤 데이터 PATCH)
    private var lastid = 0

    private var toast: Toast? = null
    private var realUri: Uri? = null

    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        ////////////////////툴바 설정////////////////////
        this.enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val mToolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val mActionBarToolbar: Toolbar
        mActionBarToolbar = findViewById<View>(R.id.toolbar) as Toolbar
        mActionBarToolbar.setTitle("검사하기")
        setSupportActionBar(mActionBarToolbar)
        ////////////////////////////////////////////////////

        ////////////////////텍스트 뷰 초기화 및 밑줄 표시////////////////////
        textView = findViewById<TextView>(R.id.textView2)
        textView.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        imageView = findViewById<ImageView>(R.id.avatars)
        /////////////////////////////////////////////////////////////////
        textView.text = ""


        ////////////////////카메라 초기화 및 카메라 실행////////////////////
        val camera = findViewById<Button>(R.id.camera)

        camera.setOnClickListener {
            textView.text = ""
            openCamera()
        }
        ////////////////////////////////////////////////////////////////

        ////////////////////앨범 초기화 및 앨범 불러오기////////////////////
        val picture = findViewById<Button>(R.id.picture)
        picture.setOnClickListener {
            textView.text = ""
            GetAlbum()
        }
        ////////////////////////////////////////////////////////////////

        // server의 url을 적어준다
        // 서버 링크 주소 넣기(현재 링크는 로컬서버임)
        initMyAPI(PostItem.BASE_URL)
    }


    // Retrofit 인스턴스를 사용하여 MyAPI 인터페이스를 구현한 클래스 생성 및 mMyAPI에 할당 //
    private fun initMyAPI(baseUrl: String) {
        Log.d(TAG, "initMyAPI : $baseUrl")
        val retrofit = Retrofit.Builder()   // Retrofit.Builder 인스턴스 생성
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        mMyAPI = retrofit.create(MyAPI::class.java)
    }
    ///////////////////////////////////////////////////////////////////////////////

    override fun onBackPressed() {
        BYTE_CODE = null
        super.onBackPressed()
        // MainActivity 로 이동
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // 현재 Activity 종료
    }


    ////////////////////신고하기 클릭////////////////////
    fun onButtonClick1(v: View?) {
        if(lastid != 0 && last != ""){
            if(last == "점자 이미지가 아닙니다"){
                if (toast != null) {
                    toast!!.cancel()
                }
                toast = Toast.makeText(applicationContext, "점자 이미지가 아닙니다", Toast.LENGTH_SHORT)
                toast!!.show()
            }
            else{
                val intent = Intent(
                    applicationContext,
                    MainActivity3::class.java
                )
                intent.putExtra("LAST_ID", lastid) // Pass the 'last' variable to MainActivity3
                startActivity(intent)
                finish()
            }
        }
        else if(BYTE_CODE == null){
            if (toast != null) {
                toast!!.cancel()
            }
            toast = Toast.makeText(applicationContext, "점자 이미지를 불러오세요", Toast.LENGTH_SHORT)
            toast!!.show()
        }
        else{
            if (toast != null) {
                toast!!.cancel()
            }
            toast = Toast.makeText(applicationContext, "'점자 확인'을 눌러주세요", Toast.LENGTH_SHORT)
            toast!!.show()
        }
    }
    ////////////////////////////////////////////////////

    ////////////////////신고 현황 클릭////////////////////
    fun onButtonClick2(v: View?) {
        val intent = Intent(
            applicationContext,
            MainActivity4::class.java
        )
        startActivity(intent)
    }
    ////////////////////////////////////////////////////

    ////////////////////점자 확인 클릭////////////////////
    fun onButtonClick3(v: View?) {

        if(BYTE_CODE!=null){
            Log.d(TAG, "GET")
            val getCall = mMyAPI.get_posts()
            // MyAPI 인터페이스의 get_posts() 메서드를 호출하여 GET 요청을 수행하는 Retrofit Call 객체 생성
            getCall.enqueue(object : Callback<List<PostItem>> {
                // Retrofit Call 객체에 대한 비동기적인 실행 및 결과 처리를 위한 Callback 객체 설정

                override fun onResponse(call: Call<List<PostItem>>, response: Response<List<PostItem>>) {
                    // 성공적인 응답을 받았을 때 호출되는 메서드

                    if (response.isSuccessful) {
                        val mList = response.body()   // 응답에서 목록 데이터를 가져옴
                        var result = ""               // 결과 문자열 초기화
                        mList?.forEach { item ->      // 목록 데이터를 순회하면서 각 항목을 처리
                            last = item.result
                            lastid = item.id
                        }
                        result += "${last}\n"
                        if(last == ""){
                            if (toast != null) {
                                toast!!.cancel()
                            }
                            toast = Toast.makeText(applicationContext, "점자 번역 중 입니다.", Toast.LENGTH_SHORT)
                            toast!!.show()
                        }
                        else{
                            textView.text = result   // 결과 문자열을 TextView에 설정하여 화면에 표시
                            if(last == "점자 이미지가 아닙니다"){
                                BYTE_CODE = null
                            }
                        }
                    } else {
                        Log.d(TAG, "Status Code : ${response.code()}")
                    }
                }
                override fun onFailure(call: Call<List<PostItem>>, t: Throwable) {
                    Log.d(TAG, "Fail msg : ${t.message}")
                    if (toast != null) {
                        toast!!.cancel()
                    }
                    toast = Toast.makeText(applicationContext, "인터넷 연결을 확인하세요.", Toast.LENGTH_SHORT)
                    toast!!.show()
                }
            })
        }
        else{
            if (toast != null) {
                toast!!.cancel()
            }
            toast = Toast.makeText(applicationContext, "점자 이미지를 불러오세요", Toast.LENGTH_SHORT)
            toast!!.show()
        }
    }
    ////////////////////////////////////////////////////


    ////////////////////각 기능 권한 확인////////////////////
    fun checkPermission(permissions: Array<out String>, type:Int):Boolean{
        for (permission in permissions){
            if(ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, permissions, type)
                return false
            }
        }
        return true
    }
    ////////////////////////////////////////////////////



    fun openCamera(){
        if(if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkPermission(CAMERA, CAMERA_CODE) && checkPermission(STORAGE2, STORAGE_CODE)
            } else {
                checkPermission(CAMERA, CAMERA_CODE) && checkPermission(STORAGE1, STORAGE_CODE)
            }
        ){
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            createImageUri(newFileName(), "image/jpg")?.let { uri ->
                realUri = uri
                intent.putExtra(MediaStore.EXTRA_OUTPUT, realUri)
                startActivityForResult(intent, CAMERA_CODE)
            }
        }
    }

    fun createImageUri(fileName: String, mimeType: String) : Uri? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)

        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)
    }

    fun newFileName(): String{
        val sdf = SimpleDateFormat("yyyyMMddHHmmss")
        val filename = sdf.format(System.currentTimeMillis())
        return "${filename}.jpg"
    }

    fun loadBitmap(photoUri:Uri) : Bitmap?{
        var image:Bitmap? = null
        try {
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1){
                val source = ImageDecoder.createSource(contentResolver, photoUri)
                image = ImageDecoder.decodeBitmap(source)
            } else{
                image = MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        return image
    }


    ////////////////////갤러리에서 사진가져오기////////////////////
    fun GetAlbum(){
        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(checkPermission(STORAGE2, STORAGE_CODE)){
                val itt = Intent(Intent.ACTION_PICK)
                itt.type = MediaStore.Images.Media.CONTENT_TYPE
                startActivityForResult(itt, STORAGE_CODE)
            }
        }
        else {
            if(checkPermission(STORAGE1, STORAGE_CODE)){
                val itt = Intent(Intent.ACTION_PICK)
                itt.type = MediaStore.Images.Media.CONTENT_TYPE
                startActivityForResult(itt, STORAGE_CODE)
            }
        }

    }
    ////////////////////////////////////////////////////

    /////////////////이전에 시작된 활동이 종료되고 결과를 반환할 때 호출/////////////////
    /////////////////이미지뷰에 사진 출력/////////////////
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_CODE -> {

                    /////////////////////////////////////////////
                    imageView.setImageURI(realUri)
                    val inputStream = realUri?.let { contentResolver.openInputStream(realUri!!) }
                    val bytes = inputStream?.readBytes()
                    BYTE_CODE = bytes
                    ////////////////////////////////////////////////////////////

                    Log.d(TAG, "POST")
                    uploadBytesToServer(BYTE_CODE!!, "","", "","사진 결과","")
                }

                STORAGE_CODE -> {
                    val uri = data?.data

                    /////////////////////////////////////////////
                    imageView.setImageURI(uri)
                    val inputStream = uri?.let { contentResolver.openInputStream(uri) }
                    val bytes = inputStream?.readBytes()
                    BYTE_CODE = bytes
                    ////////////////////////////////////////////////////////////

                    Log.d(TAG, "POST")
                    uploadBytesToServer(BYTE_CODE!!, "","", "","사진 결과","")
                }
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////

    private fun rotateImageIfRequired(img: Bitmap, selectedImage: Uri): Bitmap {
        val input = contentResolver.openInputStream(selectedImage)
        val ei = ExifInterface(input!!)
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
            else -> img
        }
    }

    private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    }

    ///////// 서버에 통합내용(이미지, 위치, 텍스트) 전송 ///////////
    private fun uploadBytesToServer(bytes: ByteArray, address: String, text: String, report:String, result:String, information:String) {
        val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
        // 바이트 배열을 RequestBody로 변환하여 이미지를 나타내는 요청 본문 생성

        val imagePart = MultipartBody.Part.createFormData("image", "image.jpg", requestBody)
        // 이미지를 나타내는 MultipartBody.Part 객체 생성

        val addressPart = address.toRequestBody("text/plain".toMediaTypeOrNull())
        // 주소를 나타내는 요청 본문 생성

        val textPart = text.toRequestBody("text/plain".toMediaTypeOrNull())
        // 텍스트를 나타내는 요청 본문 생성

        val reportPart = report.toRequestBody("text/plain".toMediaTypeOrNull())
        val resultPart = result.toRequestBody("text/plain".toMediaTypeOrNull())
        val informationPart = information.toRequestBody("text/plain".toMediaTypeOrNull())

        val call = mMyAPI.post_posts(imagePart, addressPart, textPart, reportPart, resultPart, informationPart)
        // post_posts 메서드를 호출하여 이미지와 주소, 텍스트를 서버에 업로드하는 Retrofit Call 객체 생성

        call.enqueue(object : Callback<List<PostItem>> {
            // post_posts 메서드를 호출하여 이미지와 주소, 텍스트를 서버에 업로드하는 Retrofit Call 객체 생성

            override fun onResponse(call: Call<List<PostItem>>, response: Response<List<PostItem>>) {
                // Retrofit Call 객체에 대한 비동기적인 실행 및 결과 처리를 위한 Callback 객체 설정

                if (response.isSuccessful) {
                    Log.d(TAG, "patch 성공")
                } else {
                    Log.d(TAG, "Status Code : ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<PostItem>>, t: Throwable) {
                Log.d(TAG, "Fail msg : ${t.message}")
                if (toast != null) {
                    toast!!.cancel()
                }
                toast = Toast.makeText(applicationContext, "인터넷 연결을 확인하세요.", Toast.LENGTH_SHORT)
                toast!!.show()
            }
        })

    }
    /////////////////////////////////////////////////////////

}


