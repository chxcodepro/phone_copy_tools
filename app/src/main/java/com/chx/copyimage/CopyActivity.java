package com.chx.copyimage;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 接收相册“分享”过来的图片，复制一份到本 App 的缓存，
 * 再通过 ImageProvider 的 content:// 地址放进剪贴板。
 * 这样任何支持粘贴图片的 App（微信、QQ、输入法、浏览器等）都能读取。
 */
public class CopyActivity extends Activity {

    private final Handler main = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final List<Uri> sources = collectUris(getIntent());
        if (sources.isEmpty()) {
            android.util.Log.e("CopyImage", "no image in intent: " + getIntent());
            toast("没有收到图片");
            finish();
            return;
        }
        final ContentResolver resolver = getContentResolver();
        new Thread(() -> {
            try {
                List<Uri> copied = copyToCache(resolver, sources);
                main.post(() -> putOnClipboard(copied));
            } catch (Exception e) {
                android.util.Log.e("CopyImage", "copy failed", e);
                main.post(() -> {
                    toast("复制失败：" + e.getMessage());
                    finish();
                });
            }
        }, "copy-image").start();
    }

    @SuppressWarnings("deprecation")
    private static List<Uri> collectUris(Intent intent) {
        List<Uri> list = new ArrayList<>();
        if (intent == null) return list;
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            Uri u = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (u != null) list.add(u);
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            ArrayList<Uri> us = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (us != null) list.addAll(us);
        }
        // 有些相册只通过 ClipData 传递
        if (list.isEmpty() && intent.getClipData() != null) {
            ClipData cd = intent.getClipData();
            for (int i = 0; i < cd.getItemCount(); i++) {
                Uri u = cd.getItemAt(i).getUri();
                if (u != null) list.add(u);
            }
        }
        return list;
    }

    private List<Uri> copyToCache(ContentResolver resolver, List<Uri> sources) throws Exception {
        File dir = ImageProvider.imageDir(this);
        // 清掉上一次复制的图片，避免缓存越积越多
        File[] old = dir.listFiles();
        if (old != null) for (File f : old) f.delete();

        List<Uri> result = new ArrayList<>();
        long stamp = System.currentTimeMillis();
        for (int i = 0; i < sources.size(); i++) {
            Uri src = sources.get(i);
            String ext = extFor(resolver.getType(src));
            String name = "IMG_" + stamp + "_" + i;
            File out;
            if (ext != null) {
                // 常见格式：原样复制，不损失画质
                out = new File(dir, name + "." + ext);
                try (InputStream in = resolver.openInputStream(src);
                     OutputStream os = new FileOutputStream(out)) {
                    if (in == null) throw new IllegalStateException("无法读取图片");
                    byte[] buf = new byte[64 * 1024];
                    int n;
                    while ((n = in.read(buf)) > 0) os.write(buf, 0, n);
                }
            } else {
                // HEIC 等很多 App 不认的格式：转成 JPEG
                out = new File(dir, name + ".jpg");
                Bitmap bmp = ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(resolver, src),
                        (decoder, info, s) -> decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE));
                try (OutputStream os = new FileOutputStream(out)) {
                    bmp.compress(Bitmap.CompressFormat.JPEG, 95, os);
                }
                bmp.recycle();
            }
            result.add(ImageProvider.uriFor(out));
        }
        return result;
    }

    /** 能原样放进剪贴板的格式；其余返回 null 表示需要转码。 */
    private static String extFor(String mime) {
        if (mime == null) return null;
        switch (mime) {
            case "image/jpeg":
            case "image/jpg":
                return "jpg";
            case "image/png":
                return "png";
            case "image/gif":
                return "gif";
            case "image/webp":
                return "webp";
            default:
                return null;
        }
    }

    private void putOnClipboard(List<Uri> uris) {
        ContentResolver resolver = getContentResolver();
        ClipData clip = ClipData.newUri(resolver, "图片", uris.get(0));
        for (int i = 1; i < uris.size(); i++) {
            clip.addItem(resolver, new ClipData.Item(uris.get(i)));
        }
        ClipboardManager cm = getSystemService(ClipboardManager.class);
        cm.setPrimaryClip(clip);

        // Android 13+ 系统会自己弹“已复制”提示，这里只在旧系统补一个
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            toast(uris.size() > 1 ? "已复制 " + uris.size() + " 张图片" : "图片已复制");
        }
        finish();
    }

    private void toast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
