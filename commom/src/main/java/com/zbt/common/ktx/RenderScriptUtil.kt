package com.zbt.common.ktx

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.annotation.FloatRange
import java.lang.Exception

/**
 * Description: 图片处理工具
 * @Author: xuwd11
 * Date: 21-3-10 下午1:22
 *
 */
object RenderScriptUtil {

    fun blurBitmap(
            context: Context, bitmap: Bitmap,
            @FloatRange(from = 0.0, to = 25.0, fromInclusive = false) radius: Float = 24f,
    ): Bitmap {
        try {
            val out = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            RenderScript.create(context).apply {
                val inBit = Allocation.createFromBitmap(this, bitmap)
                val outBit = Allocation.createFromBitmap(this, out)
                ScriptIntrinsicBlur.create(this, Element.U8_4(this)).apply {
                    setRadius(radius)
                    setInput(inBit)
                    forEach(outBit)
                }
                outBit.copyTo(out)
                destroy()
            }
            return out
        } catch (e: Exception) {
            e.printStackTrace()
            return bitmap
        }

    }
}