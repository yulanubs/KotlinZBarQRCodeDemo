package com.zbar.lib.decode

import android.os.Handler
import android.os.Message
import com.example.administrator.kotlindemo.R
import com.zbar.lib.CaptureActivity
import com.zbar.lib.camera.CameraManager

/**
 * 作者: 陈涛(1076559197@qq.com)

 * 时间: 2014年5月9日 下午12:23:32

 * 版本: V_1.0.0

 * 描述: 扫描消息转发
 */
class CaptureActivityHandler(activity: CaptureActivity) : Handler() {

    internal var decodeThread: DecodeThread? = null
    internal var activity: CaptureActivity? = null
    private var state: State? = null

    private enum class State {
        PREVIEW, SUCCESS, DONE
    }

    init {
        this.activity = activity
        decodeThread = DecodeThread(activity)
        decodeThread!!.start()
        state = State.SUCCESS
        CameraManager.get().startPreview()
        restartPreviewAndDecode()
    }

    override fun handleMessage(message: Message) {

        when (message.what) {
            R.id.auto_focus -> if (state == State.PREVIEW) {
                CameraManager.get().requestAutoFocus(this, R.id.auto_focus)
            }
            R.id.restart_preview -> restartPreviewAndDecode()
            R.id.decode_succeeded -> {
                state = State.SUCCESS
                activity!!.handleDecode(message.obj as String)// 解析成功，回调
            }

            R.id.decode_failed -> {
                state = State.PREVIEW
                CameraManager.get().requestPreviewFrame(decodeThread!!.getHandler(),
                        R.id.decode)
            }
        }

    }

    fun quitSynchronously() {
        state = State.DONE
        CameraManager.get().stopPreview()
        removeMessages(R.id.decode_succeeded)
        removeMessages(R.id.decode_failed)
        removeMessages(R.id.decode)
        removeMessages(R.id.auto_focus)
    }

    private fun restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW
            CameraManager.get().requestPreviewFrame(decodeThread!!.getHandler(),
                    R.id.decode)
            CameraManager.get().requestAutoFocus(this, R.id.auto_focus)
        }
    }

}
