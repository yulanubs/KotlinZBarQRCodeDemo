package com.zbar.lib.camera

import android.os.IBinder
import android.util.Log

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * 作者: 陈涛(1076559197@qq.com)

 * 时间: 2014年5月9日 下午12:22:42

 * 版本: V_1.0.0

 * 描述: 闪光灯管理
 */
internal class FlashlightManager {


//    private val iHardwareService: Any?
//    private val setFlashEnabledMethod: Method



    private val hardwareService: Any?
        get() {
            val serviceManagerClass = maybeForName("android.os.ServiceManager") ?: return null

            val getServiceMethod = maybeGetMethod(serviceManagerClass, "getService", String::class.java) ?: return null

            val hardwareService = invoke(getServiceMethod, null, "hardware") ?: return null

            val iHardwareServiceStubClass = maybeForName("android.os.IHardwareService\$Stub") ?: return null

            val asInterfaceMethod = maybeGetMethod(iHardwareServiceStubClass, "asInterface", IBinder::class.java) ?: return null

            return invoke(asInterfaceMethod, null, hardwareService)
        }

    private fun getSetFlashEnabledMethod(iHardwareService: Any?): Method? {
        if (iHardwareService == null) {
            return null
        }
        val proxyClass = iHardwareService.javaClass
        return maybeGetMethod(proxyClass, "setFlashlightEnabled", Boolean::class.java)
    }

    private fun maybeForName(name: String): Class<*>? {
        try {
            return Class.forName(name)
        } catch (cnfe: ClassNotFoundException) {
            // OK
            return null
        } catch (re: RuntimeException) {
            Log.w(TAG, "Unexpected error while finding class " + name, re)
            return null
        }

    }

    private fun maybeGetMethod(clazz: Class<*>, name: String, vararg argClasses: Class<*>): Method? {
        try {
            return clazz.getMethod(name, *argClasses)
        } catch (nsme: NoSuchMethodException) {
            // OK
            return null
        } catch (re: RuntimeException) {
            Log.w(TAG, "Unexpected error while finding method " + name, re)
            return null
        }

    }

    private operator fun invoke(method: Method, instance: Any?, vararg args: Any): Any? {
        try {
            return method.invoke(instance, *args)
        } catch (e: IllegalAccessException) {
            Log.w(TAG, "Unexpected error while invoking " + method, e)
            return null
        } catch (e: InvocationTargetException) {
            Log.w(TAG, "Unexpected error while invoking " + method, e.cause)
            return null
        } catch (re: RuntimeException) {
            Log.w(TAG, "Unexpected error while invoking " + method, re)
            return null
        }

    }

    fun enableFlashlight() {
        setFlashlight(true)
    }

    fun disableFlashlight() {
        setFlashlight(false)
    }

    private fun setFlashlight(active: Boolean) {
        if (iHardwareService != null) {
            invoke(setFlashEnabledMethod!!, iHardwareService, active)
        }
    }
    companion object {
        private val TAG = FlashlightManager::class.java.simpleName
        private var flashlightManager: FlashlightManager? = null
        private var iHardwareService: Any? = null
        private var setFlashEnabledMethod: Method?= null
        fun init() {
            if (flashlightManager == null) {
                flashlightManager = FlashlightManager()
            }
            iHardwareService = (flashlightManager as FlashlightManager).hardwareService
            setFlashEnabledMethod = (flashlightManager as FlashlightManager).getSetFlashEnabledMethod(iHardwareService)
            if (iHardwareService == null) {
                Log.v(TAG, "This device does supports control of a flashlight")
            } else {
                Log.v(TAG, "This device does not support control of a flashlight")
            }
        }
        fun get(): FlashlightManager {
            return flashlightManager!!
        }
    }
}
