package bitbot.util.encryption;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author z
 */
public class HMACSHA1 {

    public static String encode(String key, String data) {
        try {
            final Mac sha1_HMAC = Mac.getInstance("HmacSHA1");
            final SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA1");
            sha1_HMAC.init(secret_key);

            return Base64.encodeBytes( sha1_HMAC.doFinal(data.getBytes()) ); 
           // return new String(Base64.encodeBase64(sha1_HMAC.doFinal(data.getBytes())), "ASCII");
            
        } catch ( InvalidKeyException | NoSuchAlgorithmException e) {
        }
        return null;
    }
}
