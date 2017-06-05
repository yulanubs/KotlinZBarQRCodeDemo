package com.zbar.lib.camera

import android.hardware.Camera
import android.os.Handler
import android.util.Log

/**
 * 作者: 陈涛(1076559197@qq.com)

 * 时间: 2014年5月9日 下午12:21:30

 * 版本: V_1.0.0

 * 描述: 相机自动对焦
 */
internal class AutoFocusCallback : Camera.AutoFocusCallback {

    private var autoFocusHandler: Handler? = null
    private var autoFocusMessage: Int = 0

    fun setHandler(autoFocusHandler: Handler?, autoFocusMessage: Int) {
        this.autoFocusHandler = autoFocusHandler
        this.autoFocusMessage = autoFocusMessage
    }

    override fun onAutoFocus(success: Boolean, camera: Camera) {
        if (autoFocusHandler != null) {
            val message = autoFocusHandler!!.obtainMessage(autoFocusMessage, success)
            autoFocusHandler!!.sendMessageDelayed(message, AUTOFOCUS_INTERVAL_MS)
            autoFocusHandler = null
        } else {
            Log.d(TAG, "Got auto-focus callback, but no handler for it")
        }
    }

    companion object {

        private val TAG = AutoFocusCallback::class.java.simpleName

        private val AUTOFOCUS_INTERVAL_MS = 1500L
    }

}
