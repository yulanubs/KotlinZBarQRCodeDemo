package com.zbar.lib.camera

import android.content.Context
import android.graphics.Point
import android.hardware.Camera
import android.hardware.Camera.Parameters
import android.os.Handler
import android.view.SurfaceHolder

import java.io.IOException

/**
 * 作者: 陈涛(1076559197@qq.com)

 * 时间: 2014年5月9日 下午12:22:25

 * 版本: V_1.0.0

 * 描述: 相机管理
 */
class CameraManager private constructor(context: Context) {

    private val configManager: CameraConfigurationManager
    private var camera: Camera? = null
    private var initialized: Boolean = false
    private var previewing: Boolean = false
    private val useOneShotPreviewCallback: Boolean
    private val previewCallback: PreviewCallback
    private val autoFocusCallback: AutoFocusCallback
    private var parameter: Parameters? = null

    init {
        this.configManager = CameraConfigurationManager(context)
        FlashlightManager.Companion.init();
        useOneShotPreviewCallback = SDK_INT > 3
        previewCallback = PreviewCallback(configManager, useOneShotPreviewCallback)
        autoFocusCallback = AutoFocusCallback()
    }

    @Throws(IOException::class)
    fun openDriver(holder: SurfaceHolder) {
        if (camera == null) {
            camera = Camera.open()
            if (camera == null) {
                throw IOException()
            }
            camera!!.setPreviewDisplay(holder)

            if (!initialized) {
                initialized = true
                configManager.initFromCameraParameters(camera!!)
            }
            configManager.setDesiredCameraParameters(camera!!)
            FlashlightManager.Companion.get().enableFlashlight()
        }
    }

    val cameraResolution: Point
        get() = configManager.cameraResolution!!

    fun closeDriver() {
        if (camera != null) {
            FlashlightManager.Companion.get().disableFlashlight()
            camera!!.release()
            camera = null
        }
    }

    fun startPreview() {
        if (camera != null && !previewing) {
            camera!!.startPreview()
            previewing = true
        }
    }

    fun stopPreview() {
        if (camera != null && previewing) {
            if (!useOneShotPreviewCallback) {
                camera!!.setPreviewCallback(null)
            }
            camera!!.stopPreview()
            previewCallback.setHandler(null, 0)
            autoFocusCallback.setHandler(null, 0)
            previewing = false
        }
    }

    fun requestPreviewFrame(handler: Handler, message: Int) {
        if (camera != null && previewing) {
            previewCallback.setHandler(handler, message)
            if (useOneShotPreviewCallback) {
                camera!!.setOneShotPreviewCallback(previewCallback)
            } else {
                camera!!.setPreviewCallback(previewCallback)
            }
        }
    }

    fun requestAutoFocus(handler: Handler, message: Int) {
        if (camera != null && previewing) {
            autoFocusCallback.setHandler(handler, message)
            camera!!.autoFocus(autoFocusCallback)
        }
    }

    fun openLight() {
        if (camera != null) {
            parameter = camera!!.parameters
            parameter!!.flashMode = Parameters.FLASH_MODE_TORCH
            camera!!.parameters = parameter
        }
    }

    fun offLight() {
        if (camera != null) {
            parameter = camera!!.parameters
            parameter!!.flashMode = Parameters.FLASH_MODE_OFF
            camera!!.parameters = parameter
        }
    }

    companion object {
        private var cameraManager: CameraManager? = null

        internal val SDK_INT: Int

        init {
            var sdkInt: Int
            try {
                sdkInt = android.os.Build.VERSION.SDK_INT
            } catch (nfe: NumberFormatException) {
                sdkInt = 10000
            }

            SDK_INT = sdkInt
        }

        fun init(context: Context) {
            if (cameraManager == null) {
                cameraManager = CameraManager(context)
            }
        }

        fun get(): CameraManager? {
            return cameraManager
        }
    }
}
