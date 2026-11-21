package com.chx.copyimage;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;

/** 点桌面图标时显示用法说明。 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new AlertDialog.Builder(this)
                .setTitle("复制图片")
                .setMessage("用法：\n\n1. 打开相册，选中一张（或多张）照片\n"
                        + "2. 点「分享」\n3. 选择「复制到剪贴板」\n\n"
                        + "然后到微信、QQ、输入框等地方长按「粘贴」即可。\n\n"
                        + "HEIC 等特殊格式会自动转成 JPEG。")
                .setPositiveButton("知道了", (d, w) -> finish())
                .setOnCancelListener(d -> finish())
                .show();
    }
}
