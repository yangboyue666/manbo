package com.yby.manbo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {
    private static final String RELEASE_API = "https://api.github.com/repos/yangboyue666/manbo/releases/latest";
    private static final String MIRROR = "https://ghfast.top/";

    public interface Callback {
        void onResult(boolean ok, String downloadUrl, long size, String msg);
    }

    public static void check(final Callback cb) {
        new Thread(() -> {
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(RELEASE_API).openConnection();
                c.setRequestProperty("User-Agent", "Manbo");
                c.setConnectTimeout(10000);
                BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String l;
                while ((l = r.readLine()) != null) sb.append(l);
                r.close();
                c.disconnect();
                String json = sb.toString();
                String url = extract(json, "browser_download_url");
                String sizeStr = extract(json, "size");
                long size = Long.parseLong(sizeStr);
                cb.onResult(true, url, size, null);
            } catch (Exception e) {
                cb.onResult(false, null, 0, e.getMessage());
            }
        }).start();
    }

    private static String extract(String json, String key) {
        String pat = "\"" + key + "\":";
        int i = json.indexOf(pat);
        if (i < 0) return null;
        i += pat.length();
        if (json.charAt(i) == '"') {
            int j = json.indexOf('"', i + 1);
            return json.substring(i + 1, j);
        } else {
            int j = i;
            while (j < json.length() && (Character.isDigit(json.charAt(j)) || json.charAt(j) == '-')) j++;
            return json.substring(i, j);
        }
    }

    public static void downloadAndInstall(final Activity act, final String githubUrl) {
        new Thread(() -> {
            try {
                String url = MIRROR + githubUrl;
                HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
                c.setConnectTimeout(15000);
                c.setReadTimeout(60000);
                InputStream in = c.getInputStream();
                File out = new File(act.getCacheDir(), "manbo_update.apk");
                FileOutputStream fos = new FileOutputStream(out);
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) fos.write(buf, 0, n);
                in.close();
                fos.close();
                c.disconnect();
                act.runOnUiThread(() -> install(act, out));
            } catch (Exception e) {
                act.runOnUiThread(() -> Toast.makeText(act, "下载失败: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private static void install(Activity act, File apk) {
        try {
            PackageInstaller installer = act.getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                    PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            int sessionId = installer.createSession(params);
            PackageInstaller.Session session = installer.openSession(sessionId);
            InputStream in = new FileInputStream(apk);
            OutputStream out = session.openWrite("manbo.apk", 0, apk.length());
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            in.close();
            out.close();
            Intent resIntent = new Intent(act, act.getClass());
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (android.os.Build.VERSION.SDK_INT >= 26) flags |= PendingIntent.FLAG_MUTABLE;
            PendingIntent pi = PendingIntent.getActivity(act, sessionId, resIntent, flags);
            session.commit(pi.getIntentSender());
            session.close();
        } catch (Exception e) {
            Toast.makeText(act, "安装失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}