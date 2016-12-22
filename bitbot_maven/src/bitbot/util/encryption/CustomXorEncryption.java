package bitbot.util.encryption;

/**
 *
 * @author zheng
 */
public class CustomXorEncryption {

    public static String custom_xor_encrypt(String toEncrypt, long nonce) {
        char[] key = {'k', 'c',  (char) (nonce % 10)};

        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < toEncrypt.length(); i++) {
            sb.append((char) (toEncrypt.charAt(i) ^ key[i % key.length] & 0xFF));
        }
        return sb.toString();
    }
}
