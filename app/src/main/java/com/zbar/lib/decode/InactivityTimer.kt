package com.zbar.lib.decode

import android.app.Activity

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

/**
 * 作者: 陈涛(1076559197@qq.com)

 * 时间: 2014年5月9日 下午12:25:12

 * 版本: V_1.0.0

 */
class InactivityTimer(private val activity: Activity) {

    private val inactivityTimer = Executors.newSingleThreadScheduledExecutor(DaemonThreadFactory())
    private var inactivityFuture: ScheduledFuture<*>? = null

    init {
        onActivity()
    }

    fun onActivity() {
        cancel()
        inactivityFuture = inactivityTimer.schedule(FinishListener(activity), INACTIVITY_DELAY_SECONDS.toLong(), TimeUnit.SECONDS)
    }

    private fun cancel() {
        if (inactivityFuture != null) {
            inactivityFuture!!.cancel(true)
            inactivityFuture = null
        }
    }

    fun shutdown() {
        cancel()
        inactivityTimer.shutdown()
    }

    private class DaemonThreadFactory : ThreadFactory {
        override fun newThread(runnable: Runnable): Thread {
            val thread = Thread(runnable)
            thread.isDaemon = true
            return thread
        }
    }

    companion object {

        private val INACTIVITY_DELAY_SECONDS = 5 * 60
    }

}
