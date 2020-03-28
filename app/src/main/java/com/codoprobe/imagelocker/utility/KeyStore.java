package com.codoprobe.imagelocker.utility;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class KeyStore {
    private static final String ENCRYPTION_KEY = "encryption_key"; // the key name
    private static final String KEY_STORE_ID = "image_locker_key_store";

    public static String getEncryptionKey(Context ctx) {

        return ctx.getSharedPreferences(KEY_STORE_ID, Context.MODE_PRIVATE)
                .getString(ENCRYPTION_KEY, "pee pee poo pooo");

    }

    public static void changeEncryptionKey(Context ctx, String new_key, String old_key) throws Exception {
        if(getEncryptionKey(ctx).equals(old_key)) {

            if (new_key.length() == 16 ||
                new_key.length() == 24 ||
                new_key.length() == 32)
            {

                SharedPreferences sharedpreferences = ctx.getSharedPreferences(KEY_STORE_ID, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(ENCRYPTION_KEY, new_key);
                editor.apply();

            }
            else {
                throw new Exception("Key Length must be 16, 24 or 32 characters");
            }
        }
        else {
            throw new Exception("Old key does not match");
        }
    }


    public static boolean authenticate(Context ctx, String entered_key) {
        return KeyStore.getEncryptionKey(ctx).equals(entered_key);
    }
}
