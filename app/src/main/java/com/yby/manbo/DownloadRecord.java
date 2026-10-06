package com.yby.manbo;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class DownloadRecord {
    private static final String PREFS = "manbo_downloads";
    private static final String KEY_LIST = "list";
    private final SharedPreferences sp;

    public static class Item {
        public String name;
        public String url;
        public String time;
        public Item(String n, String u, String t) { name = n; url = u; time = t; }
    }

    public DownloadRecord(Context ctx) {
        sp = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void add(String name, String url) {
        try {
            JSONArray arr = new JSONArray(sp.getString(KEY_LIST, "[]"));
            JSONObject o = new JSONObject();
            o.put("name", name);
            o.put("url", url);
            o.put("time", new java.text.SimpleDateFormat("MM-dd HH:mm").format(new java.util.Date()));
            arr.put(o);
            sp.edit().putString(KEY_LIST, arr.toString()).apply();
        } catch (JSONException ignored) { }
    }

    public List<Item> getAll() {
        List<Item> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(sp.getString(KEY_LIST, "[]"));
            for (int i = arr.length() - 1; i >= 0; i--) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new Item(o.getString("name"), o.getString("url"), o.optString("time", "")));
            }
        } catch (JSONException ignored) { }
        return list;
    }

    public void clear() { sp.edit().remove(KEY_LIST).apply(); }
}
