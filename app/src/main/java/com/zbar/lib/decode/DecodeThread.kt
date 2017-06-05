package com.zbar.lib.decode

import android.os.Handler
import android.os.Looper
import com.zbar.lib.CaptureActivity

import java.util.concurrent.CountDownLatch

/**
 * 作者: 陈涛(1076559197@qq.com)

 * 时间: 2014年5月9日 下午12:24:34

 * 版本: V_1.0.0

 * 描述: 解码线程
 */
internal class DecodeThread(var activity: CaptureActivity) : Thread() {
    private var handler: Handler? = null
    private val handlerInitLatch: CountDownLatch

    init {
        handlerInitLatch = CountDownLatch(1)
    }

    fun getHandler(): Handler {
        try {
            handlerInitLatch.await()
        } catch (ie: InterruptedException) {
            // continue?
        }

        return handler!!
    }

    override fun run() {
        Looper.prepare()
        handler = DecodeHandler(activity)
        handlerInitLatch.countDown()
        Looper.loop()
    }

}
