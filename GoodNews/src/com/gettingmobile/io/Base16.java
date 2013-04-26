package com.gettingmobile.io;

public final class Base16 {
    private Base16() {
    }

    private static char encodeNibble(int nibble) {
        final byte n = (byte) (nibble & 0x0f);
        return (char) ((n < 10) ? '0' + n : 'a' + (n - 10));
    }

    public static String encode(byte[] data) {
        StringBuffer s = new StringBuffer();
        for (byte b : data) {
            s.append(encodeNibble(b >> 4)).append(encodeNibble(b & 0x0f));
        }
        return s.toString();
    }

    private static byte decodeNibble(char nc) {
        final char c = Character.toLowerCase(nc);
        if (c >= '0' && c <= '9') {
            return (byte) (c - '0');
        } else if (c >= 'a' && c <= 'f') {
            return (byte) (10 + c - 'a');
        } else {
            throw new IllegalArgumentException("Invalid nibble " + nc);
        }
    }

    public static byte[] decode(String encoded) {
        if (encoded.length() % 2 != 0)
            throw new IllegalArgumentException("No valid hexadecimal string");

        final byte[] data = new byte[encoded.length() / 2];
        for (int i = 0; i < encoded.length(); i+= 2) {
            data[i / 2] = (byte) ((decodeNibble(encoded.charAt(i)) << 4) | decodeNibble(encoded.charAt(i + 1)));
        }
        return data;
    }
}
