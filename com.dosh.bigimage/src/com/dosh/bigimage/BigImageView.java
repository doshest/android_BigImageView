
package com.dosh.bigimage;

import com.dosh.bigimage.model.PicInfo;
import com.dosh.bigimage.model.PicInfo.PicType;
import com.dosh.bigimage.model.Picture;
import com.dosh.bigimage.utils.BitmapRegionDecoderUtils;
import com.dosh.bigimage.utils.BmpCache;
import com.dosh.bigimage.utils.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;

import java.io.File;

/**
 * 正文页大图view，支持分块加载显示。 由于需要分开加载所以不能用默认加载逻辑，默认逻辑会把整个图全部加载到内存
 * 
 * @author nieyu2
 */
@SuppressLint("NewApi")
public class BigImageView extends ImageView {
    /**
     * 不分块的最大长度
     */
    private final static int maxHeight = 2048;

    /**
     * openGL能直接绘制的最大尺寸
     */
    private final static int MAX_HEIGHT_HARDWAREACCELERATED = 4096;

    /**
     * 正文页图片缩放逻辑比例
     */
    private static float sScale = 0;

    /**
     * view宽度最大值
     */
    private static int DETAIL_MAX_SRC_PIC_SIZE = 0;

    private static int DETAIL_MAX_FORWARD_PIC_SIZE = 0;

    /**
     * 加载的文件路径
     */
    private String file = "";

    /**
     * 时候分块加载
     */
    private boolean isChiped = false;

    private ChipBmps chips;

    /**
     * 显示的缩略图或者不分块的图，主要为了和默认图和失败图区分
     */
    private Bitmap bitmap;

    /**
     * 是否支持分块加载
     */
    private static boolean isSupportRegionDecoder = true;

    static {
        try {
            // 能找到BitmapRegionDecoder这个类说明支持分块加载
            ClassLoader.getSystemClassLoader().loadClass("android.graphics.BitmapRegionDecoder");
            isSupportRegionDecoder = true;
        } catch (ClassNotFoundException e) {
            isSupportRegionDecoder = false;
        }
    }

    private OnPreDrawListener drawListener = new OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            Rect r = new Rect();
            // 获取当前显示的区域，用于分块加载
            getLocalVisibleRect(r);
            r.offset(0, getTop());
            if (chips != null) {
                chips.viewPortChange(r);
            }
            return true;
        }
    };

    private Picture imageInfo;

    private static final int STATE_IDLE = 0;

    private static final int STATE_LOADING = 1;

    private static final int STATE_SUCCESS = 2;

    private static final int STATE_FAILD = 3;

    private static final int STATE_FAILD_HASIMAGE = 4;

    private int state = 0;

    private boolean isRetweetedBlog;

    private Paint paint;

    // 预期显示的图片尺寸
    private int imageWidth;

    private int imageHeight;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        paint = new Paint();
        paint.setDither(true);
        paint.setFilterBitmap(true);// 反锯齿
        getViewTreeObserver().addOnPreDrawListener(drawListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        // 这个需要remove，否则会内存泄漏
        getViewTreeObserver().removeOnPreDrawListener(drawListener);
        if (chips != null) {
            chips.recycle();
        }
        super.onDetachedFromWindow();
    }

    public BigImageView(Context context) {
        super(context);
    }

    public BigImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BigImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 如果没有分块走默认逻辑
        if (chips == null || chips.orgBmpSize == null || getDrawable() == null) {
            return;
        }
        // 如果分块加载，将缩略图缩放到要显示的大小
        int viewW = MeasureSpec.getSize(widthMeasureSpec), viewH = MeasureSpec.getSize(heightMeasureSpec);

        int imageW = getDrawable().getIntrinsicWidth();
        int imageH = getDrawable().getIntrinsicHeight();
        float sc = ((float)viewW) / imageW;
        float scH = ((float)chips.bmpSize.height()) / imageH;
        Matrix matrix = new Matrix();
        matrix.setScale(sc, scH);
        setImageMatrix(matrix);
        if (chips != null) {
            // 设置图片块缩放比例
            chips.setScale(chips.orgBmpSize.width() / (float)viewW);
            setMeasuredDimension(viewW, chips.bmpSize.height());
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        bitmap = bm;
        super.setImageBitmap(bm);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isChiped || chips == null) {
            return;
        }
        // 如果分块加载了，绘制当前区域和上下的区域
        Matrix matrix = new Matrix();
        float sc = 1 / chips.scale;
        matrix.setScale(sc, sc);
        if (null != chips && chips.getCurrentBmp() != null) {
            canvas.translate(0, chips.offset);
            canvas.drawBitmap(chips.getCurrentBmp(), matrix, paint);
            canvas.translate(0, -chips.offset);
        }
        if (null != chips && chips.getPreBmp() != null) {
            canvas.translate(0, chips.offset - chips.screenHeight);
            canvas.drawBitmap(chips.getPreBmp(), matrix, paint);
            canvas.translate(0, -chips.offset + chips.screenHeight);
        }
        if (null != chips && chips.getNextBmp() != null) {
            canvas.translate(0, chips.offset + chips.screenHeight);
            canvas.drawBitmap(chips.getNextBmp(), matrix, paint);
            canvas.translate(0, -chips.offset - chips.screenHeight);
        }
    }

    /**
     * 加载图片
     * 
     * @param url
     * @param downloadState
     */
    @SuppressLint("NewApi")
    public void setImageUrl(Picture url, IDownloadState downloadState) {
        // 计算view最大尺寸
        if (sScale == 0) {
            sScale = ((float)getResources().getDisplayMetrics().densityDpi) / DisplayMetrics.DENSITY_MEDIUM;
        }
        if (DETAIL_MAX_SRC_PIC_SIZE == 0 || DETAIL_MAX_FORWARD_PIC_SIZE == 0) {
            DisplayMetrics dm = new DisplayMetrics();
            ((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
            int maxSize = dm.widthPixels < dm.heightPixels ? dm.widthPixels : dm.heightPixels;
            NinePatchDrawable drawable = (NinePatchDrawable)(getContext().getDrawable(R.drawable.popup));
            Rect padding = new Rect();
            drawable.getPadding(padding);
            DETAIL_MAX_SRC_PIC_SIZE = maxSize - 2
                    * getContext().getResources().getDimensionPixelSize(R.dimen.timeline_padding_left);
            DETAIL_MAX_FORWARD_PIC_SIZE = DETAIL_MAX_SRC_PIC_SIZE - padding.left - padding.right;
        }
        // 从cache中取图
        boolean isOptionalImage = true;// 根据情况来判定是不是要显示大图
        Object cache;
        boolean needLoadNet = true;
        if ((cache = loadImage(new Pair<Picture, IDownloadState>(url, downloadState), true)) != null) {
            if (cache instanceof Pair) {
                Pair<Bitmap, Boolean> pair = (Pair<Bitmap, Boolean>)cache;
                setScaleType(ScaleType.FIT_CENTER);
                setImageBitmap(pair.first);
                needLoadNet = pair.second && (!isOptionalImage);
                if (!pair.second) {
                    imageInfo = url;
                    state = STATE_SUCCESS;
                }
            }
        }
        // 如果正在加载或者加载成功，不重新加载
        if (imageInfo != null && imageInfo.getPicInfo().getLargeUrl().equals(url.getPicInfo().getLargeUrl())
                && (state == STATE_SUCCESS || state == STATE_LOADING)) {
            if (state == STATE_SUCCESS) {
                downloadState.onComplete(file);
            }
            updateLayout(imageInfo);
            return;
        }
        imageInfo = url;
        state = STATE_LOADING;
        // 加载图片
        if (cache != null && !needLoadNet) {
            LoadFromNetTask task = new LoadFromNetTask();
            // task.setmParams();
            task.execute(new Pair[] {
                new Pair<Picture, IDownloadState>(url, downloadState)
            });

        } else {
            LoadTask task = new LoadTask();
            task.execute(new Pair[] {
                new Pair<Picture, IDownloadState>(url, downloadState)
            });
        }
    }

    /**
     * 加载指定图片文件
     * 
     * @param string
     * @param isOptionalImage 是否优化显示，即是否显示大图
     * @param type 图片尺寸类型
     * @param pictype 图片文件类型
     * @return
     */
    private Object loadImageFromPath(String string, String url, boolean isOptionalImage, int type, PicType pictype) {
        if (TextUtils.isEmpty(string)) {
            return null;
        }
        boolean needLoadNet = false;
        if (isOptionalImage && (type != Picture.LARGE && type != Picture.ORIGINAL)) {
            needLoadNet = true;// 非目标图片，需要重新从网络加载
        }
        file = string;
        int optionSize = 1;
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Rect imageRect = new Rect();
        // 获取加载图片的大小
        BitmapFactory.decodeFile(string, options);
        imageRect.left = imageRect.top = 0;
        imageRect.right = options.outWidth;
        imageRect.bottom = options.outHeight;
        options.inJustDecodeBounds = false;
        options.inSampleSize = 1;
        if (isSupportRegionDecoder && pictype != PicType.WEBP) {// 支持分块加载
            if (options.outHeight > maxHeight || options.outWidth > maxHeight) { // 超过最大尺寸才分块加载
                isChiped = true;
                while (options.outHeight >= maxHeight) {// 算出分块加载用到缩略图的大小
                    optionSize *= 2;
                    options.outHeight /= 2;
                }
                options.inSampleSize = optionSize;
            }
            // 当前的view是否是硬件加速的，硬件加速不能绘制超过4096的图，如果硬件加载了而且不能分块加载就把图缩放到4096一下
        } else if (Utils.isHardwareAccelerated(this)) {
            if (options.outHeight > MAX_HEIGHT_HARDWAREACCELERATED || options.outWidth > MAX_HEIGHT_HARDWAREACCELERATED) {
                while (options.outHeight >= MAX_HEIGHT_HARDWAREACCELERATED) {
                    optionSize *= 2;
                    options.outHeight /= 2;
                }
                options.inSampleSize = optionSize;
            }
        }
        // 安全加载缩略图或者普通图
        while (bitmap == null) {
            try {
                if (pictype == PicType.WEBP) {
                    bitmap = Utils.createBitmap(string, 1 / options.inSampleSize);
                } else {
                    bitmap = BitmapFactory.decodeFile(string, options);
                }

            } catch (OutOfMemoryError oom) {
                bitmap = null;
                System.gc();
                optionSize *= 2;
                options.inSampleSize = optionSize;
            }
        }

        if (isChiped) {
            // 如果分块创建分块加载器
            Object bitmapRegionDecoder = BitmapRegionDecoderUtils.newInstance(string, true);
            if (bitmapRegionDecoder == null) {
                isSupportRegionDecoder = false;
                isChiped = false;
                BmpCache.getInstance().save(url, bitmap);
                return new Pair<Bitmap, Boolean>(bitmap, needLoadNet);
            } else {
                // 创建分块管理器
                chips = new ChipBmps(file, bitmapRegionDecoder, imageRect, bitmap);
                chips.setScale(1f);
                return chips;
            }
        } else {
            // 不分块返回加载的图和是否需要网络加载的状态
            BmpCache.getInstance().save(url, bitmap);
            return new Pair<Bitmap, Boolean>(bitmap, needLoadNet);
        }
    }

    /**
     * 滚动状态变化，与listview一致
     * 
     * @param state
     */
    public void onScrollStateChange(int state) {

        if (chips == null) {

            return;
        }
        switch (state) {
            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
            case OnScrollListener.SCROLL_STATE_IDLE:
                chips.setIsLoad(true);
                break;
            case OnScrollListener.SCROLL_STATE_FLING:
                chips.setIsLoad(false);
                break;
            default:
                break;
        }
    }

    /**
     * 分块图片管理器，管理已经加载的图片块，并根据视口viewport变化，更新图片块。
     * 
     * @author NIEYU2
     */
    private class ChipBmps {
        /**
         * 保存已经加载图片块，偷懒的方式，有多少这个数据就多长。大部分是空的，而且最大长度30左右，不占多少内存。
         */
        private Bitmap[] chips;

        /**
         * 加载缩略图，用于没有合适图片块时做背景显示
         */
        private Bitmap thumbnail;

        /**
         * 原始图片大小
         */
        private Rect orgBmpSize;

        /**
         * 显示到view中的大小。view可能和图片大小不一样
         */
        private Rect bmpSize;

        /**
         * 根据当前视口viewport匹配的图片块的索引，和这个图片块在viewport上的offset
         */
        private int currentPoint = 0;

        private int offset;

        /**
         * 上次加载的匹配图片块索引
         */
        private int preCurrentPoint = -1;

        /**
         * 图片分块加载器，反射方式获取
         */
        private Object decoder;

        /**
         * 缩放比例，即orgBmpSize和bmpSize的大小比例
         */
        private float scale;

        /**
         * 屏幕高度
         */
        private int screenHeight;

        /**
         * 是否需要加载图片块，在快速滑动是不加载。
         */
        private boolean isLoad = true;

        /**
         * @param file 图片文件
         * @param bitmapRegionDecoder 分块加载器
         * @param bmpSize 图排原始大小
         * @param thumbnail 缩略图
         */
        public ChipBmps(String file, Object bitmapRegionDecoder, Rect bmpSize, Bitmap thumbnail) {
            decoder = bitmapRegionDecoder;
            orgBmpSize = bmpSize;
            this.bmpSize = orgBmpSize;
            this.thumbnail = thumbnail;
        }

        /**
         * 设置当前是否需要加载图片块
         * 
         * @param b
         */
        public void setIsLoad(boolean b) {
            isLoad = b;
            if (isLoad) {
                releaseAndLoad();
            }
        }

        /**
         * 设置缩放比例，即orgBmpSize和bmpSize的大小比例
         * 
         * @param sc
         */
        public void setScale(float sc) {
            scale = sc;
            bmpSize = new Rect();
            bmpSize.top = 0;
            bmpSize.left = 0;
            bmpSize.right = (int)(orgBmpSize.right / scale);
            bmpSize.bottom = (int)(orgBmpSize.bottom / scale);
            Rect outRect = new Rect();
            getWindowVisibleDisplayFrame(outRect);
            screenHeight = outRect.height();

            int chipCount = bmpSize.height() / screenHeight;
            int mod = bmpSize.height() % screenHeight;
            if (mod > 0) {
                chipCount++;
            }
            if (chipCount < 0) {
                chipCount = 0;
            }
            chips = new Bitmap[chipCount];
        }

        /**
         * 获取根据当前视口viewport匹配的上一个图片块，可能为空
         * 
         * @return
         */
        public Bitmap getPreBmp() {
            if (chips == null) {
                return null;
            }
            if (currentPoint > 0 && (chips.length > currentPoint - 1)) {
                return chips[currentPoint - 1];
            }
            return null;
        }

        /**
         * 获取根据当前视口viewport匹配的下一个图片块，可能为空
         * 
         * @return
         */
        public Bitmap getNextBmp() {
            if (chips == null) {
                return null;
            }
            if (currentPoint < chips.length - 1) {
                return chips[currentPoint + 1];
            }
            return null;
        }

        /**
         * 获取根据当前视口viewport匹配的图片块，可能为空
         * 
         * @return
         */
        public Bitmap getCurrentBmp() {
            if (chips == null || currentPoint >= chips.length) {
                return null;
            }
            return chips[currentPoint];
        }

        public void recycle() {
            if (decoder != null) {
                BitmapRegionDecoderUtils.recycle(decoder);
            }
        }

        /**
         * 视口变化通知，即当前显示的矩形区域发生变化
         * 
         * @param rect 当前显示的矩形区域
         */
        public void viewPortChange(Rect rect) {
            if (rect.top < 0) {
                return;
            }
            int index = 0;
            // 根据正在显示的矩形区域算出这一区域匹配到的图片块索引
            int pointTop = 0, pointBottom = screenHeight;
            while (pointTop < rect.top && pointBottom < rect.top) {
                pointTop += screenHeight;
                pointBottom += screenHeight;
                index++;
            }
            int pointCenter = (pointTop + pointBottom) >> 1;
            if (pointCenter < rect.top && pointCenter + screenHeight < rect.bottom) {
                pointTop += screenHeight;
                pointBottom += screenHeight;
                index++;
            }

            currentPoint = index;
            offset = pointTop;
            if (chips != null && currentPoint > chips.length - 1) {
                // 容错处理
                currentPoint = 0;
                offset = 0;
            }
            // 加载图片块
            releaseAndLoad();

        }

        private void releaseAndLoad() {
            if (chips == null || !isLoad) {
                return;
            }
            // 当前的和上一次加载的块一致，不从新加载
            if (currentPoint == preCurrentPoint && chips[currentPoint] != null) {
                return;
            }
            // 释放上一次加载块
            for (int i = 0; i < chips.length; i++) {
                if (i >= currentPoint - 1 && i <= currentPoint + 1) {
                    continue;
                }
                chips[i] = null;
            }
            Rect r = new Rect();
            int matchScreen = Math.round(screenHeight * scale); // 将屏幕高度根据缩放比例映射当图片上
            r.top = currentPoint * matchScreen;// 根据索引定位加载的图片块位置
            r.bottom = r.top + matchScreen;
            r.left = 0;
            r.right = orgBmpSize.width();
            // 加载当前块
            chips[currentPoint] = BitmapRegionDecoderUtils.decodeRegion(decoder, r, null);
            // 加载上一块
            if (currentPoint != 0) {
                r.top = r.top - matchScreen;
                r.bottom = r.bottom - matchScreen;
                chips[currentPoint - 1] = BitmapRegionDecoderUtils.decodeRegion(decoder, r, null);
                r.top = r.top + matchScreen;
                r.bottom = r.bottom + matchScreen;
            }
            // 加载下一块
            if (currentPoint != chips.length - 1) {
                r.top = r.top + matchScreen;
                r.bottom = r.bottom + matchScreen;
                if (r.bottom > orgBmpSize.bottom) {
                    r.bottom = orgBmpSize.bottom;
                }
                chips[currentPoint + 1] = BitmapRegionDecoderUtils.decodeRegion(decoder, r, null);
            }

            preCurrentPoint = currentPoint;
            invalidate();
        }

    }

    private class LoadFromNetTask extends AsyncTask<Pair<Picture, IDownloadState>, Void, Object> {

        private Pair<Picture, IDownloadState> pair;

        @Override
        protected Object doInBackground(Pair<Picture, IDownloadState>... params) {
            pair = (params[0]);
            if (pair == null) {
                return null;
            }

            String file, url;
            int type;
            PicType pictype;
            boolean isOptionalImage = true;// Utils.isOptionalImage(getContext());
            if (isOptionalImage) {
                type = Picture.LARGE;
                pictype = pair.first.getPicInfo().getLargeType();
                file = pair.first.getPicInfo().getLargeFilePath(getContext(), false);
                url = pair.first.getPicInfo().getLargeUrl();
            } else {
                type = Picture.THUMBNAIL;
                pictype = pair.first.getPicInfo().getThumbnailType();
                file = pair.first.getPicInfo().getThumbnailFilePath(getContext(), false);
                url = pair.first.getPicInfo().getThumbnailUrl();
            }
            File imageFile = new File(file);
            if (!imageFile.exists()) {
                try {
                    // NetEngineFactory.getNetInstance(getContext()).getPicture(url,
                    // null, false, pair.second, file);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            pair.first.setUrlType(type);
            return loadImageFromPath(file, url, isOptionalImage, type, pictype);
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result == null) {
                if (bitmap == null) {
                    state = STATE_FAILD;
                    ViewGroup.LayoutParams params = getLayoutParams();
                    params.width = LayoutParams.WRAP_CONTENT;
                    params.height = LayoutParams.WRAP_CONTENT;
                    setLayoutParams(params);
                    setImageDrawable(getContext().getDrawable(R.drawable.timeline_image_failure));
                } else {
                    state = STATE_FAILD_HASIMAGE;
                }
                imageInfo = null;
                pair.second.onFail(file);
                return;
            }
            if (result instanceof Pair) {
                setScaleType(ScaleType.FIT_CENTER);
                Pair<Bitmap, Boolean> resultPair = (Pair<Bitmap, Boolean>)result;
                setImageBitmap(resultPair.first);

            } else {
                chips = (ChipBmps)result;
                setScaleType(ScaleType.MATRIX);
                setImageBitmap(chips.thumbnail);
            }

            imageInfo = null;
            state = STATE_SUCCESS;
            updateLayout(pair.first);
            requestLayout();
        }

    }

    private class LoadTask extends AsyncTask<Pair<Picture, IDownloadState>, Void, Object> {

        private Pair<Picture, IDownloadState> pair;

        @Override
        protected Object doInBackground(Pair<Picture, IDownloadState>... params) {
            pair = (params[0]);
            imageInfo = pair.first;
            final Object bmps = loadImage(pair, false);
            return bmps;
        }

        @Override
        protected void onPreExecute() {
            ViewGroup.LayoutParams params = getLayoutParams();
            params.width = LayoutParams.WRAP_CONTENT;
            params.height = LayoutParams.WRAP_CONTENT;
            setLayoutParams(params);
            setImageDrawable(getContext().getDrawable(R.drawable.timeline_image_loading));
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Object bmps) {
            LoadFromNetTask fromNetTask = null;
            if (bmps == null) {
                imageInfo = null;
                fromNetTask = new LoadFromNetTask();
                fromNetTask.execute(new Pair[] {
                    pair
                });
                return;
            }
            if (bmps instanceof Pair) {
                setScaleType(ScaleType.FIT_CENTER);
                Pair<Bitmap, Boolean> resultPair = (Pair<Bitmap, Boolean>)bmps;
                setImageBitmap(resultPair.first);
                if (resultPair.second) {
                    fromNetTask = new LoadFromNetTask();
                }
            } else {
                chips = (ChipBmps)bmps;
                setScaleType(ScaleType.MATRIX);
                setImageBitmap(chips.thumbnail);
            }
            Picture pic = null;
            if (fromNetTask != null) {
                pic = new Picture();
                pic.setPicInfo(pair.first.getPicInfo());
                pic.setUrlType(pair.first.getUrlType());

                fromNetTask.execute(new Pair[] {
                    pair
                });
            } else {
                state = STATE_SUCCESS;
                pair.second.onComplete(file);
            }
            imageInfo = pair.first;
            updateLayout(pic == null ? pair.first : pic);
            requestLayout();
        }

    }

    private int getScaleSize(int size) {
        return (int)(sScale * size);
    }

    private void updateLayout(Picture pic) {
        ViewGroup.LayoutParams params = getLayoutParams();
        if (bitmap == null) {
            // 默认加载图或失败如用WRAP_CONTENT
            params.width = LayoutParams.WRAP_CONTENT;
            params.height = LayoutParams.WRAP_CONTENT;
            setLayoutParams(params);
            return;
        }
        // 获取图片尺寸，如果分块显示获取全图大小
        int bmpWidth = chips == null ? getDrawable().getIntrinsicWidth() : chips.orgBmpSize.width();
        int bmpHeight = chips == null ? getDrawable().getIntrinsicHeight() : chips.orgBmpSize.height();
        // 做大的显示宽度
        int showWidth = isRetweetedBlog() ? DETAIL_MAX_FORWARD_PIC_SIZE : DETAIL_MAX_SRC_PIC_SIZE;
        // 是否优化显示
        boolean isOptimal = true; // Utils.isOptionalImage(getContext());
        // 优化而且显示的是中图，即从信息流来但是没有下载大图的情况
        if (isOptimal && pic.getUrlType() == Picture.BMIDDLE) {
            // 图过服务器没有返回图片尺寸，中图乘2显示。
            if (pic.getPicInfo().getLargeWidth() == PicInfo.INT_DEFAULT) {
                bmpWidth = bmpWidth * 2;
                bmpHeight = bmpHeight * 2;
                if (getScaleSize(bmpWidth) > showWidth) {
                    params.width = showWidth;
                    params.height = bmpHeight * showWidth / bmpWidth;
                } else {
                    params.width = getScaleSize(bmpWidth);
                    params.height = getScaleSize(bmpHeight);
                }
            } else {
                // 返回图片尺寸按照尺寸显示
                int largePicWidth = pic.getPicInfo().getLargeWidth();
                int largePicHeight = pic.getPicInfo().getLargeHeight();
                if (largePicHeight * pic.getPicInfo().getBmiddleWidth() > largePicWidth
                        * pic.getPicInfo().getBmiddleHeight()) { // 长图
                    if (getScaleSize(largePicWidth) > showWidth) {
                        params.width = showWidth;
                        params.height = bmpHeight * showWidth / bmpWidth;
                    } else {
                        params.width = getScaleSize(largePicWidth);
                        params.height = bmpHeight * getScaleSize(largePicWidth) / bmpWidth;
                    }
                } else {
                    params.width = bmpWidth * imageHeight / bmpHeight;
                    params.height = imageHeight;
                }
            }
        } else {
            // 不优化显示情况，按照getScaleSize比例显示
            if (getScaleSize(bmpWidth) > showWidth) {
                params.width = showWidth;
                params.height = bmpHeight * showWidth / bmpWidth;
            } else {
                params.width = getScaleSize(bmpWidth);
                params.height = getScaleSize(bmpHeight);
            }
        }

        setLayoutParams(params);
        // 长图的中图长度和原图不一样，需要通知外部改变大小。。。。。。
        // ViewGroup.LayoutParams paramsPic = getLayoutParams();
        // paramsPic.width = showWidth;
        // paramsPic.height = params.height;
        // ((ViewGroup)getParent().getParent()).setLayoutParams(paramsPic);

    }

    private boolean isRetweetedBlog() {
        return isRetweetedBlog;
    }

    public void setRetweetedBlog(boolean b) {
        isRetweetedBlog = b;
    }

    private Object loadImage(Pair<Picture, IDownloadState> pair, boolean fromCache) {
        if (pair == null) {
            return null;
        }

        String file, url;
        boolean isOptionalImage = true;// Utils.isOptionalImage(getContext());
        int type = Picture.LARGE;
        PicType pictype = pair.first.getPicInfo().getLargeType();
        file = pair.first.getPicInfo().getLargeFilePath(getContext(), false);
        url = pair.first.getPicInfo().getLargeUrl();
        File imageFile = new File(file);
        Bitmap bitmap = null;
        if (fromCache) {
            bitmap = BmpCache.getInstance().get(url);
            if (bitmap != null) {
                pair.first.setUrlType(type);
                return new Pair<Bitmap, Boolean>(bitmap, false);
            }
        } else if (imageFile.exists()) {
            pair.first.setUrlType(type);
            return loadImageFromPath(file, url, isOptionalImage, type, pictype);
        }
        type = Picture.BMIDDLE;
        pictype = pair.first.getPicInfo().getBmiddleType();
        file = pair.first.getPicInfo().getBmiddleFilePath(getContext(), false);
        url = pair.first.getPicInfo().getBmiddleUrl();
        imageFile = new File(file);
        if (fromCache) {
            bitmap = BmpCache.getInstance().get(url);
            if (bitmap != null) {
                pair.first.setUrlType(type);
                return new Pair<Bitmap, Boolean>(bitmap, isOptionalImage ? true : false);
            }
        } else if (imageFile.exists()) {
            pair.first.setUrlType(type);
            return loadImageFromPath(file, url, isOptionalImage, type, pictype);
        }
        type = Picture.THUMBNAIL;
        pictype = pair.first.getPicInfo().getThumbnailType();
        file = pair.first.getPicInfo().getThumbnailFilePath(getContext(), false);
        url = pair.first.getPicInfo().getThumbnailUrl();
        imageFile = new File(file);
        if (fromCache) {
            bitmap = BmpCache.getInstance().get(url);
            if (bitmap != null) {
                pair.first.setUrlType(type);
                return new Pair<Bitmap, Boolean>(bitmap, isOptionalImage ? true : false);
            } else {
                return null;
            }
        } else if (imageFile.exists()) {
            pair.first.setUrlType(type);
            return loadImageFromPath(file, url, isOptionalImage, type, pictype);
        }

        return null;

    }

    /**
     * 已知图片预期显示的大小，在服务器返回图片尺寸情况下使用
     * 
     * @param width
     * @param height
     */
    public void setExpectPicSize(int width, int height) {
        imageWidth = width;
        imageHeight = height;

    }

}
