package com.zbt.common.image


import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.zbt.common.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener


object ImageLoader {

    val colors = arrayOf("#CB997E", "#DDBEA9", "#FEE8D6", "#B7B7A4", "#A5A58D", "#6B705C", "#FEC5BB", "#FCD5CE", "#FAE1DD", "#F8EDEB", "#E8E8E4", "#D8E2DC", "#EDE4DB", "#FFE5D9", "#FFD7BA", "#FFC89A", "#F1FAEE", "#A7DADC", "#447B9D", "#249EBC", "#E5E5E5", "#6D6875", "#B4838D", "#E5989B", "#FFC8DD", "#CDB4DB", "#BDE0FE", "#A2D2FF", "#026D77", "#84C5BE", "#FEFAE0", "#DDA15E", "#BB6D26", "#8D9AAF", "#CBBFD4", "#EED3D7", "#FFEAFA", "#DEE2FF", "#F07167", "#DBD2BC", "#EDDDD2", "#FFF1E6", "#F0EFEB", "#DDBEA9", "#85A59D", "#F18582", "#4A4E69", "#9B8B98", "#C8ADA7", "#F2E9E4", "#A9D6E5", "#88C1D9", "#61A4C2", "#478EAF", "#2D7CA0", "#015086", "#6E597A", "#364F70", "#FFDB5E", "#E8F6DB", "#B5C99B", "#96A97B", "#70A9A1", "#86986A", "#708355", "#19C3B2", "#FF85C8", "#FFA3A5", "#95E072", "#B892FF", "#E3CFEA", "#34312C", "#304450", "#596F7C", "#B8DBD9", "#F1FFC4", "#FFCB76", "#D9B895", "#DFD6D1", "#A99A85", "#DAC5B2", "#7E7F83", "#5D2D46", "#AE6A6B", "#B58DB6", "#CFADA7", "#B8DBD9", "#6798C0", "#A6A6A6", "#1B99E0", "#7ED8BE", "#FDEFEF", "#EED2FC", "#BADFF8", "#BEF4EC", "#D0FFD2", "#BBDEF0", "#9EC1A3", "#CEE0C2", "#ACB0BD")

    /**
     * 图片加载 模式centerCrop
     */
    @JvmStatic
    fun loadImage(context: Context?, url: String?, view: ImageView?, placeId: Int = 0, errorId: Int = 0) {
        context ?: return
        url ?: return
        view ?: return
        if (placeId == 0) {
            val drawable = ColorDrawable(Color.parseColor(colors.random()))
            Glide.with(context).load(url)
                    .placeholder(drawable)
                    .error(drawable)
                    .centerCrop()
                    .into(view)
        } else {
            Glide.with(context).load(url)
                    .placeholder(placeId)
                    .error(errorId)
                    .centerCrop()
                    .into(view)
        }


    }

    /**
     * 重载跟随fragmen周期 模式centerCrop
     */
    @JvmStatic
    fun loadImage(fragment: Fragment?, url: String?, view: ImageView?, placeId: Int = 0, errorId: Int = 0) {
        fragment ?: return
        url ?: return
        view ?: return
        if (placeId == 0) {
            val drawable = ColorDrawable(Color.parseColor(colors.random()))
            Glide.with(fragment).load(url)
                    .placeholder(drawable)
                    .error(drawable)
                    .centerCrop()
                    .into(view)
        } else {
            Glide.with(fragment).load(url)
                    .placeholder(placeId)
                    .error(errorId)
                    .centerCrop()
                    .into(view)
        }

    }

    /**
     * 图片加载 不处理拉伸模式，默认fitCenter 交给ImageView处理
     */
    @JvmStatic
    fun loadImageNoCenter(context: Context?, url: String?, view: ImageView?, placeId: Int = 0, errorId: Int = 0, listener: RequestListener<Drawable>? = null) {
        context ?: return
        url ?: return
        view ?: return
        if (placeId == 0) {
            val drawable = ColorDrawable(Color.parseColor(colors.random()))
            Glide.with(context).load(url)
                    .placeholder(drawable)
                    .error(drawable)
                    .listener(listener)
                    .into(view)
        } else {
            Glide.with(context).load(url)
                    .placeholder(placeId)
                    .error(errorId)
                    .listener(listener)
                    .into(view)
        }


    }
}


