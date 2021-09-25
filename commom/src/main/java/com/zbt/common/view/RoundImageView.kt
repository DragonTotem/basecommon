package com.zbt.common.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.zbt.common.R


/**
 * Description: 圆角或者圆形ImageView
 *
 * @Author: xuwd
 * Date: 2020/10/16 15:57
 */
class RoundImageView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet?=null, defStyleAttr: Int=0):
AppCompatImageView(context, attrs, defStyleAttr) {
    var circle=false //是否圆形
    var boder=0f
    var round=0f
    var roundTL=0f
    var roundTR=0f
    var roundBL=0f
    var roundBR = 0f
    val mPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }
    private val mPath = Path()
    init {
        init(attrs,defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int){
        context.obtainStyledAttributes(
                attrs, R.styleable.RoundImageView, defStyle, 0).apply {
            circle = getBoolean(R.styleable.RoundImageView_IsCircle, false)
            round = getDimension(R.styleable.RoundImageView_Round, 0f)
            roundTL = getDimension(R.styleable.RoundImageView_RoundTL, 0f)
            roundBL = getDimension(R.styleable.RoundImageView_RoundBL, 0f)
            roundTR = getDimension(R.styleable.RoundImageView_RoundTR, 0f)
            roundBR = getDimension(R.styleable.RoundImageView_RoundBR, 0f)
            boder = getDimension(R.styleable.RoundImageView_Border, 0f)
            mPaint.strokeWidth = boder * 2
            mPaint.color = getColor(R.styleable.RoundImageView_BorderColor, 0)


            recycle()
        }

    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mPath.apply {
            reset()
            when {
                circle                                    -> {
                    addCircle(width / 2f, height / 2f, width.coerceAtMost(height) / 2f, Path.Direction.CW)
                }
                round > 0                                 -> {
                    addRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), round, round, Path.Direction.CW)
                }
                roundBL + roundTL + roundTR + roundBR > 0 -> {
                    addRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()),
                            floatArrayOf(roundTL, roundTL,
                                    roundTR, roundTR,
                                    roundBL, roundBL,
                                    roundBR, roundBR), Path.Direction.CW)
                }
            }
        }
    }


    override fun draw(canvas: Canvas) {
        if (!mPath.isEmpty) {
            canvas.clipPath(mPath)
        }
        super.draw(canvas)
        if (boder > 0) canvas.drawPath(mPath, mPaint)
    }
}