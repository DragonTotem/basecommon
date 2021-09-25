package com.zbt.common.entity

import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.annotation.IdRes
import java.io.Serializable

/**
 * Description: 标准图片实例 包含基础原图缩略图路径
 * 对应资源id 可执行事件等
 * @Author: xuwd
 * Date: 2020/10/15 10:32
 */
open class ImageEntity constructor(url: String) : View.OnClickListener, Serializable, Parcelable {

    //原图路径
    var url: String? = url

    //缩略图路径
    var thumbnailUrl: String? = null

    //可操作性事件
    var action: String? = null

    /**标题 可选*/
    var title: String? = null

    /**描述 可选*/
    var description: String? = null

    //资源图
    @IdRes
    var res = 0

    constructor(parcel: Parcel) : this("") {
        url = parcel.readString()
        thumbnailUrl = parcel.readString()
        action = parcel.readString()
        title = parcel.readString()
        description = parcel.readString()
        res = parcel.readInt()
    }

    //可自定义事件
    override fun onClick(v: View) {
        //do some
    }


    constructor(url: String, action: String?) : this(url) {
        this.action = action
    }

    constructor(url: String, action: String?, thumbnailUrl: String?) : this(url, action) {
        this.thumbnailUrl = thumbnailUrl
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeString(thumbnailUrl)
        parcel.writeString(action)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeInt(res)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImageEntity> {
        override fun createFromParcel(parcel: Parcel): ImageEntity {
            return ImageEntity(parcel)
        }

        override fun newArray(size: Int): Array<ImageEntity?> {
            return arrayOfNulls(size)
        }
    }
}