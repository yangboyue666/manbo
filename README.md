# 曼波浏览器 (Manbo)

基于 Android System WebView 的轻量浏览器，原生 Java 实现，**零第三方依赖**。

## 功能

- 网页浏览（System WebView，不打包 Chromium 内核，体积小）
- 书签管理（本地 SharedPreferences 存储）
- Cookie 支持（WebView 内置，含第三方 Cookie）
- 文件下载（调用系统下载管理器，断点续传由系统负责）
- 在线聊天（轮询后端，3 秒刷新，支持 PHP / Supabase 两种模式）
- 地址栏智能识别（网址直接访问，非网址用 Bing 搜索）

## 聊天后端配置

打开 App → 菜单 `≡` → **设置**：

| 模式 | 后端地址填什么 | 接口约定 |
|------|---------------|---------|
| PHP 轮询 | 你的服务器根目录，如 `https://xxx.com/` | `send.php`(POST nickname/content/client_id) + `get.php`(GET 返回 JSON 数组) |
| Supabase REST | Supabase 项目 URL，如 `https://xxx.supabase.co/` | REST API `/rest/v1/messages` |

还需填写**昵称**（聊天显示名）。Supabase 模式需额外配 anon key（当前版本在 SettingsStore 中预留，下版加设置项）。

## 构建

### GitHub Actions（推荐）
仓库 **Actions** 页 → `Build APK` → `Run workflow`（手动触发）。
构建完成后在 Release 页下载 APK。

### 本地构建
```bash
gradle wrapper          # 首次生成 wrapper
./gradlew assembleDebug # debug 包
./gradlew assembleRelease  # release 包（需配置签名）
```
产物：`app/build/outputs/apk/debug/app-debug.apk`

## 技术栈

- Java 17，Android API 26+（Android 8.0+）
- Android System WebView（跟随系统更新，不打包内核）
- **零第三方依赖**（dependencies 为空，全手写，体积最小化）
- Gradle 8.9 + AGP 8.7.3
- R8 minify + shrinkResources（release 包压缩混淆）

## 项目结构

```
app/src/main/java/com/yby/manbo/
├── MainActivity.java       浏览器主界面（WebView + 地址栏 + 工具栏）
├── ChatActivity.java       聊天页（轮询后端）
├── BookmarkActivity.java   书签管理
├── SettingsActivity.java   设置页（后端地址/昵称/模式）
├── ChatClient.java         HTTP 聊天客户端（PHP/Supabase 双模式）
├── BookmarkManager.java    书签本地存储
├── SettingsStore.java      设置本地存储
├── MessageAdapter.java     聊天消息列表适配器
└── BookmarkAdapter.java    书签列表适配器
```

## 知识产权声明

本项目**所有源码均为原创编写**。开发时参考了 Median Browser (`bi-box/Median`) 的**公开功能列表**（其 README 中的功能描述），**未复制其任何源代码**。本项目与 Median Browser 无代码派生关系，应用名（曼波）、图标（波浪）、包名（`com.yby.manbo`）均与之不同。

## License

本项目源码版权归仓库所有者所有。
