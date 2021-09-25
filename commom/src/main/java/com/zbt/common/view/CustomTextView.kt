package com.zbt.common.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View.OnClickListener
import androidx.annotation.FloatRange
import androidx.appcompat.widget.AppCompatTextView
import com.zbt.common.R


/**
 * Description: 自定常用按钮样式
 * @Author: xuwd
 * Date: 2020/10/21 19:15
 *
 */
open class CustomTextView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        AppCompatTextView(context, attrs, defStyleAttr) {
    private var isCustomSize = false
    private var attSize = 0

    companion object {
        val Full = 1
        val Middle = 2
        val Small = 3
        val None = 0
        val Round = 1
        const val Circle = 2
    }

    private val path = Path()
    private val progressPath = Path()
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }
    var border = 0f
    var strokeType = 0
    var round = 0
    var progressMode = false

    init {
        init(attrs, defStyleAttr)
    }

    var progress = 0f
        set(@FloatRange(from = 0.0, to = 1.0) value) {
            field = value
            invalidate()
        }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        context.obtainStyledAttributes(
                attrs, R.styleable.CustomTextView, defStyle, 0).apply {

            val sizeType = getInt(R.styleable.CustomTextView_SizeType, 0)
            isCustomSize = sizeType > 0
            attSize = when (sizeType) {
                Full -> {
                    resources.getDimensionPixelSize(R.dimen.CustomTextFull)
                }
                Middle -> {
                    resources.getDimensionPixelSize(R.dimen.CustomTextMiddle)
                }
                Small -> {
                    resources.getDimensionPixelSize(R.dimen.CustomTextSmall)
                }
                else->{0}
            }
            strokeType = getInt(R.styleable.CustomTextView_StrokeType, 0)
            progressMode = getBoolean(R.styleable.CustomTextView_ProgressMode, false)
            border = getDimension(R.styleable.CustomTextView_Border, 0f)
            round = getDimensionPixelSize(R.styleable.CustomTextView_Round, 0)
            paint.strokeWidth = border * 2
            paint.color=getColor(R.styleable.CustomTextView_BorderColor,0)
            recycle()
        }
    }



    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if(isCustomSize){
            setMeasuredDimension(attSize,ViewUtils.dp2px(context,36))
        }else{
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        path.apply {
            reset()
            addRoundRect(RectF(0f,0f,w.toFloat(),h.toFloat()),round.toFloat(),round.toFloat(),Path.Direction.CW)
            when(strokeType){
                Circle->{
                    reset()
                    val r= w.coerceAtMost(h)
                    addRoundRect(RectF(0f,0f,w.toFloat(),h.toFloat()),r/2f,r/2f,Path.Direction.CW)
                }
            }
        }
    }


    override fun draw(canvas: Canvas) {
        if (progressMode) {
            progressPath.apply {
                reset()
                val wp = width * progress
                when (strokeType) {
                    Circle -> {
                        val r = width.coerceAtMost(height)
                        if (wp <= r) {
                            addCircle(wp - r / 2f, height / 2f, r / 2f, Path.Direction.CW)
                        } else {
                            addRoundRect(RectF(0f, 0f, wp, height.toFloat()), r / 2f, r / 2f, Path.Direction.CW)
                        }
                    }
                    else -> {
                        addRoundRect(RectF(0f, 0f, wp, height.toFloat()), round.toFloat(), round.toFloat(), Path.Direction.CW)
                    }
                }
                canvas.clipPath(progressPath)

            }
        }
        if (!path.isEmpty) {
            canvas.clipPath(path)
        }
        super.draw(canvas)
        if (border > 0) canvas.drawPath(path, paint)
    }


    override fun setOnClickListener(l: OnClickListener?) {
        singleClickListener=l
        super.setOnClickListener(oneClickListener)
    }

    private var singleClickListener: OnClickListener? = null
    private var lastClickTime: Long = 0
    private val oneClickListener = OnClickListener { v ->
        val current = System.currentTimeMillis()
        if (current - lastClickTime > 1000) {
            lastClickTime = current
            singleClickListener?.onClick(v)
        }
    }


}