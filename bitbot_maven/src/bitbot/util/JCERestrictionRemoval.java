package bitbot.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.Security;

/**
 *
 * @author
 */
public class JCERestrictionRemoval {

    private static double getJavaVersion() {
        String version = System.getProperty("java.version");
        int pos = version.indexOf('.');
        pos = version.indexOf('.', pos + 1);
        return Double.parseDouble(version.substring(0, pos));
    }

    // attempt to enable unlimited-strength crypto on OracleJDK
    private static boolean checkCryptoRestrictions() {
        if (getJavaVersion() >= 1.9) {
            return setNonRestrictedJava9();
        } else {
            return setNonRestrictedJava8();
        }
    }

    private static boolean setNonRestrictedJava9() {
        try {
            if (Security.getProperty("crypto.policy") == null) {
                Security.setProperty("crypto.policy", "unlimited");
            }
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    private static boolean setNonRestrictedJava8() {
        try {
            Class jceSecurity = Class.forName("javax.crypto.JceSecurity");
            Field isRestricted = jceSecurity.getDeclaredField("isRestricted");

            if (Modifier.isFinal(isRestricted.getModifiers())) {
                Field modifiers = Field.class.getDeclaredField("modifiers");
                modifiers.setAccessible(true);
                modifiers.setInt(isRestricted, isRestricted.getModifiers() & ~Modifier.FINAL);
            }

            isRestricted.setAccessible(true);
            isRestricted.setBoolean(null, false); // isRestricted = false;
            isRestricted.setAccessible(false);

            return true;
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
        }
        return false;
    }

    /**
     * Credits: ntoskrnl of StackOverflow
     * http://stackoverflow.com/questions/1179672/
     *
     */
    public static void removeCryptographyRestrictions() {
        if (!isRestrictedCryptography()) {
            System.out.println("Cryptography restrictions removal not needed");
            return;
        }
        if (!checkCryptoRestrictions()) {
            System.out.println("Cryptography restrictions removal not needed");
        } else {
            System.out.println("Cryptography restrictions removed");
        }
    }

    private static boolean isRestrictedCryptography() {
        // This simply matches the Oracle JRE, but not OpenJDK.
        return "Java(TM) SE Runtime Environment".equals(System.getProperty("java.runtime.name"));
    }
}
