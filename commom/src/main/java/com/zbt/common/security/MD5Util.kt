package com.zbt.common.security

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

/**
 * Description: md5计算工具
 * @Author: xuwd11
 * Date: 2021/2/19 14:33
 *
 */
object MD5Util {


    /**
     * 计算字符串MD5
     */
    fun getMD5String(source: String): String {
        val md5 = MessageDigest.getInstance("MD5")
        val builder = StringBuilder()
        var temp = ""
        md5.digest(source.toByteArray()).forEach {
            temp = Integer.toHexString((0xff.and(it.toInt())))
            if (temp.length == 1) {
                builder.append(0)
            }
            builder.append(temp)

        }

        return builder.toString()
    }


    /**
     * 计算文件的MD5值
     */
    fun getFileMD5(file: File): String {
        if (!file.isFile || !file.exists()) {
            return ""
        }

        val md5 = MessageDigest.getInstance("MD5")
        val builder = StringBuilder()
        var temp = ""
        FileInputStream(file).use {
            val buffer = ByteArray(8192)
            var len = it.read(buffer)
            while (len != -1) {
                md5.update(buffer, 0, len)
                len = it.read(buffer)
            }
        }
        md5.digest().forEach {
            temp = Integer.toHexString((0xff.and(it.toInt())))
            if (temp.length == 1) {
                builder.append(0)
            }
            builder.append(temp)

        }

        return builder.toString()
    }

}