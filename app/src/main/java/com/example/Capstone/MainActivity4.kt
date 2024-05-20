package com.example.Capstone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity4 : AppCompatActivity() {

    ////////////////////리스트 뷰 선언////////////////////
    lateinit var listView: ListView
    lateinit var adapter: ListItemAdapter
    ////////////////////////////////////////////////////

    private var toast: Toast? = null
    private var lastId: Int = 0 // Initialize with a default value

    private val TAG = javaClass.simpleName
    private lateinit var mMyAPI: MyAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)

        ////////////////////툴바 설정////////////////////
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_main4)
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
        mActionBarToolbar.setTitle("신고 진행 현황")
        setSupportActionBar(mActionBarToolbar)
        ////////////////////////////////////////////////////

        ////////////////////리스트 뷰 설정////////////////////
        listView = findViewById(R.id.listview)
        ////////////////////////////////////////////////////

        lastId = intent.getIntExtra("LAST_ID", 0)

        // server의 url을 적어준다
        val BASE_URL = PostItem.BASE_URL    // 서버 링크 주소 넣기(현재 링크는 로컬서버임)
        initMyAPI(BASE_URL)

        Log.d(TAG, "GET")
        val getCall = mMyAPI.get_posts() // MyAPI 인터페이스의 get_posts() 메서드를 호출

        getCall.enqueue(object : Callback<List<PostItem>> {
            override fun onResponse(call: Call<List<PostItem>>, response: Response<List<PostItem>>) {
                if (response.isSuccessful) {
                    val mList = response.body() // 응답에서 목록 데이터를 가져옴
                    adapter = ListItemAdapter() // 어댑터를 생성
                    var listnum = 0

                    mList?.forEach { item ->
                        if (item.report != "") { // item.report가 null이 아닌 경우에만 실행
                            listnum++;
                            adapter.addItem(ListItem(listnum.toString(), item.address, item.report, item.text, item.time))
                            // 어댑터에 각 항목 추가
                        }
                    }

                    listView.adapter = adapter // 리스트뷰에 어댑터 설정
                    adapter.notifyDataSetChanged() // 데이터 변경을 알림
                } else {
                    Log.d(TAG, "Status Code : ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<PostItem>>, t: Throwable) {
                // 요청 실패 시 처리
                Log.d(TAG, "Error: ${t.message}")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // onResume에서 deletePosts 호출
        deletePosts()
    }

    private fun deletePosts() {
        initMyAPI(PostItem.BASE_URL)

        val getCall = mMyAPI.get_posts()

        getCall.enqueue(object : Callback<List<PostItem>> {
            override fun onResponse(call: Call<List<PostItem>>, response: Response<List<PostItem>>) {
                if (response.isSuccessful) {
                    val postItems = response.body()

                    if (postItems != null) {
                        // time 필드가 null인 PostItem을 찾기
                        val itemsToDelete = postItems.filter { it.id != lastId && it.time == ""}

                        if (itemsToDelete.isNotEmpty()) {
                            itemsToDelete.forEach { itemToDelete ->
                                val deleteCall = mMyAPI.delete_posts(itemToDelete.id!!)
                                deleteCall.enqueue(object : Callback<PostItem> {
                                    override fun onResponse(call: Call<PostItem>, response: Response<PostItem>) {
                                        if (response.isSuccessful) {
                                            Log.d(TAG, "삭제 완료")
                                        } else {
                                            Log.d(TAG, "Status Code : ${response.code()}")
                                        }
                                    }

                                    override fun onFailure(call: Call<PostItem>, t: Throwable) {
                                        Log.d(TAG, "Fail msg : ${t.message}")
                                    }
                                })
                            }
                        } else {
                            Log.d(TAG, "삭제할 항목이 없습니다.")
                        }
                    } else {
                        Log.d(TAG, "PostItem 목록이 비어 있습니다.")
                    }
                } else {
                    Log.d(TAG, "Status Code : ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<PostItem>>, t: Throwable) {
                Log.d(TAG, "Fail msg : ${t.message}")
            }
        })
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

    /*override fun onBackPressed() {
        super.onBackPressed()
        // MainActivity 로 이동
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // 현재 Activity 종료
    }*/

    fun onButtonClick1(view: View) {
        Log.d(TAG, "GET")
        val getCall = mMyAPI.get_posts() // MyAPI 인터페이스의 get_posts() 메서드를 호출

        getCall.enqueue(object : Callback<List<PostItem>> {
            override fun onResponse(call: Call<List<PostItem>>, response: Response<List<PostItem>>) {
                if (response.isSuccessful) {
                    val mList = response.body() // 응답에서 목록 데이터를 가져옴
                    adapter = ListItemAdapter() // 어댑터를 생성
                    var listnum = 0

                    mList?.forEach { item ->
                        if (item.report!= "" && item.time != "") { // item.report, time이 null이 아닌 경우에만 실행
                            listnum++;
                            adapter.addItem(ListItem(listnum.toString(), item.address, item.report, item.text, item.time))
                            // 어댑터에 각 항목 추가
                        }
                    }

                    listView.adapter = adapter // 리스트뷰에 어댑터 설정
                    adapter.notifyDataSetChanged() // 데이터 변경을 알림


                    // 이미 정의된 toast를 사용하여 토스트 메시지 표시
                    if (toast != null) {
                        toast!!.cancel()
                    }
                    toast = Toast.makeText(applicationContext, "업데이트 했습니다", Toast.LENGTH_SHORT)
                    toast!!.show()


                } else {
                    Log.d(TAG, "Status Code : ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<PostItem>>, t: Throwable) {
                // 요청 실패 시 처리
                Log.d(TAG, "Error: ${t.message}")
                if (toast != null) {
                    toast!!.cancel()
                }
                toast = Toast.makeText(applicationContext, "인터넷 연결을 확인하세요.", Toast.LENGTH_SHORT)
                toast!!.show()
            }
        })
    }


}