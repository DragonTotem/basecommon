package com.zbt.common.view

import android.content.Context
import android.content.res.XmlResourceParser
import android.util.AttributeSet
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.zbt.common.R
import com.zbt.common.databinding.CommonBottomItemViewBinding


import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

/**
 * Description: 自定义底部导航栏
 *
 * @Author: xuwd
 * Date: 2020/10/9 14:16
 */
class BottomNavigationView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {
    val XML_ITEM = "item"
    var XML_MENU = "menu"

    var layoutParams: LinearLayout.LayoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
    val menuList = mutableListOf<MenuItem>()
    var onMenuItemSelectedListener: OnMenuItemSelectedListener? = null
    var onMenuItemReSelectedListener: OnMenuItemReSelectedListener? = null
    var currentSelect: MenuItem? = null;
    private var clickListener: OnClickListener = OnClickListener { v ->
        with(v.tag as MenuItem) {
            if (isSelected) {
                onMenuItemReSelectedListener?.onMenuItemReSelected(this)
            } else {
                currentSelect?.let {
                    it.isSelected = false
                    it.rootView?.isSelected = false
                }
                currentSelect = this
                this.isSelected = true
                this.rootView?.isSelected = true
                onMenuItemSelectedListener?.onMenuItemSelected(this)
            }
        }

    }

    private var factory: MenuItemFactory? = object : MenuItemFactory {
        override fun createMenuItem(parser: XmlPullParser): MenuItem {
            val item = DefaultMenuItem()
            with(parser) {

                for (i in 0 until attributeCount) {
                    when (getAttributeName(i)) {
                        "title" -> {
                            item.title = getAttributeValue(i).substring(1).toInt()
                        }
                        "icon" -> {
                            val a = getAttributeValue(i).substring(1).toInt()
                            item.iconRes = a
                        }

                        "id" -> {
                            val a = getAttributeValue(i).substring(1).toInt()
                            item.id = a
                        }
                    }
                }
            }

            return item
        }
    }

    init {
        initBottom(attrs)
    }


    private fun initBottom(attrs: AttributeSet?) {
        orientation = HORIZONTAL
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.BottomNavigationView, 0, 0)
        val res = a.getResourceId(R.styleable.BottomNavigationView_navigationMenu, 0)

        a.recycle()
        if (res != 0) {
            var parser: XmlResourceParser? = null
            try {
                parser = resources.getLayout(res)

                parseMenu(parser)
            } catch (e: XmlPullParserException) {
                throw InflateException("Error inflating menu XML", e)
            } catch (e: IOException) {
                throw InflateException("Error inflating menu XML", e)
            } finally {
                parser?.close()
            }
        }

        menuList.firstOrNull()?.apply {
            isSelected = true
            rootView?.isSelected = true
            currentSelect = this
        }


    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun parseMenu(parser: XmlPullParser) {

        var eventType = parser.eventType
        var tagName: String
        var lookingForEndOfUnknownTag = false
        var unknownTagName: String? = null


        do {
            if (eventType == XmlPullParser.START_TAG) {
                tagName = parser.name
                if (tagName == XML_MENU) {
                    eventType = parser.next()
                    break
                }
                throw RuntimeException("Expecting menu, got $tagName")
            }
            eventType = parser.next()

        } while (eventType != XmlPullParser.END_DOCUMENT)
        var reachedEndOfMenu = false
        while (!reachedEndOfMenu) {

            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (lookingForEndOfUnknownTag) {
                        break
                    }
                    tagName = parser.name
                    if (tagName == XML_ITEM) {

                        factory?.createMenuItem(parser)?.let {
                            it.getItemView(context)?.let { item ->
                                addView(item, layoutParams)
                                item.tag = it
                                item.setOnClickListener(clickListener)
                            }
                            menuList.add(it)
                        }


                    } else {
                        lookingForEndOfUnknownTag = true
                        unknownTagName = tagName
                    }
                }
                XmlPullParser.END_TAG -> {
                    tagName = parser.name
                    if (lookingForEndOfUnknownTag && tagName == unknownTagName) {
                        lookingForEndOfUnknownTag = false
                        unknownTagName = null
                    } else if (tagName == XML_MENU) {
                        reachedEndOfMenu = true

                    }
                }
                XmlPullParser.END_DOCUMENT -> throw RuntimeException("Unexpected end of document")
            }
            eventType = parser.next()
        }
    }


    /**
     * 抽象选项item工厂
     */
    interface MenuItemFactory {
        fun createMenuItem(parser: XmlPullParser): MenuItem
    }

    abstract class MenuItem {
        var id: Int = 0
        var title: Int = 0
        var iconRes: Int = 0
        var rootView: View? = null
        var bottomTextView: TextView? = null
        var bottomImageView: ImageView? = null
        var isSelected: Boolean = false
        abstract fun getItemView(context: Context): View?
    }

    class DefaultMenuItem : MenuItem() {

        override fun getItemView(context: Context): View? {
            val v = CommonBottomItemViewBinding.inflate(LayoutInflater.from(context))
            v.bottomItemText.setText(title)
            v.bottomItemIcon.setImageResource(iconRes)
            v.root.id = id
            bottomTextView = v.bottomItemText
            bottomImageView = v.bottomItemIcon
            rootView = v.root
            return v.root
        }

    }

    /**
     * 菜单被点击选中时回调
     */
    interface OnMenuItemSelectedListener {
        fun onMenuItemSelected(item: MenuItem)
    }

    /**
     * 菜单已选中再次被点击时回调
     */
    interface OnMenuItemReSelectedListener {
        fun onMenuItemReSelected(item: MenuItem)
    }
}