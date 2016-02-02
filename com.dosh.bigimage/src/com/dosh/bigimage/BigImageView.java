
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
 * ����ҳ��ͼview��֧�ַֿ������ʾ�� ������Ҫ�ֿ��������Բ�����Ĭ�ϼ����߼���Ĭ���߼��������ͼȫ�����ص��ڴ�
 * 
 * @author nieyu2
 */
@SuppressLint("NewApi")
public class BigImageView extends ImageView {
    /**
     * ���ֿ����󳤶�
     */
    private final static int maxHeight = 2048;

    /**
     * openGL��ֱ�ӻ��Ƶ����ߴ�
     */
    private final static int MAX_HEIGHT_HARDWAREACCELERATED = 4096;

    /**
     * ����ҳͼƬ�����߼�����
     */
    private static float sScale = 0;

    /**
     * view������ֵ
     */
    private static int DETAIL_MAX_SRC_PIC_SIZE = 0;

    private static int DETAIL_MAX_FORWARD_PIC_SIZE = 0;

    /**
     * ���ص��ļ�·��
     */
    private String file = "";

    /**
     * ʱ��ֿ����
     */
    private boolean isChiped = false;

    private ChipBmps chips;

    /**
     * ��ʾ������ͼ���߲��ֿ��ͼ����ҪΪ�˺�Ĭ��ͼ��ʧ��ͼ����
     */
    private Bitmap bitmap;

    /**
     * �Ƿ�֧�ַֿ����
     */
    private static boolean isSupportRegionDecoder = true;

    static {
        try {
            // ���ҵ�BitmapRegionDecoder�����˵��֧�ַֿ����
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
            // ��ȡ��ǰ��ʾ���������ڷֿ����
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

    // Ԥ����ʾ��ͼƬ�ߴ�
    private int imageWidth;

    private int imageHeight;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        paint = new Paint();
        paint.setDither(true);
        paint.setFilterBitmap(true);// �����
        getViewTreeObserver().addOnPreDrawListener(drawListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        // �����Ҫremove��������ڴ�й©
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
        // ���û�зֿ���Ĭ���߼�
        if (chips == null || chips.orgBmpSize == null || getDrawable() == null) {
            return;
        }
        // ����ֿ���أ�������ͼ���ŵ�Ҫ��ʾ�Ĵ�С
        int viewW = MeasureSpec.getSize(widthMeasureSpec), viewH = MeasureSpec.getSize(heightMeasureSpec);

        int imageW = getDrawable().getIntrinsicWidth();
        int imageH = getDrawable().getIntrinsicHeight();
        float sc = ((float)viewW) / imageW;
        float scH = ((float)chips.bmpSize.height()) / imageH;
        Matrix matrix = new Matrix();
        matrix.setScale(sc, scH);
        setImageMatrix(matrix);
        if (chips != null) {
            // ����ͼƬ�����ű���
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
        // ����ֿ�����ˣ����Ƶ�ǰ��������µ�����
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
     * ����ͼƬ
     * 
     * @param url
     * @param downloadState
     */
    @SuppressLint("NewApi")
    public void setImageUrl(Picture url, IDownloadState downloadState) {
        // ����view���ߴ�
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
        // ��cache��ȡͼ
        boolean isOptionalImage = true;// ����������ж��ǲ���Ҫ��ʾ��ͼ
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
        // ������ڼ��ػ��߼��سɹ��������¼���
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
        // ����ͼƬ
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
     * ����ָ��ͼƬ�ļ�
     * 
     * @param string
     * @param isOptionalImage �Ƿ��Ż���ʾ�����Ƿ���ʾ��ͼ
     * @param type ͼƬ�ߴ�����
     * @param pictype ͼƬ�ļ�����
     * @return
     */
    private Object loadImageFromPath(String string, String url, boolean isOptionalImage, int type, PicType pictype) {
        if (TextUtils.isEmpty(string)) {
            return null;
        }
        boolean needLoadNet = false;
        if (isOptionalImage && (type != Picture.LARGE && type != Picture.ORIGINAL)) {
            needLoadNet = true;// ��Ŀ��ͼƬ����Ҫ���´��������
        }
        file = string;
        int optionSize = 1;
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Rect imageRect = new Rect();
        // ��ȡ����ͼƬ�Ĵ�С
        BitmapFactory.decodeFile(string, options);
        imageRect.left = imageRect.top = 0;
        imageRect.right = options.outWidth;
        imageRect.bottom = options.outHeight;
        options.inJustDecodeBounds = false;
        options.inSampleSize = 1;
        if (isSupportRegionDecoder && pictype != PicType.WEBP) {// ֧�ַֿ����
            if (options.outHeight > maxHeight || options.outWidth > maxHeight) { // �������ߴ�ŷֿ����
                isChiped = true;
                while (options.outHeight >= maxHeight) {// ����ֿ�����õ�����ͼ�Ĵ�С
                    optionSize *= 2;
                    options.outHeight /= 2;
                }
                options.inSampleSize = optionSize;
            }
            // ��ǰ��view�Ƿ���Ӳ�����ٵģ�Ӳ�����ٲ��ܻ��Ƴ���4096��ͼ�����Ӳ�������˶��Ҳ��ֿܷ���ؾͰ�ͼ���ŵ�4096һ��
        } else if (Utils.isHardwareAccelerated(this)) {
            if (options.outHeight > MAX_HEIGHT_HARDWAREACCELERATED || options.outWidth > MAX_HEIGHT_HARDWAREACCELERATED) {
                while (options.outHeight >= MAX_HEIGHT_HARDWAREACCELERATED) {
                    optionSize *= 2;
                    options.outHeight /= 2;
                }
                options.inSampleSize = optionSize;
            }
        }
        // ��ȫ��������ͼ������ͨͼ
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
            // ����ֿ鴴���ֿ������
            Object bitmapRegionDecoder = BitmapRegionDecoderUtils.newInstance(string, true);
            if (bitmapRegionDecoder == null) {
                isSupportRegionDecoder = false;
                isChiped = false;
                BmpCache.getInstance().save(url, bitmap);
                return new Pair<Bitmap, Boolean>(bitmap, needLoadNet);
            } else {
                // �����ֿ������
                chips = new ChipBmps(file, bitmapRegionDecoder, imageRect, bitmap);
                chips.setScale(1f);
                return chips;
            }
        } else {
            // ���ֿ鷵�ؼ��ص�ͼ���Ƿ���Ҫ������ص�״̬
            BmpCache.getInstance().save(url, bitmap);
            return new Pair<Bitmap, Boolean>(bitmap, needLoadNet);
        }
    }

    /**
     * ����״̬�仯����listviewһ��
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
     * �ֿ�ͼƬ�������������Ѿ����ص�ͼƬ�飬�������ӿ�viewport�仯������ͼƬ�顣
     * 
     * @author NIEYU2
     */
    private class ChipBmps {
        /**
         * �����Ѿ�����ͼƬ�飬͵���ķ�ʽ���ж���������ݾͶ೤���󲿷��ǿյģ�������󳤶�30���ң���ռ�����ڴ档
         */
        private Bitmap[] chips;

        /**
         * ��������ͼ������û�к���ͼƬ��ʱ��������ʾ
         */
        private Bitmap thumbnail;

        /**
         * ԭʼͼƬ��С
         */
        private Rect orgBmpSize;

        /**
         * ��ʾ��view�еĴ�С��view���ܺ�ͼƬ��С��һ��
         */
        private Rect bmpSize;

        /**
         * ���ݵ�ǰ�ӿ�viewportƥ���ͼƬ��������������ͼƬ����viewport�ϵ�offset
         */
        private int currentPoint = 0;

        private int offset;

        /**
         * �ϴμ��ص�ƥ��ͼƬ������
         */
        private int preCurrentPoint = -1;

        /**
         * ͼƬ�ֿ�����������䷽ʽ��ȡ
         */
        private Object decoder;

        /**
         * ���ű�������orgBmpSize��bmpSize�Ĵ�С����
         */
        private float scale;

        /**
         * ��Ļ�߶�
         */
        private int screenHeight;

        /**
         * �Ƿ���Ҫ����ͼƬ�飬�ڿ��ٻ����ǲ����ء�
         */
        private boolean isLoad = true;

        /**
         * @param file ͼƬ�ļ�
         * @param bitmapRegionDecoder �ֿ������
         * @param bmpSize ͼ��ԭʼ��С
         * @param thumbnail ����ͼ
         */
        public ChipBmps(String file, Object bitmapRegionDecoder, Rect bmpSize, Bitmap thumbnail) {
            decoder = bitmapRegionDecoder;
            orgBmpSize = bmpSize;
            this.bmpSize = orgBmpSize;
            this.thumbnail = thumbnail;
        }

        /**
         * ���õ�ǰ�Ƿ���Ҫ����ͼƬ��
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
         * �������ű�������orgBmpSize��bmpSize�Ĵ�С����
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
         * ��ȡ���ݵ�ǰ�ӿ�viewportƥ�����һ��ͼƬ�飬����Ϊ��
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
         * ��ȡ���ݵ�ǰ�ӿ�viewportƥ�����һ��ͼƬ�飬����Ϊ��
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
         * ��ȡ���ݵ�ǰ�ӿ�viewportƥ���ͼƬ�飬����Ϊ��
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
         * �ӿڱ仯֪ͨ������ǰ��ʾ�ľ����������仯
         * 
         * @param rect ��ǰ��ʾ�ľ�������
         */
        public void viewPortChange(Rect rect) {
            if (rect.top < 0) {
                return;
            }
            int index = 0;
            // ����������ʾ�ľ������������һ����ƥ�䵽��ͼƬ������
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
                // �ݴ���
                currentPoint = 0;
                offset = 0;
            }
            // ����ͼƬ��
            releaseAndLoad();

        }

        private void releaseAndLoad() {
            if (chips == null || !isLoad) {
                return;
            }
            // ��ǰ�ĺ���һ�μ��صĿ�һ�£������¼���
            if (currentPoint == preCurrentPoint && chips[currentPoint] != null) {
                return;
            }
            // �ͷ���һ�μ��ؿ�
            for (int i = 0; i < chips.length; i++) {
                if (i >= currentPoint - 1 && i <= currentPoint + 1) {
                    continue;
                }
                chips[i] = null;
            }
            Rect r = new Rect();
            int matchScreen = Math.round(screenHeight * scale); // ����Ļ�߶ȸ������ű���ӳ�䵱ͼƬ��
            r.top = currentPoint * matchScreen;// ����������λ���ص�ͼƬ��λ��
            r.bottom = r.top + matchScreen;
            r.left = 0;
            r.right = orgBmpSize.width();
            // ���ص�ǰ��
            chips[currentPoint] = BitmapRegionDecoderUtils.decodeRegion(decoder, r, null);
            // ������һ��
            if (currentPoint != 0) {
                r.top = r.top - matchScreen;
                r.bottom = r.bottom - matchScreen;
                chips[currentPoint - 1] = BitmapRegionDecoderUtils.decodeRegion(decoder, r, null);
                r.top = r.top + matchScreen;
                r.bottom = r.bottom + matchScreen;
            }
            // ������һ��
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
            // Ĭ�ϼ���ͼ��ʧ������WRAP_CONTENT
            params.width = LayoutParams.WRAP_CONTENT;
            params.height = LayoutParams.WRAP_CONTENT;
            setLayoutParams(params);
            return;
        }
        // ��ȡͼƬ�ߴ磬����ֿ���ʾ��ȡȫͼ��С
        int bmpWidth = chips == null ? getDrawable().getIntrinsicWidth() : chips.orgBmpSize.width();
        int bmpHeight = chips == null ? getDrawable().getIntrinsicHeight() : chips.orgBmpSize.height();
        // �������ʾ���
        int showWidth = isRetweetedBlog() ? DETAIL_MAX_FORWARD_PIC_SIZE : DETAIL_MAX_SRC_PIC_SIZE;
        // �Ƿ��Ż���ʾ
        boolean isOptimal = true; // Utils.isOptionalImage(getContext());
        // �Ż�������ʾ������ͼ��������Ϣ��������û�����ش�ͼ�����
        if (isOptimal && pic.getUrlType() == Picture.BMIDDLE) {
            // ͼ��������û�з���ͼƬ�ߴ磬��ͼ��2��ʾ��
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
                // ����ͼƬ�ߴ簴�ճߴ���ʾ
                int largePicWidth = pic.getPicInfo().getLargeWidth();
                int largePicHeight = pic.getPicInfo().getLargeHeight();
                if (largePicHeight * pic.getPicInfo().getBmiddleWidth() > largePicWidth
                        * pic.getPicInfo().getBmiddleHeight()) { // ��ͼ
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
            // ���Ż���ʾ���������getScaleSize������ʾ
            if (getScaleSize(bmpWidth) > showWidth) {
                params.width = showWidth;
                params.height = bmpHeight * showWidth / bmpWidth;
            } else {
                params.width = getScaleSize(bmpWidth);
                params.height = getScaleSize(bmpHeight);
            }
        }

        setLayoutParams(params);
        // ��ͼ����ͼ���Ⱥ�ԭͼ��һ������Ҫ֪ͨ�ⲿ�ı��С������������
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
     * ��֪ͼƬԤ����ʾ�Ĵ�С���ڷ���������ͼƬ�ߴ������ʹ��
     * 
     * @param width
     * @param height
     */
    public void setExpectPicSize(int width, int height) {
        imageWidth = width;
        imageHeight = height;

    }

}
