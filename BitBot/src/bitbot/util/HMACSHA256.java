package bitbot.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author z
 */
public class HMACSHA256 {

    public static String encode(String key, String data) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            return Base64.encodeBase64String(sha256_HMAC.doFinal(data.getBytes()));

        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
        }
        return null;
    }

    public static String decode(String keyStr, String data) {
        try {
            SecretKey key = new SecretKeySpec(keyStr.getBytes(),"hmacSHA256");

            Mac hmacSha256 = Mac.getInstance("hmacSHA256");
            hmacSha256.init(key);
            // decode the info.
            byte[] mac = hmacSha256.doFinal(Base64.decodeBase64(data.getBytes()));

            return new String(mac);
            
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {

        }
        return null;
    }
}
