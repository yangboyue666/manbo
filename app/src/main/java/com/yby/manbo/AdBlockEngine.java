package com.yby.manbo;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class AdBlockEngine {
    private final Set<String> blockedHosts = new HashSet<>();

    public AdBlockEngine() {
        String[] hosts = {
            "doubleclick.net", "googlesyndication.com", "googleadservices.com",
            "google-analytics.com", "adservice.google.com", "adnxs.com",
            "pubmatic.com", "rubiconproject.com", "openx.net", "criteo.com",
            "taboola.com", "outbrain.com", "ads.yahoo.com", "hotjar.com",
            "mixpanel.com", "segment.io", "amplitude.com", "advertising.com",
            "2mdn.net", "serving-sys.com", "moatads.com", "scorecardresearch.com",
            "quantserve.com", "chartbeat.com", "fullstory.com", "clarity.ms",
            "mc.yandex.ru", "ad.mail.ru", "an.yandex.ru", "tns-counter.com",
            "umeng.com", "umeng.co", "tanx.com", "mediav.com", "miaozhen.com"
        };
        for (String h : hosts) blockedHosts.add(h);
    }

    public boolean isBlocked(String url) {
        if (url == null) return false;
        try {
            String host = new URL(url).getHost();
            if (host == null) return false;
            host = host.toLowerCase();
            for (String b : blockedHosts) {
                if (host.equals(b) || host.endsWith("." + b)) return true;
            }
        } catch (Exception e) { }
        return false;
    }
}
