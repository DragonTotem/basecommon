package com.zbt.common.security

import com.google.common.truth.Truth
import org.junit.Test

/**
 * Description:RSA加解密验证测试
 *
 * @Author: xuwd11
 * Date: 2021/2/20 11:24
 */
class RSAUtilTest {
    val textShort = "hello 小明"
    val textLong = "RSA公开密钥密码体制是一种使用不同的加密密钥与解密密钥，“由已知加密密钥推导出解密密钥在计算上是不可行的”密码体制。" +
            "在公开密钥密码体制中，加密密钥（即公开密钥）PK是公开信息，而解密密钥（即秘密密钥）SK是需要保密的。加密算法E和解密算法D也都是公开的。虽然解密密钥SK是由公开密钥PK决定的，但却不能根据PK计算出SK。" +
            "正是基于这种理论，1978年出现了著名的RSA算法，它通常是先生成一对RSA密钥，其中之一是保密密钥，由用户保存；另一个为公开密钥，可对外公开，甚至可在网络服务器中注册。为提高保密强度，RSA密钥至少为500位长，一般推荐使用1024位。这就使加密的计算量很大。为减少计算量，在传送信息时，常采用传统加密方法与公开密钥加密方法相结合的方式，即信息采用改进的DES或IDEA对话密钥加密，然后使用RSA密钥加密对话密钥和信息摘要。对方收到信息后，用不同的密钥解密并可核对信息摘要"


    @Test
    fun decodeByPrivateKey() {
        val keyPair = RSAUtil.generateRSAKeyPair()

        val temp1 = RSAUtil.encryptByPublicKey(textShort.toByteArray(), keyPair.public.encoded)
        val raw1 = RSAUtil.decryptByPrivateKey(temp1, keyPair.private.encoded)
        Truth.assertThat(textShort).isEqualTo(String(raw1))

        val temp2 = RSAUtil.encryptByPublicKey(textLong.toByteArray(), keyPair.public.encoded)
        val raw2 = RSAUtil.decryptByPrivateKey(temp2, keyPair.private.encoded)
        Truth.assertThat(textLong).isEqualTo(String(raw2))

    }
}