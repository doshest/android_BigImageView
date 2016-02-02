
package com.dosh.bigimage.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.io.FileDescriptor;
import java.io.InputStream;

/**
 * ͨ������, ʹ��BitmapRegionDecoder, ������API 10�ṩ
 * 
 * @author doshest
 */
public class BitmapRegionDecoderUtils {
    private static Reflection mReflection = new Reflection();

    private static final String CLASS_NAME = "android.graphics.BitmapRegionDecoder";

    public static boolean isSupported() {
        try {
            return Class.forName(CLASS_NAME) != null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * ͨ���������BitmapRegionDecoder#newInstance
     * 
     * @param pathName
     * @param isShareable
     * @return
     */
    public static Object newInstance(String pathName, boolean isShareable) {
        final String METHOD_NAME = "newInstance";
        return mReflection.invokeStaticMethod(CLASS_NAME, METHOD_NAME, new Class<?>[] {
                String.class, boolean.class
        }, new Object[] {
                pathName, isShareable
        });
    }

    /**
     * ͨ���������BitmapRegionDecoder#newInstance
     * 
     * @param is
     * @param isShareable
     * @return
     */
    public static Object newInstance(InputStream is, boolean isShareable) {
        final String METHOD_NAME = "newInstance";
        return mReflection.invokeStaticMethod(CLASS_NAME, METHOD_NAME, new Class<?>[] {
                InputStream.class, boolean.class
        }, new Object[] {
                is, isShareable
        });
    }

    /**
     * ͨ���������BitmapRegionDecoder#newInstance
     * 
     * @param fd
     * @param isShareable
     * @return
     */
    public static Object newInstance(FileDescriptor fd, boolean isShareable) {
        final String METHOD_NAME = "newInstance";
        return mReflection.invokeStaticMethod(CLASS_NAME, METHOD_NAME, new Class<?>[] {
                FileDescriptor.class, boolean.class
        }, new Object[] {
                fd, isShareable
        });
    }

    /**
     * ͨ���������BitmapRegionDecoder#newInstance
     * 
     * @param data
     * @param offset
     * @param length
     * @param isShareable
     * @return
     */
    public static Object newInstance(byte[] data, int offset, int length, boolean isShareable) {
        final String METHOD_NAME = "newInstance";
        return mReflection.invokeStaticMethod(CLASS_NAME, METHOD_NAME, new Class<?>[] {
                byte[].class, int.class, int.class, boolean.class
        }, new Object[] {
                data, offset, length, isShareable
        });
    }

    /**
     * ͨ���������BitmapRegionDecoder#getWidth
     * 
     * @param bitmapRegionDecoder
     * @return
     */
    public static int getWidth(Object bitmapRegionDecoder) {
        try {
            return (Integer)mReflection.invokeMethod(bitmapRegionDecoder, "getWidth", new Object[] {});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * ͨ���������BitmapRegionDecoder#getHeight
     * 
     * @param bitmapRegionDecoder
     * @return
     */
    public static int getHeight(Object bitmapRegionDecoder) {
        try {
            return (Integer)mReflection.invokeMethod(bitmapRegionDecoder, "getHeight", new Object[] {});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * ͨ���������BitmapRegionDecoder#decodeRegion
     * 
     * @param bitmapRegionDecoder
     * @param rect
     * @param options
     * @return
     */
    public static Bitmap decodeRegion(Object bitmapRegionDecoder, Rect rect, BitmapFactory.Options options) {
        return (Bitmap)mReflection.invokeMethod(bitmapRegionDecoder, "decodeRegion", new Class<?>[] {
                Rect.class, BitmapFactory.Options.class
        }, new Object[] {
                rect, options
        });
    }

    /**
     * ͨ���������BitmapRegionDecoder#isRecycled
     * 
     * @param bitmapRegionDecoder
     * @return
     */
    public static boolean isRecycled(Object bitmapRegionDecoder) {
        try {
            return (Boolean)mReflection.invokeMethod(bitmapRegionDecoder, "isRecycled", new Object[] {});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * ͨ���������BitmapRegionDecoder#recycle
     * 
     * @param bitmapRegionDecoder
     */
    public static void recycle(Object bitmapRegionDecoder) {
        try {
            mReflection.invokeMethod(bitmapRegionDecoder, "recycle", new Object[] {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
