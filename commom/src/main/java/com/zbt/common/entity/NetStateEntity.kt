package com.zbt.common.entity

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Description: 网络状态实例相关
 * @Author: xuwd
 * Date: 2020/12/7 14:25
 *
 */

object NetStateEntity {
    internal val mNetState = MutableLiveData(NetState(false, NetType.None))

    /**
     *   获取当前网络状态熟悉的livedata
     *  飞行模式和无网络时状态为 [NetType.None]。
     *
     *  当wifi与移动数据同时存在时状态为[NetType.Wifi],
     *  关闭wifi，状态由[NetType.Wifi]->[NetType.None]->[NetType.Other]
     *  关闭移动数据，状态不变。
     *
     *  当网络状态由移动数据变为wifi时，状态由[NetType.Other]->[NetType.Wifi]。
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun getNetState(): LiveData<NetState> {
        return mNetState
    }


    /**
     * 网络类型
     */
    enum class NetType {
        /**
         * 无网络
         */
        None,

        /**
         * 移动网络
         */
        Mobile,

        /**
         * wifi网络
         */
        Wifi,

        /**
         * 其他网络
         */
        Other
    }

    /**
     * 网络状态属性
     * @param isConnect 是否有网络连接
     * @param type 如果有网络连接，返回当前连接类型
     */
    data class NetState(val isConnect: Boolean, val type: NetType) {
        override fun toString(): String {
            return "isConnect = $isConnect,type = ${type.name}"
        }
    }
}
