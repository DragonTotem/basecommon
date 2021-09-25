package com.zbt.common.ktx

import android.os.Build
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import androidx.annotation.ColorInt
import androidx.core.text.inSpans

/**
 * Description: spannable补充工具
 * @Author: xuwd
 * Date: 2020/12/29 13:32
 *
 */
abstract class SimpleClickSpan : ClickableSpan() {
    override fun updateDrawState(ds: TextPaint) {
        //去除颜色和下划线
    }
}

class ColorUnderLine constructor(@ColorInt underLineColor: Int) : UnderlineSpan() {
    private val mColor = underLineColor
    override fun updateDrawState(ds: TextPaint) {
        if (Build.VERSION.SDK_INT > 28) {
            ds.underlineThickness = ds.getUnderlineThickness()
            ds.underlineColor = mColor
        } else {
            super.updateDrawState(ds)
        }
    }
}

inline fun SpannableStringBuilder.colorUnderLine(
        @ColorInt color: Int,
        builderAction: SpannableStringBuilder.() -> Unit,
) = inSpans(ColorUnderLine(color), builderAction = builderAction)