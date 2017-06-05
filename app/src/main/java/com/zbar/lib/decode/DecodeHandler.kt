package com.zbar.lib.decode

import android.graphics.Bitmap
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message

import com.example.administrator.kotlindemo.R
import com.zbar.lib.CaptureActivity
import com.zbar.lib.ZbarManager
import com.zbar.lib.bitmap.PlanarYUVLuminanceSource

import java.io.File
import java.io.FileOutputStream

/**
 * 作者: 陈涛(1076559197@qq.com)

 * 时间: 2014年5月9日 下午12:24:13

 * 版本: V_1.0.0

 * 描述: 接受消息后解码
 */
internal class DecodeHandler(activity: CaptureActivity) : Handler() {

    var activity: CaptureActivity? = null

    init {
        this.activity = activity
    }

    override fun handleMessage(message: Message) {
        when (message.what) {
            R.id.decode -> decode(message.obj as ByteArray, message.arg1, message.arg2)
            R.id.quit -> Looper.myLooper()!!.quit()
        }
    }

    private fun decode(data: ByteArray, width: Int, height: Int) {
        var width = width
        var height = height
        val rotatedData = ByteArray(data.size)
        for (y in 0..height - 1) {
            for (x in 0..width - 1)
                rotatedData[x * height + height - y - 1] = data[x + y * width]
        }
        val tmp = width// Here we are swapping, that's the difference to #11
        width = height
        height = tmp

        val manager = ZbarManager()
        val result = manager.decode(rotatedData, width, height, true, activity!!.x, activity!!.y, activity!!.cropWidth,
                activity!!.cropHeight)

        if (result != null) {
            if (activity!!.isNeedCapture) {
                // 生成bitmap
                val source = PlanarYUVLuminanceSource(rotatedData, width, height, activity!!.x, activity!!.y,
                        activity!!.cropWidth, activity!!.cropHeight, false)
                val pixels = source.renderThumbnail()
                val w = source.thumbnailWidth
                val h = source.thumbnailHeight
                val bitmap = Bitmap.createBitmap(pixels, 0, w, w, h, Bitmap.Config.ARGB_8888)
                try {
                    val rootPath = Environment.getExternalStorageDirectory().absolutePath + "/Qrcode/"
                    val root = File(rootPath)
                    if (!root.exists()) {
                        root.mkdirs()
                    }
                    val f = File(rootPath + "Qrcode.jpg")
                    if (f.exists()) {
                        f.delete()
                    }
                    f.createNewFile()

                    val out = FileOutputStream(f)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    out.flush()
                    out.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            if (null != activity!!.handler) {
                val msg = Message()
                msg.obj = result
                msg.what = R.id.decode_succeeded
                activity!!.handler?.sendMessage(msg)
            }
        } else {
            if (null != activity!!.handler) {
                activity!!.handler?.sendEmptyMessage(R.id.decode_failed)
            }
        }
    }

}
