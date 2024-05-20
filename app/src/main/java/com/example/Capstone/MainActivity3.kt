package com.example.Capstone

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//////////////////////// GPS 권한 처리에 필요한 변수 ////////////////////////
@RequiresApi(Build.VERSION_CODES.Q)
val GPS = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION)
/////////////////////////////////////////////////////////////////////////

//////////////////////// 기능 코드 ////////////////////////
val GPS_CODE = 100
/////////////////////////////////////////////////////////

//////////// Android에서 위치 기반 서비스를 사용하기 위한 클라이언트 변수 ////////////
lateinit var fusedLocationProviderClient: FusedLocationProviderClient
/////////////////////////////////////////////////////////////////////////////

class MainActivity3 : AppCompatActivity() {

    //////////////////////// 텍스트 뷰 선언 ////////////////////////
    lateinit var textView: TextView
    lateinit var editText: EditText
    lateinit var reportText: EditText
    var location_give: String? = null
    //////////////////////////////////////////////////////////////

    private val TAG = javaClass.simpleName
    private lateinit var mMyAPI: MyAPI
    private var lastId: Int = 0 // Initialize with a default value

    private var toast: Toast? = null


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        //////////////////// 툴바 설정 ////////////////////
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_main3)
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
        mActionBarToolbar.setTitle("신고하기")
        setSupportActionBar(mActionBarToolbar)
        ////////////////////////////////////////////////////

        // Intent extras에서 'last' 변수 값을 검색합니다.
        lastId = intent.getIntExtra("LAST_ID", 0)

        //////////////////// 텍스트 뷰 초기화 ////////////////////
        textView = findViewById<TextView>(R.id.textView)
        editText = findViewById<EditText>(R.id.EditText)
        reportText = findViewById<EditText>(R.id.ReportView)
        ////////////////////////////////////////////////////////


        //////////////////// 버튼 초기화 ////////////////////
        val getpos = findViewById<Button>(R.id.gps)
        ////////////////////////////////////////////////////////

        //////////////////// GPS버튼 누르면 위치정보 표시 ////////////////////
        getpos.setOnClickListener {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            CallGPS()
        }
        //////////////////////////////////////////////////////////////////

        // server의 url을 적어준다
        val BASE_URL = PostItem.BASE_URL    // 서버 링크 주소 넣기(현재 링크는 로컬서버임)
        initMyAPI(BASE_URL)


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

    //////////////////// 신고 접수 클릭 ////////////////////
    @SuppressLint("SimpleDateFormat")
    fun onButtonClick1(v: View?) {

        if(editText.text.toString().trim().isNotEmpty() && location_give != null && reportText.text.toString().trim().isNotEmpty()){

            val currentTime : Long = System.currentTimeMillis() // ms로 반환
            println(currentTime)
            val dataFormat1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") // 년 월 일


            Log.d(TAG, "PATCH")
            val item = PostItem()   // 새로운 PostItem 객체 생성
            item.address = textView.text.toString().trim()  // 새로운 PostItem 객체의 address 속성을 설정
            item.text = editText.text.toString().trim()   // PostItem 객체의 text 속성을 설정
            item.report = "처리전"
            item.time = dataFormat1.format(Date(currentTime))
            item.information = reportText.text.toString().trim()

            //pk 값은 동적으로 setting 해서 사용가능
            val patchCall = mMyAPI.patch_posts(lastId, item)
            // 마지막 항목의 id와 수정할 데이터를 전달하여 PATCH 요청을 수행하는 Retrofit Call 객체 생성

            patchCall.enqueue(object : Callback<PostItem> {
                // Retrofit Call 객체에 대한 비동기적인 실행 및 결과 처리를 위한 Callback 객체 설정
                override fun onResponse(call: Call<PostItem>, response: Response<PostItem>) {
                    // 성공적인 응답을 받았을 때 호출되는 메서드

                    if (response.isSuccessful) {
                        Log.d(TAG, "patch 성공")
                        if (toast != null) {
                            toast!!.cancel()
                        }
                        toast = Toast.makeText(applicationContext, "신고 접수 되었습니다\n" + item.time, Toast.LENGTH_SHORT)
                        toast!!.show()

                        val intent = Intent(
                            applicationContext,
                            MainActivity4::class.java
                        )
                        startActivity(intent)
                        finish()

                    } else {
                        Log.d(TAG, "Status Code : ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<PostItem>, t: Throwable) {
                    Log.d(TAG, "Fail msg : ${t.message}")
                    if (toast != null) {
                        toast!!.cancel()
                    }
                    toast = Toast.makeText(applicationContext, "인터넷 연결을 확인하세요.", Toast.LENGTH_SHORT)
                    toast!!.show()
                }
            })

        }

        else if(location_give == null){
            if (toast != null) {
                toast!!.cancel()
            }
            toast = Toast.makeText(applicationContext,  "'현재 위치 확인'을 눌러주세요", Toast.LENGTH_SHORT)
            toast!!.show()
        }

        else if(editText.text.toString().trim().isEmpty()){
            if (toast != null) {
                toast!!.cancel()
            }
            toast = Toast.makeText(applicationContext,  "상세 위치를 입력해주세요", Toast.LENGTH_SHORT)
            toast!!.show()
        }
        else{
            if (toast != null) {
                toast!!.cancel()
            }
            toast = Toast.makeText(applicationContext,  "신고 내용을 작성해주세요", Toast.LENGTH_SHORT)
            toast!!.show()
        }
    }
    //////////////////////////////////////////////////////

    //////////////////// 신고 현황 클릭 ////////////////////
    fun onButtonClick2(v: View?) {

        //////////////////// 리스트 뷰로 데이터 전달 및 화면전환 ////////////////////
        val intent = Intent(
            applicationContext,
            MainActivity4::class.java
        )
        intent.putExtra("LAST_ID", lastId) // Pass the 'last' variable to MainActivity3
        startActivity(intent)
        /////////////////////////////////////////////////////////////

    }
    //////////////////////////////////////////////////////

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
    //////////////////////////////////////////////////////

    //////////////////// 위치 정보 가져오기 ////////////////////
    @RequiresApi(Build.VERSION_CODES.Q)
    fun CallGPS(){
        if(checkPermission(GPS, GPS_CODE)){
            getLastLocation()
        }
    }
    /////////////////////////////////////////////////////////

    //////////////////// 현재 위치 가져오기 ////////////////////
    @SuppressLint("SetTextI18n")
    private fun getLastLocation() {
        if (isLocationEnabled()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                val location: Location? = task.result
                if (location == null) {
                    NewLocationData()
                } else {
                    Log.d("Debug:", "Your Location:" + location.longitude)
                    location_give =
                        getCityName(
                            location.latitude,
                            location.longitude
                        )
                    textView.text = location_give
                }
            }
        } else {
            if (toast != null) {
                toast!!.cancel()
            }
            toast = Toast.makeText(applicationContext,  "GPS를 켜주세요", Toast.LENGTH_SHORT)
            toast!!.show()

        }
    }
    /////////////////////////////////////////////////////////

    //////////////////// GPS 켜져 있는지 확인 //////////////////
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    /////////////////////////////////////////////////////////

    //////////////////// 위치 정보 업데이트 ////////////////////
    private fun NewLocationData() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()
        )
    }
    /////////////////////////////////////////////////////////

    /////////////////// 위치 정보 불러와서 작성 /////////////////
    private val locationCallback = object : LocationCallback() {
        @SuppressLint("SetTextI18n")
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location = locationResult.lastLocation!!
            Log.d("Debug:", "your last last location: " + lastLocation.longitude.toString())
            location_give =
                getCityName(
                    lastLocation.latitude,
                    lastLocation.longitude
                )
            textView.text = location_give
        }
    }
    /////////////////////////////////////////////////////////

    ///////////////////// 위치 상세 이름 제공 //////////////////
    private fun getCityName(lat: Double, long: Double): String {
        var nowAddr = "현재 위치를 확인 할 수 없습니다."
        val geocoder = Geocoder(this, Locale.KOREA)
        val address: List<Address>?

        try {
            address = geocoder.getFromLocation(lat, long, 1)
            if (!address.isNullOrEmpty()) {
                nowAddr = address[0].getAddressLine(0).toString()
            }
        } catch (e: IOException) {
            if (toast != null) {
                toast!!.cancel()
            }
            toast = Toast.makeText(applicationContext,  "주소를 가져 올 수 없습니다.", Toast.LENGTH_SHORT)
            toast!!.show()
            e.printStackTrace()
        }
        return nowAddr
    }
    /////////////////////////////////////////////////////////
}


