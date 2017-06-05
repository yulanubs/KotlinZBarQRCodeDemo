package com.zbar.lib.decode

import android.app.Activity
import android.content.DialogInterface

/**
 * 作者: 陈涛(1076559197@qq.com)

 * 时间: 2014年5月9日 下午12:24:51

 * 版本: V_1.0.0

 */
class FinishListener(private val activityToFinish: Activity) : DialogInterface.OnClickListener, DialogInterface.OnCancelListener, Runnable {

    override fun onCancel(dialogInterface: DialogInterface) {
        run()
    }

    override fun onClick(dialogInterface: DialogInterface, i: Int) {
        run()
    }

    override fun run() {
        activityToFinish.finish()
    }

}
