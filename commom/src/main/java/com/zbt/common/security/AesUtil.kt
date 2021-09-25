package com.zbt.common.security

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Description: Aes加密工具
 * @Author: xuwd11
 * Date: 2021/2/19 15:02
 *
 */
object AesUtil {

    private val KEY_ALGORITHM = "AES"
    val KEY_ALGORITHM_PADDING = "AES/CBC/PKCS5Padding"


    /**
     * aes加密
     * @param data 待加密的数据
     * @param encryptKey 加密的秘钥key
     * @param iv 加密的平移向量
     */
    fun encrypt(data: ByteArray, encryptKey: ByteArray, iv: ByteArray): ByteArray {
        val zeroIv = IvParameterSpec(iv)
        val key = SecretKeySpec(encryptKey, KEY_ALGORITHM)
        try {
            val cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING)
            cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv)
            return cipher.doFinal(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ByteArray(0)
    }

    /**
     * aes解密
     * @param data 待解密的数据
     * @param decryptKey 加密的秘钥key
     * @param iv 加密的平移向量
     */
    fun decrypt(data: ByteArray, decryptKey: ByteArray, iv: ByteArray): ByteArray {
        val zeroIv = IvParameterSpec(iv)
        val key = SecretKeySpec(decryptKey, KEY_ALGORITHM)
        try {
            val cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING)
            cipher.init(Cipher.DECRYPT_MODE, key, zeroIv)
            return cipher.doFinal(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ByteArray(0)
    }
}

data class AesEntity(val encryptKey: ByteArray, val iv: ByteArray) {
    fun encrypt(data: ByteArray): ByteArray {
        return AesUtil.encrypt(data, encryptKey, iv)
    }

    fun decrypt(data: ByteArray): ByteArray {
        return AesUtil.decrypt(data, encryptKey, iv)
    }
}