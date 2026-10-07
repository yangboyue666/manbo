package com.yby.manbo;

public class BadWordFilter {
    private static final String[] BAD_WORDS = {
        "广告代刷", "加微信", "兼职刷单", "刷单", "代开发票", "办证",
        "点击领取", "免费领取", "中奖了", "恭喜获得", "扫码领取", "博彩", "私彩"
    };

    public static String check(String text) {
        if (text == null) return null;
        String lower = text.toLowerCase();
        for (String w : BAD_WORDS) {
            if (lower.contains(w.toLowerCase())) return w;
        }
        return null;
    }
}