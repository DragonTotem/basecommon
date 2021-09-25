package com.zbt.common.security

import androidx.annotation.IntRange
import java.io.ByteArrayOutputStream
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

/**
 * Description: 客户端非对称加密工具，只提供使用服务器公钥加密功能
 * @Author: xuwd11
 * Date: 2021/2/19 15:46
 *
 */
object RSAUtil {
    const val KEY_ALGORITHM = "RSA"
    const val ECB_PKCS1_PADDING = "RSA/ECB/PKCS1Padding"
    const val DEFAULT_KEY_SIZE = 1024
    /**
     * 公钥加密数据
     * @param data 待加密的数据
     * @param key 未经任何转换的服务器公钥
     * @return 加密成功则返回加密后的字节数组，否则返回0字节数组
     */
    fun encryptByPublicKey(data: ByteArray, key: ByteArray): ByteArray {
        return try {
            val keyFactory = KeyFactory.getInstance(KEY_ALGORITHM)

            val pubKey = keyFactory.generatePublic(X509EncodedKeySpec(key))

            val cipher = Cipher.getInstance(ECB_PKCS1_PADDING)
            cipher.init(Cipher.ENCRYPT_MODE, pubKey)
            val maxLen = (pubKey as RSAPublicKey).modulus.bitLength() / 8 - 11
            encryptAndDecrypt(data, cipher, maxLen)

        } catch (e: Exception) {
            ByteArray(0)
        }

    }

    /**
     * 私钥解密数据
     * @param data 待解密的数据
     * @param key 未经任何转换的本地私钥
     * @return 解密成功则返回解密后的字节数组，否则返回0字节数组
     */
    fun decryptByPrivateKey(data: ByteArray, key: ByteArray): ByteArray {
        return try {
            val keyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
            val privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(key))
            val cipher = Cipher.getInstance(ECB_PKCS1_PADDING)
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            val maxLen = (privateKey as RSAPrivateKey).modulus.bitLength() / 8
            encryptAndDecrypt(data, cipher, maxLen)
        } catch (e: Exception) {
            ByteArray(0)
        }
    }


    /**
     * 加解密分组实现
     * @param data 待处理的数据
     * @param cipher 使用的秘钥
     * @param maxLen 最大分组长度
     * @return 处理后的数据
     */
    private fun encryptAndDecrypt(data: ByteArray, cipher: Cipher, maxLen: Int): ByteArray {
        val size = data.size
        return if (size <= maxLen) {
            cipher.doFinal(data)
        } else {
            ByteArrayOutputStream(2 * maxLen).use {
                var position = 0
                while (position < size) {
                    it.write(cipher.doFinal(data, position, maxLen.coerceAtMost(size - position)))
                    position += maxLen
                }
                it.toByteArray()
            }
        }

    }


    /**
     * 生成公私钥
     * @param length 秘钥长度512-2048 一般为1024
     */
    fun generateRSAKeyPair(@IntRange(from = 512, to = 2048) length: Int = DEFAULT_KEY_SIZE): KeyPair {
        val key = KeyPairGenerator.getInstance(KEY_ALGORITHM)
        key.initialize(length)
        return key.genKeyPair()
    }
}