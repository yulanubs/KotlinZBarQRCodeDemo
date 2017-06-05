package com.zbar.lib

/**
 * 作者: 陈涛(1076559197@qq.com)

 * 时间: 2014年5月9日 下午12:25:46

 * 版本: V_1.0.0

 * 描述: zbar调用类
 */
class ZbarManager {

    external fun decode(data: ByteArray, width: Int, height: Int, isCrop: Boolean, x: Int, y: Int, cwidth: Int, cheight: Int): String

    companion object {

        init {
            System.loadLibrary("zbar")
        }
    }
}
