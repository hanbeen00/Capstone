package com.example.Capstone

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private val TAG = javaClass.simpleName
    private lateinit var mMyAPI: MyAPI

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        val mActionBarToolbar: Toolbar
        mActionBarToolbar = findViewById<View>(R.id.toolbar) as Toolbar
        mActionBarToolbar.setTitle("Just Dot It")
        setSupportActionBar(mActionBarToolbar)
        ////////////////////////////////////////////////////

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
                        val itemsToDelete = postItems.filter { it.time == "" }

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

    ////////////////////점자 검사 클릭////////////////////
    fun onButtonClick1(v: View?) {
        val intent = Intent(
            applicationContext,
            MainActivity2::class.java
        )
        startActivity(intent)
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
}