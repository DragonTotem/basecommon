package com.zbt.common.security

import com.google.common.truth.Truth
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Description:
 *
 * @Author: xuwd11
 * Date: 2021/2/19 17:29
 */
class MD5UtilTest {
    private val text = "hello 小明"
    private val md5 = "a3c80a507c15d2ff0d70f68ec467fdf3"
    private var file = File("test")

    @Test
    fun getMD5String() {
        Truth.assertThat(MD5Util.getMD5String(text)).isEqualTo(md5)
    }

    @Before
    fun setUp() {
        file.delete()
        file.createNewFile()
        file.writeBytes(text.toByteArray())

    }

    @After
    fun tearDown() {
        file.delete()
    }

    @Test
    fun getFileMD5() {
        val temp = MD5Util.getFileMD5(file)
        println(temp)
        Truth.assertThat(temp).isEqualTo(md5)

    }
}