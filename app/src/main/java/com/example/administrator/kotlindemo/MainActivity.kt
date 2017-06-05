package com.example.administrator.kotlindemo

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.zbar.lib.CaptureActivity

@Suppress("JAVA_CLASS_ON_COMPANION")
class MainActivity : AppCompatActivity() {
    private lateinit  var  btn_qrcode: Button;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_qrcode= findViewById(R.id.btn_qrcode) as Button;
        btn_qrcode.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, CaptureActivity::class.java)
            startActivityForResult(intent, CaptureActivity.REQUEST_CODE_SCAN)

        })
    }

//    fun qrcode(view: View) {
//        val intent = Intent()
//        intent.setAction("com.zbar.lib.CaptureActivity")
//        startActivityForResult(intent, CaptureActivity.REQUEST_CODE_SCAN)
//
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 扫描二维码/条码回传
        if (requestCode == CaptureActivity.REQUEST_CODE_SCAN && resultCode == Activity.RESULT_OK) {
            if (data != null) {

                val content = data.getStringExtra(CaptureActivity.DECODED_CONTENT_KEY)
                if (!TextUtils.isEmpty(content)) {
                    Toast.makeText(this@MainActivity, "扫描结果：" + content, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
