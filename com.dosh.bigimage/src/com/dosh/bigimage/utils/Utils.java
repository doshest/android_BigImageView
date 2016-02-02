
package com.dosh.bigimage.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.lang.reflect.Method;

public class Utils {
    public static boolean isHardwareAccelerated(View view) {
        boolean accelerated = false;
        try {
            Class viewClass = View.class;
            Method isHardwareAccelerated = viewClass.getMethod("isHardwareAccelerated", null);
            accelerated = (Boolean)isHardwareAccelerated.invoke(view, null);
        } catch (Exception e) {
            Log.e("dosh_bgimage", e.toString());
        }

        return accelerated;
    }

    /**
     * 获取图片的保存目录，以“/”结尾
     * 
     * @param context
     * @return
     */
    public static final String getPictureImgSaveDir(Context context) {
        return getSDPath() + "/dosh_image/test.jpg";
    }

    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
        }
        if (sdDir != null)
            return sdDir.toString();
        else
            return null;

    }

    public static Bitmap createBitmap(String path, float scale) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        if (scale <= 0) {
            return null;
        }

        File f = new File(path);
        if (!f.exists()) {
            return null;
        }
        int[] rgbData = parseWebp(path, scale);
        int width = 0, height = 0;
        if (rgbData == null || rgbData.length <= 2 || (width = rgbData[0]) <= 0 || (height = rgbData[1]) <= 0) {
            return null;
        }

        try {
            return Bitmap.createBitmap(rgbData, 2, width, width, height, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            System.gc();
            throw e;
        }
    }

    public static int[] parseWebp(String path, float scale) {
        // TODO WEBP加载
        return null;
    }

}
