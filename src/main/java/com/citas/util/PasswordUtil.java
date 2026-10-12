package com.citas.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Almacenamiento seguro de contrasenas: PBKDF2-HMAC-SHA256 con sal aleatoria.
 * Formato guardado: {@code pbkdf2$iteraciones$salHex$hashHex}. Nunca se guarda la contrasena en claro.
 */
public final class PasswordUtil {

    private static final int ITERACIONES = 65536;
    private static final int BITS_CLAVE = 256;
    private static final int BYTES_SAL = 16;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hashear(String password) {
        byte[] sal = new byte[BYTES_SAL];
        ALEATORIO.nextBytes(sal);
        byte[] hash = derivar(password.toCharArray(), sal, ITERACIONES);
        return "pbkdf2$" + ITERACIONES + "$" + aHex(sal) + "$" + aHex(hash);
    }

    public static boolean verificar(String password, String almacenado) {
        if (password == null || almacenado == null) {
            return false;
        }
        String[] partes = almacenado.split("\\$");
        if (partes.length != 4 || !"pbkdf2".equals(partes[0])) {
            return false;
        }
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] esperado = deHex(partes[3]);
            byte[] obtenido = derivar(password.toCharArray(), deHex(partes[2]), iteraciones);
            return MessageDigest.isEqual(esperado, obtenido);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static byte[] derivar(char[] password, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, sal, iteraciones, BITS_CLAVE);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 no disponible en esta JVM", e);
        }
    }

    public static String aHex(byte[] datos) {
        StringBuilder sb = new StringBuilder(datos.length * 2);
        for (byte b : datos) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] deHex(String hex) {
        byte[] r = new byte[hex.length() / 2];
        for (int i = 0; i < r.length; i++) {
            r[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return r;
    }
}
