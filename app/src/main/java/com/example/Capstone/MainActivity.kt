package com.example.Capstone

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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

    lateinit var editText: EditText
    private var toast: Toast? = null

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

        editText = findViewById<EditText>(R.id.editText)


    }
    override fun onResume() {
        super.onResume()
        editText.setText(loadName())
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
        if(loadName().toString().isNotEmpty()){
            val intent = Intent(
                applicationContext,
                MainActivity2::class.java
            )
            startActivity(intent)
        }
        else{
            if (toast != null) {
                toast!!.cancel()
            }
            toast = Toast.makeText(applicationContext, "닉네임을 입력해주세요", Toast.LENGTH_SHORT)
            toast!!.show()
        }
    }
    ////////////////////////////////////////////////////

    ////////////////////신고 현황 클릭////////////////////
    fun onButtonClick2(v: View?) {
        if(loadName().toString().isNotEmpty()){
            val intent = Intent(
                applicationContext,
                MainActivity4::class.java
            )
            startActivity(intent)
        }
        else{
            if (toast != null) {
                toast!!.cancel()
            }
            toast = Toast.makeText(applicationContext, "닉네임을 입력해주세요", Toast.LENGTH_SHORT)
            toast!!.show()
        }
    }
    ////////////////////////////////////////////////////

    // 이름 제출 클릭
    fun onButtonClick3(v: View?) {
        saveName()
    }


    // 이름 저장
    private fun saveName() {
        if(editText.text.toString().isNotEmpty()){
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("savedName", editText.text.toString())
            editor.apply()
            Log.d(TAG, "닉네임이 저장되었습니다: ${editText.text}")
            if (toast != null) {
                toast!!.cancel()
            }
            toast = Toast.makeText(applicationContext, "닉네임이 저장되었습니다", Toast.LENGTH_SHORT)
            toast!!.show()
        }
        else{
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("savedName", editText.text.toString())
            editor.apply()
            Log.d(TAG, "닉네임이 비어 있어 저장되지 않았습니다.")
            if (toast != null) {
                toast!!.cancel()
            }
            toast = Toast.makeText(applicationContext, "닉네임을 입력해주세요", Toast.LENGTH_SHORT)
            toast!!.show()
        }
    }

    // 저장된 이름 불러오기
    private fun loadName(): String? {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val savedName = sharedPreferences.getString("savedName", "")
        return savedName
    }
    ////////////////////////////////////////////////////
}