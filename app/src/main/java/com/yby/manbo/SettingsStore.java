package com.yby.manbo;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsStore {
    private static final String PREFS = "manbo_settings";
    private static final String KEY_BACKEND_URL = "backend_url";
    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_MODE = "chat_mode";
    private static final String KEY_SUPA_KEY = "supabase_key";
    private static final String KEY_CLIENT_ID = "client_id";
    private static final String KEY_HOME = "home_url";
    private static final String KEY_ADBLOCK = "adblock_enabled";
    private static final String KEY_ADDR_BOTTOM = "addr_bar_bottom";
    private static final String KEY_DESKTOP = "desktop_mode";
    private static final String KEY_HOME_TITLE = "home_title";
    private final SharedPreferences sp;

    public SettingsStore(Context ctx) {
        sp = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public String getBackendUrl() { return sp.getString(KEY_BACKEND_URL, "https://nrsazdivhncogdvezzwi.supabase.co"); }
    public void setBackendUrl(String url) { sp.edit().putString(KEY_BACKEND_URL, url).apply(); }

    public String getNickname() { return sp.getString(KEY_NICKNAME, ""); }
    public void setNickname(String n) { sp.edit().putString(KEY_NICKNAME, n).apply(); }

    public int getMode() { return sp.getInt(KEY_MODE, 1); }
    public void setMode(int m) { sp.edit().putInt(KEY_MODE, m).apply(); }

    public String getSupabaseKey() { return sp.getString(KEY_SUPA_KEY, "sb_publishable_qXVeFhCPmBHpDc9EdFvMqw_3JwJlakj"); }
    public void setSupabaseKey(String k) { sp.edit().putString(KEY_SUPA_KEY, k).apply(); }

    public String getClientId() {
        String id = sp.getString(KEY_CLIENT_ID, null);
        if (id == null) {
            id = "m" + System.currentTimeMillis() + (int)(Math.random()*10000);
            sp.edit().putString(KEY_CLIENT_ID, id).apply();
        }
        return id;
    }

    public String getHomeUrl() { return sp.getString(KEY_HOME, "https://www.bing.com"); }
    public void setHomeUrl(String u) { sp.edit().putString(KEY_HOME, u).apply(); }

    public boolean isConfigured() {
        String u = getBackendUrl();
        return u != null && !u.trim().isEmpty();
    }

    public boolean isAdBlockEnabled() { return sp.getBoolean(KEY_ADBLOCK, true); }
    public void setAdBlockEnabled(boolean e) { sp.edit().putBoolean(KEY_ADBLOCK, e).apply(); }

    public boolean isAddressBarBottom() { return sp.getBoolean(KEY_ADDR_BOTTOM, false); }
    public void setAddressBarBottom(boolean b) { sp.edit().putBoolean(KEY_ADDR_BOTTOM, b).apply(); }

    public boolean isDesktopMode() { return sp.getBoolean(KEY_DESKTOP, false); }
    public void setDesktopMode(boolean d) { sp.edit().putBoolean(KEY_DESKTOP, d).apply(); }

    public String getHomeTitle() { return sp.getString(KEY_HOME_TITLE, "manbo"); }
    public void setHomeTitle(String t) { sp.edit().putString(KEY_HOME_TITLE, t).apply(); }
}
