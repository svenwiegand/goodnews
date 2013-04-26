package com.gettingmobile.Security;

import android.util.Log;
import com.gettingmobile.io.CharacterSet;

import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Hash {
    private static final String LOG_TAG = "goodnews.Hash";
    private static SoftReference<Hash> INSTANCE = null;
    private final MessageDigest messageDigest;

    public static Hash getInstance() {
        Hash hash = INSTANCE != null ? INSTANCE.get() : null;
        if (hash == null) {
            hash = new Hash();
            INSTANCE = new SoftReference<Hash>(hash);
        }
        return hash;
    }

    private Hash() {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            Log.e(LOG_TAG, "Failed to create MD5 MessageDigest.", ex);
            throw new RuntimeException(ex);
        }
    }

    public byte[] create(String data, String salt) {
        messageDigest.reset();
        if (salt != null) {
            messageDigest.update(CharacterSet.stringToBytes(salt));
        }
        messageDigest.update(CharacterSet.stringToBytes(data));
        return messageDigest.digest();
    }

    public byte[] create(String data) {
        return create(data, null);
    }
}
