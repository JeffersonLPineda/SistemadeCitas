package com.citas.util;

/** Utilidad minima para generar JSON a mano (API REST basica de la agenda). */
public final class Json {

    private Json() {
    }

    public static String texto(String valor) {
        if (valor == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (char c : valor.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append('"').toString();
    }
}
