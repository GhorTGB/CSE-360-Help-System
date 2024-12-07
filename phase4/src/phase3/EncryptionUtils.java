package phase3;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtils {
    private static final String ALGORITHM = "AES";
    private static final String DEFAULT_KEY = "defaultkey123456";

    private static final String ENCRYPTION_KEY;

    static {
        String key = System.getenv("ENCRYPTION_KEY");
        if (key == null || !(key.length() == 16 || key.length() == 32)) {
            key = DEFAULT_KEY;
        }
        ENCRYPTION_KEY = key;
    }

    public static String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String cipherText) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decoded = Base64.getDecoder().decode(cipherText);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted, "UTF-8");
    }
}