package com.zbar.lib.bitmap

import kotlin.experimental.and

class InvertedLuminanceSource(private val delegate: LuminanceSource) : LuminanceSource(delegate.width, delegate.height) {

    override fun getRow(y: Int, row: ByteArray): ByteArray {
        var row = row
        row = delegate.getRow(y, row)
        val width = width
        for (i in 0..width - 1) {
            row[i] = (255 - (row[i] and 0xFF.toByte())).toByte()
        }
        return row
    }

    override val matrix: ByteArray
        get() {
            val matrix = delegate.matrix
            val length = width * height
            val invertedMatrix = ByteArray(length)
            for (i in 0..length - 1) {
                invertedMatrix[i] = (255 - (matrix[i] and 0xFF.toByte())).toByte()
            }
            return invertedMatrix
        }


    override fun crop(left: Int, top: Int, width: Int, height: Int): LuminanceSource {
        return InvertedLuminanceSource(delegate.crop(left, top, width, height))
    }


    /**
     * @return original delegate [LuminanceSource] since invert undoes
     * *         itself
     */
    override fun invert(): LuminanceSource {
        return delegate
    }

    override fun rotateCounterClockwise(): LuminanceSource {
        return InvertedLuminanceSource(delegate.rotateCounterClockwise())
    }

    override fun rotateCounterClockwise45(): LuminanceSource {
        return InvertedLuminanceSource(delegate.rotateCounterClockwise45())
    }

}
