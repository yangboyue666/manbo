package com.yby.manbo;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class PasswordVault {
    private static final String PREFS = "manbo_vault";
    private static final String KEY_LIST = "vault";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String ALIAS = "manbo_vault_key";
    private final SharedPreferences sp;

    public static class Entry {
        public String site;
        public String username;
        public String password;
        public Entry(String s, String u, String p) { site = s; username = u; password = p; }
    }

    public PasswordVault(Context ctx) {
        sp = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private SecretKey getOrCreateKey() throws Exception {
        KeyStore ks = KeyStore.getInstance(ANDROID_KEYSTORE);
        ks.load(null);
        SecretKey key = (SecretKey) ks.getKey(ALIAS, null);
        if (key == null) {
            KeyGenerator kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
            kg.init(new KeyGenParameterSpec.Builder(ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build());
            key = kg.generateKey();
        }
        return key;
    }

    private String encrypt(String plain) throws Exception {
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
        byte[] iv = c.getIV();
        byte[] enc = c.doFinal(plain.getBytes("UTF-8"));
        return Base64.encodeToString(iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(enc, Base64.NO_WRAP);
    }

    private String decrypt(String data) throws Exception {
        String[] parts = data.split(":");
        byte[] iv = Base64.decode(parts[0], Base64.NO_WRAP);
        byte[] enc = Base64.decode(parts[1], Base64.NO_WRAP);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(128, iv));
        return new String(c.doFinal(enc), "UTF-8");
    }

    public List<Entry> getAll() {
        List<Entry> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(sp.getString(KEY_LIST, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new Entry(o.getString("site"), o.getString("username"), decrypt(o.getString("password"))));
            }
        } catch (Exception ignored) { }
        return list;
    }

    public void add(String site, String username, String password) {
        try {
            JSONArray arr = new JSONArray(sp.getString(KEY_LIST, "[]"));
            JSONObject o = new JSONObject();
            o.put("site", site);
            o.put("username", username);
            o.put("password", encrypt(password));
            arr.put(o);
            sp.edit().putString(KEY_LIST, arr.toString()).apply();
        } catch (Exception ignored) { }
    }

    public void remove(int index) {
        try {
            JSONArray arr = new JSONArray(sp.getString(KEY_LIST, "[]"));
            JSONArray newArr = new JSONArray();
            for (int i = 0; i < arr.length(); i++) {
                if (i != index) newArr.put(arr.get(i));
            }
            sp.edit().putString(KEY_LIST, newArr.toString()).apply();
        } catch (JSONException ignored) { }
    }
}
