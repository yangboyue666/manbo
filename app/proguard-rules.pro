# Keep WebView related classes
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.yby.manbo.** { *; }
