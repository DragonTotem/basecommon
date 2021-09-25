package com.zbt.common.log

/**
 * Description: 日志输出抽象接口
 * @Author: xuwd11
 * Date: 2021/1/19 8:50
 *
 */
interface ILogInterface {
    fun i(msg: String?)
    fun d(msg: String?)
    fun w(msg: String?)
    fun e(msg: String?)
}