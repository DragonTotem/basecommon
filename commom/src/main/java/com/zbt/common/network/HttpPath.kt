package com.zbt.common.network

import android.text.TextUtils

/**
 * @author xuwd11
 * 请求路径定义model
 */
open class HttpPath private constructor() {
    /**
     * 路由名称 用于功能路由，不同服务映射不同主机域名
     */
    protected var routeName: String? = null

    /**
     * 普通情况下不设置，特殊需要覆盖基础url时设置
     */
    var powerUrl:String=""

    /**
     * 如果设置了url,则会覆盖HttpRequest和retrofit中的url,普通情况不设置
     */
    fun powerUrl(url:String):HttpPath{
        powerUrl=url
        return this
    }

    /**
     * 仅仅为子路径 不包含域名
     */
    private val httpUrl = StringBuilder()

    //默认请求方式post
    var method = Method.POST
    /**
     * 获取请求路径 不包含域名host
     */
    /**
     * 设置当前实例全路径
     */
    var path: String
        get() = httpUrl.toString().replace("//".toRegex(), "/")
        set(path) {
            require(!(TextUtils.isEmpty(path) || path.contains(" "))) { "path is null or have a blank" }
            httpUrl.delete(0, httpUrl.length).append(path)
            if (path[0] == '/') {
                httpUrl.delete(0, 1)
            }
        }

    /**
     * 添加子路径 可带'/' 也可不带
     * 例如 "sub/getList" 或"/sub/getList"
     */
    private fun appendPath(path: String) {
        require(!(TextUtils.isEmpty(path) || path.contains(" "))) { "path is null or have a blank" }
        httpUrl.append("/").append(path)
    }


    /**
     * 指定父结构路由名称
     * @param routeName 路由
     */
    fun setRouteName(routeName: String?): HttpPath {
        this.routeName = routeName
        return this
    }

    /**
     * 生成当前路径下的子路径结构
     * @param subPath 子路径
     */
    fun produce(subPath: String): HttpPath {
        val temp = HttpPath()
        temp.httpUrl.append(httpUrl)
        temp.method = method
        temp.appendPath(subPath)
        return temp
    }

    fun method(method: Method): HttpPath {
        this.method = method
        return this
    }

    enum class Method {
        GET, POST
    }

    /**
     * 请求路径定义构造器 初始化必须传入正常路径 方法可选，默认post
     */
    class Builder {
        var httpPath = HttpPath()
        fun path(path: String): Builder {
            httpPath.path = path
            return this
        }

        fun method(method: Method): Builder {
            httpPath.method(method)
            return this
        }

        fun build(): HttpPath {
            return httpPath
        }
    }
}