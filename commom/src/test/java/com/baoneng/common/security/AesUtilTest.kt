package com.zbt.common.security

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.security.SecureRandom

/**
 * Description:AES加解密验证类
 *
 * @Author: xuwd11
 * Date: 2021/2/19 17:19
 */
class AesUtilTest {
    private var aes: AesEntity? = null

    @Before
    fun setUp() {
        val sec = SecureRandom()
        val key = ByteArray(16).apply { sec.nextBytes(this) }
        val iv = ByteArray(16).apply { sec.nextBytes(this) }
        aes = AesEntity(key, iv)
    }

    @Test
    fun encryptAndDecrypt() {
        val data = "hello 小明"
        var decode = ""
        aes?.apply {
            val p = encrypt(data.toByteArray())
            println(String(p))
            decode = String(decrypt(p))
        }
        println(decode)
        assertThat(decode).isEqualTo(data)
    }
}