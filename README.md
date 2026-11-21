# 复制图片（CopyImage）

有些安卓手机（比如 ColorOS / OxygenOS）的相册没有「复制」按钮，没法把照片直接复制到剪贴板再粘贴到别处。

这个小工具给系统分享菜单加了一项 **「复制到剪贴板」**：在相册里分享照片时点它，图片就进了剪贴板，然后到微信、QQ、便签、输入框里长按「粘贴」就行。

## 下载

到 [Releases](../../releases) 下载最新的 `CopyImage-x.y.z.apk`，安装即可。

- 系统要求：Android 9 及以上
- 不需要任何权限，不联网

## 使用

1. 打开相册，选中一张或多张照片
2. 点「分享」
3. 在分享面板里找到「复制到剪贴板」（可能要左右滑动找一下）
4. 到目标 App 的输入框长按，选「粘贴」

## 细节

- JPG / PNG / GIF / WebP 原样复制，不损失画质
- HEIC 等很多 App 不认的格式会自动转成 JPEG
- 支持一次复制多张（是否能一次粘贴多张取决于目标 App）
- 图片缓存在 App 私有目录，每次复制新图片时会清掉上一次的缓存

## 自己编译

```bash
./gradlew assembleDebug
```

产物在 `app/build/outputs/apk/debug/`。

## 发布

推送 `v*` 格式的 tag 后，GitHub Actions 会自动构建签名 APK 并发布到 Releases：

```bash
git tag v1.0.1
git push origin v1.0.1
```

签名用到的 Secrets：`SIGNING_KEYSTORE_BASE64`、`SIGNING_STORE_PASSWORD`、`SIGNING_KEY_ALIAS`、`SIGNING_KEY_PASSWORD`。未配置时会退回 debug 签名。
