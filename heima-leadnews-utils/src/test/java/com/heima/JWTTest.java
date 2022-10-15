package com.heima;

import com.heima.utils.common.JwtUtils;
import com.heima.utils.common.Payload;
import com.heima.utils.common.RsaUtils;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JWTTest {
    public static final String publicKeyPath = "D:\\Code\\keys\\rsa\\rsa-key.pub";
    public static final String privateKeyPath = "D:\\Code\\keys\\rsa\\rsa-key";


    /**
     * 生成token
     */
    @Test
    public void testGenToken() throws Exception {
        /**
         * 参数一：登录用户信息
         * 参数二：私钥key
         * 参数三：过期时间（分）
         */
        User loginUser = new User(1,"jack");
        PrivateKey privateKey = RsaUtils.getPrivateKey(privateKeyPath);
        String token = JwtUtils.generateTokenExpireInMinutes(loginUser, privateKey, 1);
        System.out.println(token);
    }

    @Test
    public void verifyToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJ1c2VyIjoie1wiaWRcIjoxLFwibmFtZVwiOlwiamFja1wifSIsImp0aSI6Ik4yWXhORGhrWm1NdE1HTTBZaTAwTjJSbUxUZzRZMkl0T0RrMFpqQXhOMlF5T1dNMCIsImV4cCI6MTY2NDA5MzA2Nn0.QWtYpZzXDNgoQoHK-hyNuKJnCVQd7pimdlIVjY_chloFxVliNTxyzHh3vYBRikGB1ouJnWPULyQnrpOES1PxjcfT38-ei0w5d3um0j_AMMUG5M5QYwLL719n-kPU9SGnsi4Z9zOC-4K0yfAeGqKU666UVLOvyYP14GS0AQbu-W-fGNNLea6KM2LpYG-2tQBWokasWAgizzGpq8akCVF9Z4t6ismkw1D3l0ys2u7L9de0BlJBPCr1Ku7QRiiICX6P8sELF6dFTR52idcxOSEIAKDclmH_WHgqJPQuPn5BVCNYpSfRKQ-tvRO3mrJXvK28BqPnXLe0Zm3EMy-bMdDFJQ";
        PublicKey publicKey = RsaUtils.getPublicKey(publicKeyPath);
        try {
            Payload<User> payload = JwtUtils.getInfoFromToken(token, publicKey, User.class);
            System.out.println("login");
            User user = payload.getInfo();
            System.out.println(user);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("failed");
        }
    }
}
