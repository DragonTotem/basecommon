package com.zbt.common.ktx

import android.content.Context
import android.widget.Toast
import androidx.annotation.IntRange
import com.zbt.common.view.ViewUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.ByteBuffer

/**
 * Description: 常用简单kotlin拓展工具
 * @Author: xuwd11
 * Date: 2021/2/19 16:50
 *
 */

/**
 * 用于buffer扩容，如果buffer剩余空间小于[capacity],则增加相应容量
 *  @param capacity 扩充容量
 *  @return 返回新的byteBuffer,如果没扩充则返回旧的
 */
fun ByteBuffer.checkCapacity(capacity: Int): ByteBuffer {
    if (remaining() < capacity) {
        val buffer = if (isDirect) {
            ByteBuffer.allocateDirect(this.capacity() + capacity)
        } else {
            ByteBuffer.allocate(this.capacity() + capacity)
        }
        flip()
        buffer.put(this)
        return buffer
    }
    return this
}

/**
 * 异常堆栈转换为字符串
 */
fun Throwable.logString(): String {
    StringWriter().use {
        printStackTrace(PrintWriter(it))
        return it.toString()
    }
}

/**
 * 不带主线程的协程空间
 */
fun noMainScope(): CoroutineScope = CoroutineScope(SupervisorJob())


/**
 * 弹出toast 默认短时
 */
fun Context.showToast(text: String, @IntRange(from = 0, to = 1) time: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, time).show()
}

/**
 * dp转px
 */
fun Context.dp2px(dp: Int): Int {
    return ViewUtils.dp2px(this, dp)
}

/**
 * px转dp
 */
fun Context.px2dp(px: Int): Int {
    return ViewUtils.px2dp(this, px)
}

