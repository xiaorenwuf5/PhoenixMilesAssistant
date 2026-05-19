# 凤凰知音累计助手

一个用于 vivo X100 Pro 的 Android MVP：从阿里商旅截图或分享文本中识别国航航班、日期、航段和舱位，快速估算凤凰知音可用里程、定级里程、定级航段，以及 2026 年国航快线额外定级航段。

## 使用方式

1. 用 Android Studio 打开 `PhoenixMilesAssistant` 目录。
2. 等待 Gradle 同步并连接 vivo X100 Pro。
3. 运行 `app`。
4. 在阿里商旅页面截图后，打开本 App 选择截图，或从相册分享图片到本 App。

## 不安装 Android Studio：用 GitHub 构建 APK

1. 在 GitHub 新建一个空仓库，例如 `PhoenixMilesAssistant`。
2. 把本目录 `PhoenixMilesAssistant` 里的所有文件上传到仓库根目录。
3. 进入仓库的 `Actions` 页面。
4. 打开 `Build debug APK` 工作流，点 `Run workflow`。
5. 等构建成功后，在这次 workflow run 的 `Artifacts` 区域下载 `PhoenixMilesAssistant-debug-apk`。
6. 解压后把 `app-debug.apk` 发到 X100 Pro，点开安装。

这个 APK 是 debug 包，只适合自用测试，不建议发给别人长期使用。

## 构建环境

- 推荐 Android Studio 2026 版本或更新版本。
- 项目使用 Android Gradle Plugin `9.0.1`、`compileSdk 36`、Java 17。
- 这个目录没有自带 Gradle Wrapper；如果 Android Studio 提示缺少 Wrapper，可以让 Android Studio 使用本机 Gradle，或在项目打开成功后通过 IDE 生成 Wrapper。
- 首次 Gradle 同步需要联网下载 Android Gradle Plugin 和 ML Kit OCR 依赖。
- 如果用 GitHub Actions，不需要本机安装 Java、Gradle、Android SDK 或 Android Studio。

## vivo X100 Pro 安装提示

1. 手机开启开发者选项和 USB 调试。
2. Android Studio 识别手机后直接点 Run。
3. 如果系统拦截安装，允许“通过 USB 安装”。
4. App 不需要读取账号，不需要悬浮窗，不需要无障碍权限；第一版只处理你主动选择或分享过来的截图。

## 当前能力

- 支持 OCR 识别中文截图，使用 ML Kit bundled Chinese text recognition。
- 支持识别 `CA4132`、`05-20`、`首都国际机场T3 -> 江北国际机场T3`、`经济舱(S)` 这类阿里商旅页面。
- 支持手工修正字段后重新计算。
- 支持 2026 年国航快线额外定级航段规则。
- 支持复制结果、跳转国航官方累计计算器核对。

## 注意

- 本地航距使用机场坐标估算，国航最终入账以 IATA 航距、旅行日规则、实际承运航司和官方活动规则为准。
- 第一版重点解决差旅平台页面快速判断舱位累计的问题，不读取阿里商旅或国航账号。
