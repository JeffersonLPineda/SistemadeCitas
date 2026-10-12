package com.citas.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordUtilTest {

    @Test
    void verificaLaContrasenaCorrectaYRechazaLaIncorrecta() {
        String hash = PasswordUtil.hashear("Secreta#123");
        assertTrue(PasswordUtil.verificar("Secreta#123", hash));
        assertFalse(PasswordUtil.verificar("secreta#123", hash));
        assertFalse(PasswordUtil.verificar("Secreta#123", "texto-sin-formato"));
    }

    @Test
    void nuncaGuardaLaContrasenaEnClaveYUsaSalDistinta() {
        String a = PasswordUtil.hashear("Secreta#123");
        String b = PasswordUtil.hashear("Secreta#123");
        assertFalse(a.contains("Secreta#123"));
        assertNotEquals(a, b);
    }
}
