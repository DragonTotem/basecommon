package com.zbt.common.network

import com.google.gson.annotations.SerializedName

/**
 *
 * 请求结果类 抽象网络基本数据
 * @author xuwd
 */

class BaseResponseEntity<T> {
    /**
     * 接口服务是否成功
     */
    @JvmField
    @SerializedName("success")
    var success = false

    /**
     * 接口服务实例数据
     */
    @JvmField
    @SerializedName("data")
    var data: T? = null

    /**
     * 接口返回的信息 通常是错误信息
     */
    @SerializedName("message")
    @JvmField
    var msg = "unknown"

    /**
     * 接口返回错误码 默认0
     */
    @JvmField
    @SerializedName("code")
    var code = 0

    /**
     * 当前服务器时间 毫秒
     */
    @JvmField
    @SerializedName("time")
    var time: Long = 0

    /**
     * 可选参数 用于分页数据 当前页
     */
    @JvmField
    @SerializedName("currentPage")
    var currentPage = 1

    /**
     * 可选参数 用于分页数据 总页数
     */
    @JvmField
    @SerializedName("maxPage")
    var maxPage = 1

    override fun toString(): String {
        return "BaseResponseEntity{ success=$success, " +
                "currentPage=$currentPage, " +
                "maxPage=$maxPage, " +
                "data=$data, " +
                "message=$msg, " +
                "code=$code, " +
                "time=$time }"
    }

    /**
     * 数据格式 json
     * {
        "success": true, //布尔 表示接口业务是否成功
        "data": "",//{},[{},{}],1,false 业务数据
    "code": 0,//错误码
    "message": "aaa",//补充信息
    "time": 100000000,//服务器时间
        "currentPage": 1,//当前页 可选
        "maxPage": 1//最大页 可选
        }
     */

}