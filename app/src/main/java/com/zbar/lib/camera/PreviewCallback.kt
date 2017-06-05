package com.zbar.lib.camera

import android.hardware.Camera
import android.os.Handler
import android.util.Log

/**
 * 作者: 陈涛(1076559197@qq.com)

 * 时间: 2014年5月9日 下午12:23:14

 * 版本: V_1.0.0

 * 描述: 相机预览回调
 */
internal class PreviewCallback(private val configManager: CameraConfigurationManager, private val useOneShotPreviewCallback: Boolean) : Camera.PreviewCallback {
    private var previewHandler: Handler? = null
    private var previewMessage: Int = 0

    fun setHandler(previewHandler: Handler?, previewMessage: Int) {
        this.previewHandler = previewHandler
        this.previewMessage = previewMessage
    }

    override fun onPreviewFrame(data: ByteArray, camera: Camera) {
        val cameraResolution = configManager.cameraResolution
        if (!useOneShotPreviewCallback) {
            camera.setPreviewCallback(null)
        }
        if (previewHandler != null) {
            val message = previewHandler!!.obtainMessage(previewMessage, cameraResolution!!.x,
                    cameraResolution.y, data)
            message.sendToTarget()
            previewHandler = null
        } else {
            Log.d(TAG, "Got preview callback, but no handler for it")
        }
    }

    companion object {

        private val TAG = PreviewCallback::class.java.simpleName
    }

}
