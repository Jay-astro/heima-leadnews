package com.heima;

import com.heima.utils.common.BCrypt;
import com.heima.utils.common.RsaUtils;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class BCryptTest {

    public static final String publicKeyPath = "D:\\Code\\keys\\rsa\\rsa-key.pub";
    public static final String privateKeyPath = "D:\\Code\\keys\\rsa\\rsa-key";

    /**
     * 生成公私钥
     *
     * @throws Exception
     */
    @Test
    public void testGenRsa() throws Exception {
        RsaUtils.generateKey(publicKeyPath, privateKeyPath, "jay", 1024);
    }

    /**
     * 公钥
     *
     * @throws Exception
     */
    @Test
    public void testGetpublicKey() throws Exception {
        PublicKey publicKey = RsaUtils.getPublicKey(publicKeyPath);
        System.out.println(publicKey);
    }

    /**
     * 私钥
     *
     * @throws Exception
     */
    @Test
    public void testGetprivateKey() throws Exception {
        PrivateKey privateKey = RsaUtils.getPrivateKey(privateKeyPath);
        System.out.println(privateKey);
    }

    /**
     * 加密
     */
    @Test
    public void testEncode() {
        //产生随机盐
        String salt = BCrypt.gensalt();
        System.out.println(salt);
        //加密
        String pwd = BCrypt.hashpw("admin", salt);
        System.out.println(pwd);
    }

    /**
     * 验证
     */
    @Test
    public void testMatch() {
        String pwd = "$2a$10$1ZNMO89VzYrpzw2K.cF8QeJ/smPrpFG3zkYPH7b0aueoIcr205n6q";
        boolean flag = BCrypt.checkpw("admin", pwd);
        System.out.println(flag);
    }
}
