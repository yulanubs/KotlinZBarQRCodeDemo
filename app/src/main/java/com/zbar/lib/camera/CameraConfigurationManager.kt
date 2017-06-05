package com.zbar.lib.camera

import android.content.Context
import android.graphics.Point
import android.hardware.Camera
import android.os.Build
import android.util.Log
import android.view.Display
import android.view.WindowManager

import java.util.regex.Pattern

/**
 * 作者: 陈涛(1076559197@qq.com)

 * 时间: 2014年5月9日 下午12:22:12

 * 版本: V_1.0.0

 * 描述: 相机参数配置
 */
internal class CameraConfigurationManager(private val context: Context) {
    var screenResolution: Point? = null
        private set
    var cameraResolution: Point? = null
        private set
    var previewFormat: Int = 0
        private set
    var previewFormatString: String? = null
        private set

    fun initFromCameraParameters(camera: Camera) {
        val parameters = camera.parameters
        previewFormat = parameters.previewFormat
        previewFormatString = parameters.get("preview-format")
        val manager = context
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        screenResolution = Point(display.width, display.height)

        val screenResolutionForCamera = Point()
        screenResolutionForCamera.x = screenResolution!!.x
        screenResolutionForCamera.y = screenResolution!!.y

        if (screenResolution!!.x < screenResolution!!.y) {
            screenResolutionForCamera.x = screenResolution!!.y
            screenResolutionForCamera.y = screenResolution!!.x
        }
        cameraResolution = getCameraResolution(parameters, screenResolutionForCamera)
    }

    fun setDesiredCameraParameters(camera: Camera) {
        val parameters = camera.parameters
        parameters.setPreviewSize(cameraResolution!!.x, cameraResolution!!.y)
        setFlash(parameters)
        setZoom(parameters)

        camera.setDisplayOrientation(90)
        camera.parameters = parameters
    }

    private fun setFlash(parameters: Camera.Parameters) {
        if (Build.MODEL.contains("Behold II") && CameraManager.SDK_INT == 3) { // 3
            parameters.set("flash-value", 1)
        } else {
            parameters.set("flash-value", 2)
        }
        parameters.set("flash-mode", "off")
    }

    private fun setZoom(parameters: Camera.Parameters) {

        val zoomSupportedString = parameters.get("zoom-supported")
        if (zoomSupportedString != null && !java.lang.Boolean.parseBoolean(zoomSupportedString)) {
            return
        }

        var tenDesiredZoom = TEN_DESIRED_ZOOM

        val maxZoomString = parameters.get("max-zoom")
        if (maxZoomString != null) {
            try {
                val tenMaxZoom = (10.0 * java.lang.Double
                        .parseDouble(maxZoomString)).toInt()
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom
                }
            } catch (nfe: NumberFormatException) {
                Log.w(TAG, "Bad max-zoom: " + maxZoomString)
            }

        }

        val takingPictureZoomMaxString = parameters
                .get("taking-picture-zoom-max")
        if (takingPictureZoomMaxString != null) {
            try {
                val tenMaxZoom = Integer.parseInt(takingPictureZoomMaxString)
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom
                }
            } catch (nfe: NumberFormatException) {
                Log.w(TAG, "Bad taking-picture-zoom-max: " + takingPictureZoomMaxString)
            }

        }

        val motZoomValuesString = parameters.get("mot-zoom-values")
        if (motZoomValuesString != null) {
            tenDesiredZoom = findBestMotZoomValue(motZoomValuesString,
                    tenDesiredZoom)
        }

        val motZoomStepString = parameters.get("mot-zoom-step")
        if (motZoomStepString != null) {
            try {
                val motZoomStep = java.lang.Double.parseDouble(motZoomStepString
                        .trim { it <= ' ' })
                val tenZoomStep = (10.0 * motZoomStep).toInt()
                if (tenZoomStep > 1) {
                    tenDesiredZoom -= tenDesiredZoom % tenZoomStep
                }
            } catch (nfe: NumberFormatException) {
                // continue
            }

        }

        // Set zoom. This helps encourage the user to pull back.
        // Some devices like the Behold have a zoom parameter
        if (maxZoomString != null || motZoomValuesString != null) {
            parameters.set("zoom", (tenDesiredZoom / 10.0).toString())
        }

        // Most devices, like the Hero, appear to expose this zoom parameter.
        // It takes on values like "27" which appears to mean 2.7x zoom
        if (takingPictureZoomMaxString != null) {
            parameters.set("taking-picture-zoom", tenDesiredZoom)
        }
    }

    companion object {

        private val TAG = CameraConfigurationManager::class.java
                .simpleName

        private val TEN_DESIRED_ZOOM = 27
        private val COMMA_PATTERN = Pattern.compile(",")

        private fun getCameraResolution(parameters: Camera.Parameters,
                                        screenResolution: Point): Point {

            var previewSizeValueString: String? = parameters.get("preview-size-values")
            if (previewSizeValueString == null) {
                previewSizeValueString = parameters.get("preview-size-value")
            }

            var cameraResolution: Point? = null

            if (previewSizeValueString != null) {
                cameraResolution = findBestPreviewSizeValue(previewSizeValueString,
                        screenResolution)
            }

            if (cameraResolution == null) {
                cameraResolution = Point(screenResolution.x shr 3 shl 3,
                        screenResolution.y shr 3 shl 3)
            }

            return cameraResolution
        }

        private fun findBestPreviewSizeValue(
                previewSizeValueString: CharSequence, screenResolution: Point): Point? {
            var bestX = 0
            var bestY = 0
            var diff = Integer.MAX_VALUE
            for (previewSize in COMMA_PATTERN.split(previewSizeValueString)) {

               var previewSize = previewSize.trim { it <= ' ' }
                val dimPosition = previewSize.indexOf('x')
                if (dimPosition < 0) {
                    continue
                }

                val newX: Int
                val newY: Int
                try {
                    newX = Integer.parseInt(previewSize.substring(0, dimPosition))
                    newY = Integer.parseInt(previewSize.substring(dimPosition + 1))
                } catch (nfe: NumberFormatException) {
                    continue
                }

                val newDiff = Math.abs(newX - screenResolution.x) + Math.abs(newY - screenResolution.y)
                if (newDiff == 0) {
                    bestX = newX
                    bestY = newY
                    break
                } else if (newDiff < diff) {
                    bestX = newX
                    bestY = newY
                    diff = newDiff
                }

            }

            if (bestX > 0 && bestY > 0) {
                return Point(bestX, bestY)
            }
            return null
        }

        private fun findBestMotZoomValue(stringValues: CharSequence,
                                         tenDesiredZoom: Int): Int {
            var tenBestValue = 0
            for (stringValue in COMMA_PATTERN.split(stringValues)) {
                var stringValue = stringValue.trim { it <= ' ' }
                val value: Double
                try {
                    value = java.lang.Double.parseDouble(stringValue)
                } catch (nfe: NumberFormatException) {
                    return tenDesiredZoom
                }

                val tenValue = (10.0 * value).toInt()
                if (Math.abs(tenDesiredZoom - value) < Math.abs(tenDesiredZoom - tenBestValue)) {
                    tenBestValue = tenValue
                }
            }
            return tenBestValue
        }
    }

}
