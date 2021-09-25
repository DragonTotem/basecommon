package com.zbt.common.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.Dimension
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.zbt.common.R
import com.zbt.common.app.CommonBaseActivity
import com.zbt.common.databinding.CommonTopBarViewBinding


/**
 * Description:全局头部栏定义
 * @Author: xuwd
 * Date: 2020/9/29
 */
class TopBarView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
ConstraintLayout(context, attrs, defStyleAttr)  {
    companion object {
        @Dimension(unit = Dimension.DP)
        private val BarHeight = 48

        @Dimension(unit = Dimension.SP)
        private val TitleSize = 28

        @Dimension(unit = Dimension.SP)
        private val NameSize = 22

        @Dimension(unit = Dimension.DP)
        private val Padding = 6
        private const val ModeCenter = 1
        private const val ModeLeft = 0

    }

    private var title: String? = null
    private var leftText: String? = null
    private var rightText: String? = null
    private var leftIconRes = 0
    private var rightIconRes = 0
    private var titleColor = 0
    private var leftTextColor = 0
    private var rightTextColor = 0
    private var titleMode = 0
    private val defaultColor = Color.BLACK

    //默认返回箭头
    private val defaultBackRes = R.drawable.common_ic_arrow_l
    private var binding: CommonTopBarViewBinding? = null

    private var lastClickTime: Long = 0
    private val oneClickListener = OnClickListener { v ->
        val current = System.currentTimeMillis()
        if (current - lastClickTime > 1000) {
            lastClickTime = current
            rightClickListener?.onClick(v)
        }
    }

    init {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.TopBarView, defStyle, 0)
        title = a.getString(
                R.styleable.TopBarView_title)
        titleColor = a.getColor(
                R.styleable.TopBarView_titleColor,
                defaultColor)
        leftText = a.getString(
                R.styleable.TopBarView_leftText)
        leftTextColor = a.getColor(
                R.styleable.TopBarView_leftTextColor,
                defaultColor)
        rightText = a.getString(
                R.styleable.TopBarView_rightText)
        rightTextColor = a.getColor(
                R.styleable.TopBarView_rightTextColor,
                defaultColor)
        titleMode = a.getInt(R.styleable.TopBarView_titleGravity, ModeLeft)

        leftIconRes = a.getResourceId(R.styleable.TopBarView_leftIcon, defaultBackRes)
        rightIconRes = a.getResourceId(R.styleable.TopBarView_rightIcon, 0)

        val hiddenBack=a.getBoolean(R.styleable.TopBarView_hiddenBack,false)
        val hiddenLine=a.getBoolean(R.styleable.TopBarView_hiddenLine,false)
        a.recycle()




        binding = CommonTopBarViewBinding.inflate(LayoutInflater.from(context), this)

        binding?.apply {
            title?.let {
                topTitle.text=title
                topTitle.setTextColor(titleColor)
                topTitle.visibility= View.VISIBLE

            }
            leftText?.let {
                topLeftText.text=leftText
                topLeftText.setTextColor(leftTextColor)
                topLeftText.visibility= View.VISIBLE

            }
            rightText?.let {
                topRightText.text=rightText
                topRightText.setTextColor(rightTextColor)
                topRightText.visibility= View.VISIBLE
            }
            if(leftIconRes!=0){
                topLeftIcon.setImageResource(leftIconRes)
                topLeftIcon.visibility= View.VISIBLE
            }
            if(rightIconRes!=0){
                topRightIcon.setImageResource(rightIconRes)
                topRightIcon.visibility= View.VISIBLE
            }
            if(titleMode== ModeCenter){
                with(ConstraintSet()){
                    clone(this@TopBarView)
                    connect(topTitle.id,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START,0)
                    connect(topTitle.id,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END,0)
                    applyTo(this@TopBarView)
                }
            }
            if(hiddenBack){
                topLeftIcon.visibility= View.GONE
            }
            if (hiddenLine) {
                topBarLine.visibility = View.GONE
            }

            setLeftClick {
                when (context) {
                    is CommonBaseActivity -> (context as CommonBaseActivity).onBackPressed()
                }
            }
            binding?.topRightIcon?.setOnClickListener(oneClickListener)
            binding?.topRightText?.setOnClickListener(oneClickListener)
        }
    }

    enum class Item{
        LeftTv,LeftIv,Title,RightTv,RightIv,Line
    }

    /**
     * 设置字体内容
     */
    fun setTextContent(item: Item, content: String?) {
        when (item) {
            Item.LeftTv -> binding?.topLeftText?.text = content ?: ""
            Item.Title -> binding?.topTitle?.text = content ?: ""
            Item.RightTv -> binding?.topRightText?.text = content ?: ""
            else         -> return
        }
    }

    /**
     * 设置字体颜色
     */
    fun setTextColor(item: Item, color: Int) {
        when (item) {
            Item.LeftTv -> binding?.topLeftText?.setTextColor(color)
            Item.Title -> binding?.topTitle?.setTextColor(color)
            Item.RightTv -> binding?.topRightText?.setTextColor(color)

        }
    }

    /**
     * 设置字体大小
     */
    fun setTextSize(item: Item, size: Float) {
        when (item) {
            Item.LeftTv -> binding?.topLeftText?.textSize = size
            Item.Title -> binding?.topTitle?.textSize = size
            Item.RightTv -> binding?.topRightText?.textSize = size
            else         -> return
        }
    }

    /**
     * 指定内容隐藏显示
     */
    fun setItemVisibility(item: Item, visible: Int) {
        when (item) {
            Item.LeftTv -> binding?.topLeftText?.visibility = visible
            Item.LeftIv -> binding?.topLeftIcon?.visibility = visible
            Item.Title -> binding?.topTitle?.visibility = visible
            Item.RightTv -> binding?.topRightText?.visibility = visible
            Item.RightIv -> binding?.topRightIcon?.visibility = visible
            Item.Line -> binding?.topBarLine?.visibility = visible
        }
    }

    /**
     * 设置图像资源包括底栏线
     */
    fun setImageRes(item: Item, res: Int) {
        when (item) {
            Item.LeftIv -> binding?.topLeftIcon?.setImageResource(res)
            Item.RightIv -> binding?.topRightIcon?.setImageResource(res)
            Item.Line -> binding?.topBarLine?.setBackgroundResource(res)
            else -> return
        }
    }

    /**
     * 设置返回键着色
     */
    fun setBackArrowTint(colorStateList: ColorStateList) {
        binding?.topLeftIcon?.imageTintList = colorStateList
    }

    /**
     * 设置返回键着色
     */
    fun setBackArrowTintByRes(@ColorRes colorId: Int) {
        binding?.topLeftIcon?.imageTintList = resources.getColorStateList(colorId, null)
    }

    /**
     * 设置返回键着色
     */
    fun setBackArrowColor(@ColorInt color: Int) {
        binding?.topLeftIcon?.imageTintList = ColorStateList.valueOf(color)
    }


    fun setLeftClick(clickListener: OnClickListener) {
        binding?.topLeftIcon?.setOnClickListener(clickListener)
        binding?.topLeftText?.setOnClickListener(clickListener)
    }

    fun setRightClick(clickListener: OnClickListener) {
        rightClickListener = clickListener

    }

    private var rightClickListener: OnClickListener? = null


}