
package com.dosh.bigimage.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * ͼƬ���棬���������ú�lru�㷨
 */
public class BmpCache {

    private BmpCache() {
        long size = Runtime.getRuntime().totalMemory() / 3;// ��������ʱ������ڴ��С������֮һ����ܴ�
        if (size < maxCacheSize) {
            maxCacheSize = size;
        }
    }

    private ReferenceQueue<Bitmap> queue = new ReferenceQueue<Bitmap>();

    private long maxCacheSize = 5 * 1024 * 1024;// ���ֵ

    private long cachesize = 0;

    private static BmpCache mInstance = null;

    private ConcurrentLRUHashMap<String, CacheEntry> mBmpCacheMap = new ConcurrentLRUHashMap<String, BmpCache.CacheEntry>();

    private boolean mActive = true;

    public static BmpCache getInstance() {
        if (mInstance == null) {
            synchronized (BmpCache.class) {
                if (mInstance == null) {
                    mInstance = new BmpCache();
                }
            }
        }
        return mInstance;
    }

    /**
     * ���浽cache
     * 
     * @param url
     * @param bm
     */
    public void save(String url, Bitmap bm) {
        if (mActive == false) {
            return;
        }

        CacheEntry e;
        while ((e = (CacheEntry)queue.poll()) != null) {
            mBmpCacheMap.remove(e.url);
            cachesize -= e.size;
        }

        if (cachesize > maxCacheSize) {
            trimToSize(maxCacheSize - 5000);
        }

        if (bm == null || bm.isRecycled() || url == null || "".equals(url.trim())) {
            return;
        }
        e = new CacheEntry(bm, queue, url);
        if (!mBmpCacheMap.containsKey(url)) {
            cachesize += e.size;
        }
        mBmpCacheMap.put(url, e);

    }

    /**
     * ��cache��С������ָ��ֵ
     * 
     * @param maxSize
     */
    private void trimToSize(long maxSize) {
        while (true) {
            String key;
            CacheEntry value;

            if ((this.cachesize < maxSize) || (this.mBmpCacheMap.isEmpty())) {
                break;
            }
            Map.Entry<String, CacheEntry> toEvict = this.mBmpCacheMap.entrySet().iterator().next();
            key = toEvict.getKey();
            value = toEvict.getValue();
            this.mBmpCacheMap.remove(key);
            this.cachesize -= value.size;

            value.clear();

        }
    }

    private static boolean isApkDebugable(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {

        }
        return false;
    }

    /**
     * �ӻ����л�ȡͼƬ������û��ָ��ͼƬ���ؿ�
     * 
     * @param url
     * @return
     */
    public Bitmap get(String url) {
        if (mActive == false) {
            return null;
        }
        if (url != null && !url.trim().equals("")) {
            CacheEntry cacheEntry = mBmpCacheMap.get(url);
            if (cacheEntry != null) {
                Bitmap bmp = cacheEntry.get();
                if (bmp == null) {
                    mBmpCacheMap.remove(url);
                    cachesize -= cacheEntry.size;
                }
                return bmp;
            }
        }
        return null;
    }

    /**
     * cacheʵ���ࡣ
     * 
     * @author nieyu2
     */
    private static class CacheEntry extends SoftReference<Bitmap> {
        /**
         * ͼƬ�����С����λΪbyte
         */
        public int size;

        private String url;

        private CacheEntry(Bitmap bmp, ReferenceQueue<Bitmap> queue, String url) {
            super(bmp, queue);
            this.url = url;
            int pixSize = 0;
            if (bmp.getConfig() == Config.RGB_565) {
                pixSize = 2;
            } else if (bmp.getConfig() == Config.ARGB_4444) {
                pixSize = 2;
            }
            if (bmp.getConfig() == Config.ARGB_8888) {
                pixSize = 4;
            }
            size = (pixSize * bmp.getWidth() * bmp.getHeight());
        }
    }

    /**
     * ��ջ���
     */
    public void clear() {
        clear(mBmpCacheMap);
    }

    private void clear(Map<String, CacheEntry> map) {
        Iterator<Entry<String, CacheEntry>> itr = map.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<String, CacheEntry> e = itr.next();
            SoftReference<Bitmap> sr = e.getValue();
            if (sr != null) {
                Bitmap bmp = sr.get();
                if (bmp != null && !bmp.isRecycled()) {
                    bmp.recycle();
                }
            }
        }
        map.clear();
        cachesize = 0;
    }
}
