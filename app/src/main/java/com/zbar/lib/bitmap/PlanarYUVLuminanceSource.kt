package com.zbar.lib.bitmap

import kotlin.experimental.and

class PlanarYUVLuminanceSource(private val yuvData: ByteArray, private val dataWidth: Int, private val dataHeight: Int, private val left: Int, private val top: Int, width: Int, height: Int, reverseHorizontal: Boolean) : LuminanceSource(width, height) {

    init {

        if (left + width > dataWidth || top + height > dataHeight) {
            throw IllegalArgumentException("Crop rectangle does not fit within image data.")
        }
        if (reverseHorizontal) {
            reverseHorizontal(width, height)
        }
    }

    override fun getRow(y: Int, row: ByteArray): ByteArray {
        var row = row
        if (y < 0 || y >= height) {
            throw IllegalArgumentException("Requested row is outside the image: " + y)
        }
        val width = width
        if (row == null || row.size < width) {
            row = ByteArray(width)
        }
        val offset = (y + top) * dataWidth + left
        System.arraycopy(yuvData, offset, row, 0, width)
        return row
    }

    override // If the caller asks for the entire underlying image, save the copy and
            // give them the
            // original data. The docs specifically warn that result.length must be
            // ignored.
            // If the width matches the full width of the underlying data, perform a
            // single copy.
            // Otherwise copy one cropped row at a time.
    val matrix: ByteArray
        get() {
            val width = width
            val height = height
            if (width == dataWidth && height == dataHeight) {
                return yuvData
            }

            val area = width * height
            val matrix = ByteArray(area)
            var inputOffset = top * dataWidth + left
            if (width == dataWidth) {
                System.arraycopy(yuvData, inputOffset, matrix, 0, area)
                return matrix
            }
            val yuv = yuvData
            for (y in 0..height - 1) {
                val outputOffset = y * width
                System.arraycopy(yuv, inputOffset, matrix, outputOffset, width)
                inputOffset += dataWidth
            }
            return matrix
        }


    override fun crop(left: Int, top: Int, width: Int, height: Int): LuminanceSource {
        return PlanarYUVLuminanceSource(yuvData, dataWidth, dataHeight, this.left + left, this.top + top, width, height, false)
    }

    fun renderThumbnail(): IntArray {
        val width = width / THUMBNAIL_SCALE_FACTOR
        val height = height / THUMBNAIL_SCALE_FACTOR
        val pixels = IntArray(width * height)
        val yuv = yuvData
        var inputOffset = top * dataWidth + left

        for (y in 0..height - 1) {
            val outputOffset = y * width
            for (x in 0..width - 1) {
                val grey = yuv[inputOffset + x * THUMBNAIL_SCALE_FACTOR] and 0xff.toByte()
                pixels[outputOffset + x] = 0xFF000000.toInt() or grey * 0x00010101
            }
            inputOffset += dataWidth * THUMBNAIL_SCALE_FACTOR
        }
        return pixels
    }

    /**
     * @return width of image from [.renderThumbnail]
     */
    val thumbnailWidth: Int
        get() = width / THUMBNAIL_SCALE_FACTOR

    /**
     * @return height of image from [.renderThumbnail]
     */
    val thumbnailHeight: Int
        get() = height / THUMBNAIL_SCALE_FACTOR

    private fun reverseHorizontal(width: Int, height: Int) {
        val yuvData = this.yuvData
        var y = 0
        var rowStart = top * dataWidth + left
        while (y < height) {
            val middle = rowStart + width / 2
            var x1 = rowStart
            var x2 = rowStart + width - 1
            while (x1 < middle) {
                val temp = yuvData[x1]
                yuvData[x1] = yuvData[x2]
                yuvData[x2] = temp
                x1++
                x2--
            }
            y++
            rowStart += dataWidth
        }
    }

    companion object {

        private val THUMBNAIL_SCALE_FACTOR = 2
    }

}
