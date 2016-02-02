
package com.dosh.bigimage.model;

import com.dosh.bigimage.utils.Utils;

import org.json.JSONObject;

import android.content.Context;

import java.io.Serializable;

public class PicInfo extends JsonDataObject implements Serializable {

    private static final long serialVersionUID = 1164752975093152985L;

    public static final int INT_DEFAULT = 0;

    public static final int WITHOUT_PHOTO_TAG = 0;

    public static final int WITH_PHOTO_TAG = 1;

    private PicInfoSize bmiddle;

    private PicInfoSize large;

    private PicInfoSize original;

    private PicInfoSize largest;// 原始图

    private PicInfoSize thumbnail;

    /**
     * 430 新增视频小page需求。
     */
    private PicInfoSize pic_small;

    private PicInfoSize pic_big;

    private PicInfoSize pic_middle;

    public enum PicType {
        /* LOCAL(1), */WEBP(2), OTHER(0);

        private final int type;

        PicType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public static PicType getPicType(int value) {
            PicType[] picTypes = PicType.values();
            for (PicType picType : picTypes) {
                if (value == picType.getType()) {
                    return picType;
                }
            }

            return OTHER;
        }
    }

    public enum CutType {
        UNKNOWN(0), NORMAL(1), CUT(2);

        private final int type;

        CutType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public static CutType getCutType(int value) {
            CutType[] cutTypes = CutType.values();
            for (CutType cutType : cutTypes) {
                if (value == cutType.getType()) {
                    return cutType;
                }
            }

            return NORMAL;
        }

    }

    private static final String TYPE_WEBP = "WEBP";

    private String localPath; // 非服务器返回，用于本地占位微博图片显示

    private int localResourceId = -1; // 非服务器返回，用于显示本地资源图片

    private int localWidth;

    private int localHeight;

    private String object_id;

    private String pic_id;

    private int photo_tag;// 标识是否有图片标签

    /**
     * Constructor
     */
    public PicInfo() {
        super();
    }

    public PicInfo(String jsonStr) throws Exception {
        super(jsonStr);
    }

    public PicInfo(JSONObject jsonObj) throws Exception {
        super(jsonObj);
    }

    public void setObjectId(String objectId) {
        object_id = objectId;
    }

    public String getObjectId() {
        return object_id;
    }

    public void setPicId(String picId) {
        this.pic_id = picId;
    }

    public String getPicId() {
        return pic_id;
    }

    public void setPhotoTag(int photo_tag) {
        this.photo_tag = photo_tag;
    }

    public int getPhotoTag() {
        return photo_tag;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public int getLocalResourceId() {
        return localResourceId;
    }

    public void setLocalResourceId(int localResourceId) {
        this.localResourceId = localResourceId;
    }

    public String getLargestUrl() {
        if (largest != null) {
            return largest.getUrl() == null ? "" : largest.getUrl();
        }
        return "";
    }

    public void setLargestUrl(String LargestUrl) {
        if (largest == null) {
            largest = new PicInfoSize();
        }
        largest.setUrl(LargestUrl);
    }

    public int getLargestWidth() {
        if (largest != null) {
            return largest.getWidth();
        }
        return INT_DEFAULT;
    }

    public void setLargestWidth(int LargestWidth) {
        if (largest == null) {
            largest = new PicInfoSize();
        }
        largest.setWidth(LargestWidth);
    }

    public int getLargestHeight() {
        if (largest != null) {
            return largest.getHeight();
        }
        return INT_DEFAULT;
    }

    public void setLargestHeight(int LargestHeight) {
        if (largest == null) {
            largest = new PicInfoSize();
        }
        largest.setHeight(LargestHeight);
    }

    public PicType getLargestType() {
        if (largest != null) {
            return getType(largest.getType());
        }
        return PicType.OTHER;
    }

    public void setLargestType(PicType LargestType) {
        if (largest == null) {
            largest = new PicInfoSize();
        }
        largest.setType(getTypeStr(LargestType));
    }

    public void setLargestTypeByString(String LargestType) {
        if (largest == null) {
            largest = new PicInfoSize();
        }
        largest.setType(LargestType);
    }

    public CutType getLargestCutType() {
        if (largest != null) {
            return CutType.getCutType(largest.getCut_type());
        }
        return CutType.UNKNOWN;
    }

    public void setLargestCutType(CutType LargestCutType) {
        if (largest == null) {
            largest = new PicInfoSize();
        }
        largest.setCut_type(LargestCutType.getType());
    }

    public String getThumbnailUrl() {
        if (thumbnail != null) {
            return thumbnail.getUrl() == null ? "" : thumbnail.getUrl();
        }
        return "";
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        if (thumbnail == null) {
            thumbnail = new PicInfoSize();
        }
        thumbnail.setUrl(thumbnailUrl);
    }

    public int getThumbnailWidth() {
        if (thumbnail != null) {
            return thumbnail.getWidth();
        }
        return INT_DEFAULT;
    }

    public void setThumbnailWidth(int thumbnailWidth) {
        if (thumbnail == null) {
            thumbnail = new PicInfoSize();
        }
        thumbnail.setWidth(thumbnailWidth);
    }

    public int getThumbnailHeight() {
        if (thumbnail != null) {
            return thumbnail.getHeight();
        }
        return INT_DEFAULT;
    }

    public void setThumbnailHeight(int thumbnailHeight) {
        if (thumbnail == null) {
            thumbnail = new PicInfoSize();
        }
        thumbnail.setHeight(thumbnailHeight);
    }

    public PicType getThumbnailType() {
        if (thumbnail != null) {
            return getType(thumbnail.getType());
        }
        return PicType.OTHER;
    }

    public void setThumbnailType(PicType thumbnailType) {
        if (thumbnail == null) {
            thumbnail = new PicInfoSize();
        }
        thumbnail.setType(getTypeStr(thumbnailType));
    }

    public void setThumbnailTypeByString(String thumbnailType) {
        if (thumbnail == null) {
            thumbnail = new PicInfoSize();
        }
        thumbnail.setType(thumbnailType);
    }

    public CutType getThumbnailCutType() {
        if (thumbnail != null) {
            return CutType.getCutType(thumbnail.getCut_type());
        }
        return CutType.UNKNOWN;
    }

    public void setThumbnailCutType(CutType thumbnailCutType) {
        if (thumbnail == null) {
            thumbnail = new PicInfoSize();
        }
        thumbnail.setCut_type(thumbnailCutType.getType());
    }

    public String getBmiddleUrl() {
        if (bmiddle != null) {
            return bmiddle.getUrl() == null ? "" : bmiddle.getUrl();
        }
        return "";
    }

    public void setBmiddleUrl(String bmiddleUrl) {
        if (bmiddle == null) {
            bmiddle = new PicInfoSize();
        }
        bmiddle.setUrl(bmiddleUrl);
    }

    public int getBmiddleWidth() {
        if (bmiddle != null) {
            return bmiddle.getWidth();
        }
        return INT_DEFAULT;
    }

    public void setBmiddleWidth(int bmiddleWidth) {
        if (bmiddle == null) {
            bmiddle = new PicInfoSize();
        }
        bmiddle.setWidth(bmiddleWidth);
    }

    public int getBmiddleHeight() {
        if (bmiddle != null) {
            return bmiddle.getHeight();
        }
        return INT_DEFAULT;
    }

    public void setBmiddleHeight(int bmiddleHeight) {
        if (bmiddle == null) {
            bmiddle = new PicInfoSize();
        }
        bmiddle.setHeight(bmiddleHeight);
    }

    public PicType getBmiddleType() {
        if (bmiddle != null) {
            return getType(bmiddle.getType());
        }
        return PicType.OTHER;
    }

    public void setBmiddleType(PicType bmiddleType) {
        if (bmiddle == null) {
            bmiddle = new PicInfoSize();
        }
        bmiddle.setType(getTypeStr(bmiddleType));
    }

    public void setBmiddleTypeByString(String bmiddleType) {
        if (bmiddle == null) {
            bmiddle = new PicInfoSize();
        }
        bmiddle.setType(bmiddleType);
    }

    public CutType getBmiddleCutType() {
        if (bmiddle != null) {
            return CutType.getCutType(bmiddle.getCut_type());
        }
        return CutType.UNKNOWN;
    }

    public void setBmiddleCutType(CutType bmiddleCutType) {
        if (bmiddle == null) {
            bmiddle = new PicInfoSize();
        }
        bmiddle.setCut_type(bmiddleCutType.getType());
    }

    public String getLargeUrl() {
        if (large != null) {
            return large.getUrl() == null ? "" : large.getUrl();
        }
        return "";
    }

    public void setLargeUrl(String largeUrl) {
        if (large == null) {
            large = new PicInfoSize();
        }
        large.setUrl(largeUrl);
    }

    public int getLargeWidth() {
        if (large != null) {
            return large.getWidth();
        }
        return INT_DEFAULT;
    }

    public void setLargeWidth(int largeWidth) {
        if (large == null) {
            large = new PicInfoSize();
        }
        large.setWidth(largeWidth);
    }

    public int getLargeHeight() {
        if (large != null) {
            return large.getHeight();
        }
        return INT_DEFAULT;
    }

    public void setLargeHeight(int largeHeight) {
        if (large == null) {
            large = new PicInfoSize();
        }
        large.setHeight(largeHeight);
    }

    public PicType getLargeType() {
        if (large != null) {
            return getType(large.getType());
        }
        return PicType.OTHER;
    }

    public void setLargeType(PicType largeType) {
        if (large == null) {
            large = new PicInfoSize();
        }
        large.setType(getTypeStr(largeType));
    }

    public void setLargeTypeByString(String largeType) {
        if (large == null) {
            large = new PicInfoSize();
        }
        large.setType(largeType);
    }

    public CutType getLargeCutType() {
        if (large != null) {
            return CutType.getCutType(large.getCut_type());
        }
        return CutType.UNKNOWN;
    }

    public void setLargeCutType(CutType largeCutType) {
        if (large == null) {
            large = new PicInfoSize();
        }
        large.setCut_type(largeCutType.getType());
    }

    public String getOriginalUrl() {
        if (original != null) {
            return original.getUrl() == null ? "" : original.getUrl();
        }
        return "";
    }

    public void setOriginalUrl(String originalUrl) {
        if (original == null) {
            original = new PicInfoSize();
        }
        original.setUrl(originalUrl);
    }

    public int getOriginalWidth() {
        if (original != null) {
            return original.getWidth();
        }
        return INT_DEFAULT;
    }

    public void setOriginalWidth(int originalWidth) {
        if (original == null) {
            original = new PicInfoSize();
        }
        original.setWidth(originalWidth);
    }

    public int getOriginalHeight() {
        if (original != null) {
            return original.getHeight();
        }
        return INT_DEFAULT;
    }

    public void setOriginalHeight(int originalHeight) {
        if (original == null) {
            original = new PicInfoSize();
        }
        original.setHeight(originalHeight);
    }

    public PicType getOriginalType() {
        if (original != null) {
            return getType(original.getType());
        }
        return PicType.OTHER;
    }

    public void setOriginalType(PicType originalType) {
        if (original == null) {
            original = new PicInfoSize();
        }
        original.setType(getTypeStr(originalType));
    }

    public void setOriginalTypeByString(String originalType) {
        if (original == null) {
            original = new PicInfoSize();
        }
        original.setType(originalType);
    }

    public CutType getOriginalCutType() {
        if (original != null) {
            return CutType.getCutType(original.getCut_type());
        }
        return CutType.UNKNOWN;
    }

    public void setOriginalCutType(CutType originalCutType) {
        if (original == null) {
            original = new PicInfoSize();
        }
        original.setCut_type(originalCutType.getType());
    }

    public int getLocalWidth() {
        return localWidth;
    }

    public void setLocalWidth(int localWidth) {
        this.localWidth = localWidth;
    }

    public int getLocalHeight() {
        return localHeight;
    }

    public void setLocalHeight(int localHeight) {
        this.localHeight = localHeight;
    }

    public PicInfoSize getPic_small() {
        return pic_small;
    }

    public PicInfoSize getPic_big() {
        return pic_big;
    }

    public PicInfoSize getPic_middle() {
        return pic_middle;
    }

    public String getPicBigUrl() {
        if (pic_big != null) {
            return pic_big.getUrl() == null ? "" : pic_big.getUrl();
        }
        return "";
    }

    public void setPicBigUrl(String picBigUrl) {
        if (pic_big == null) {
            pic_big = new PicInfoSize();
        }
        pic_big.setUrl(picBigUrl);
    }

    public int getPicBigWidth() {
        if (pic_big != null) {
            return pic_big.getWidth();
        }
        return INT_DEFAULT;
    }

    public void setPicBigWidth(int picBigWidth) {
        if (pic_big == null) {
            pic_big = new PicInfoSize();
        }
        pic_big.setWidth(picBigWidth);
    }

    public int getPicBigHeight() {
        if (pic_big != null) {
            return pic_big.getHeight();
        }
        return INT_DEFAULT;
    }

    public void setPicBigHeight(int picBigHeight) {
        if (pic_big == null) {
            pic_big = new PicInfoSize();
        }
        pic_big.setHeight(picBigHeight);
    }

    public String getPicSmallUrl() {
        if (pic_small != null) {
            return pic_small.getUrl() == null ? "" : pic_small.getUrl();
        }
        return "";
    }

    public void setPicSmallUrl(String picSmallUrl) {
        if (pic_small == null) {
            pic_small = new PicInfoSize();
        }
        pic_small.setUrl(picSmallUrl);
    }

    public int getPicMiddleWidth() {
        if (pic_middle != null) {
            return pic_middle.getWidth();
        }
        return INT_DEFAULT;
    }

    public void setPicMiddleWidth(int picMiddleWidth) {
        if (pic_middle == null) {
            pic_middle = new PicInfoSize();
        }
        pic_middle.setWidth(picMiddleWidth);
    }

    public int getPicMiddleHeight() {
        if (pic_middle != null) {
            return pic_middle.getHeight();
        }
        return INT_DEFAULT;
    }

    public void setPicMiddleHeight(int picMiddleHeight) {
        if (pic_middle == null) {
            pic_middle = new PicInfoSize();
        }
        pic_middle.setHeight(picMiddleHeight);
    }

    public String getPicMiddleUrl() {
        if (pic_middle != null) {
            return pic_middle.getUrl() == null ? "" : pic_middle.getUrl();
        }
        return "";
    }

    public void setPicMiddleUrl(String picMiddleUrl) {
        if (pic_middle == null) {
            pic_middle = new PicInfoSize();
        }
        pic_middle.setUrl(picMiddleUrl);
    }

    public int getPicSmallWidth() {
        if (pic_small != null) {
            return pic_small.getWidth();
        }
        return INT_DEFAULT;
    }

    public void setPicSmallWidth(int picSmallWidth) {
        if (pic_small == null) {
            pic_small = new PicInfoSize();
        }
        pic_small.setWidth(picSmallWidth);
    }

    public int getPicSmallHeight() {
        if (pic_small != null) {
            return pic_small.getHeight();
        }
        return INT_DEFAULT;
    }

    public void setPicSmallHeight(int picSmallHeight) {
        if (pic_small == null) {
            pic_small = new PicInfoSize();
        }
        pic_small.setHeight(picSmallHeight);
    }

    @Override
    public JsonDataObject initFromJsonObject(JSONObject jsonObj) throws Exception {
        if (jsonObj == null) {
            return null;
        }
        object_id = jsonObj.optString("object_id");
        photo_tag = jsonObj.optInt("photo_tag", WITHOUT_PHOTO_TAG);
        JSONObject jobjThumbnail = jsonObj.optJSONObject("thumbnail");
        if (jobjThumbnail != null) {
            if (thumbnail == null) {
                thumbnail = new PicInfoSize();
            }
            thumbnail.setUrl(jobjThumbnail.optString("url"));
            thumbnail.setWidth(jobjThumbnail.optInt("width", -1));
            thumbnail.setHeight(jobjThumbnail.optInt("height", -1));
            thumbnail.setType(jobjThumbnail.optString("type"));
            thumbnail.setCut_type(jobjThumbnail.optInt("cut_type"));
        }

        JSONObject jobjBmiddle = jsonObj.optJSONObject("bmiddle");
        if (jobjBmiddle != null) {
            if (bmiddle == null) {
                bmiddle = new PicInfoSize();
            }
            bmiddle.setUrl(jobjBmiddle.optString("url"));
            bmiddle.setWidth(jobjBmiddle.optInt("width", -1));
            bmiddle.setHeight(jobjBmiddle.optInt("height", -1));
            bmiddle.setType(jobjBmiddle.optString("type"));
            bmiddle.setCut_type(jobjBmiddle.optInt("cut_type"));
        }

        JSONObject jobjLarge = jsonObj.optJSONObject("large");
        if (jobjLarge != null) {
            if (large == null) {
                large = new PicInfoSize();
            }
            large.setUrl(jobjLarge.optString("url"));
            large.setWidth(jobjLarge.optInt("width", -1));
            large.setHeight(jobjLarge.optInt("height", -1));
            large.setType(jobjLarge.optString("type"));
            large.setCut_type(jobjLarge.optInt("cut_type"));
        }

        JSONObject jobjOriginal = jsonObj.optJSONObject("original");
        if (jobjOriginal != null) {
            if (original == null) {
                original = new PicInfoSize();
            }
            original.setUrl(jobjOriginal.optString("url"));
            original.setWidth(jobjOriginal.optInt("width", -1));
            original.setHeight(jobjOriginal.optInt("height", -1));
            original.setType(jobjOriginal.optString("type"));
            original.setCut_type(jobjOriginal.optInt("cut_type"));
        }
        JSONObject jobjLargest = jsonObj.optJSONObject("largest");
        if (jobjLargest != null) {
            if (largest == null) {
                largest = new PicInfoSize();
            }
            largest.setUrl(jobjLargest.optString("url"));
            largest.setWidth(jobjLargest.optInt("width", -1));
            largest.setHeight(jobjLargest.optInt("height", -1));
            largest.setType(jobjLargest.optString("type"));
            largest.setCut_type(jobjLargest.optInt("cut_type"));
        }

        JSONObject jobjPicSmall = jsonObj.optJSONObject("pic_small");
        if (jobjPicSmall != null) {
            if (pic_small == null) {
                pic_small = new PicInfoSize();
            }
            pic_small.setUrl(jobjPicSmall.optString("url"));
            pic_small.setWidth(jobjPicSmall.optInt("width", -1));
            pic_small.setHeight(jobjPicSmall.optInt("height", -1));
        }

        JSONObject jobjPicBig = jsonObj.optJSONObject("pic_big");
        if (jobjPicBig != null) {
            if (pic_big == null) {
                pic_big = new PicInfoSize();
            }
            pic_big.setUrl(jobjPicBig.optString("url"));
            pic_big.setWidth(jobjPicBig.optInt("width", -1));
            pic_big.setHeight(jobjPicBig.optInt("height", -1));
        }

        JSONObject jobjPicMiddle = jsonObj.optJSONObject("pic_middle");
        if (jobjPicMiddle != null) {
            if (pic_middle == null) {
                pic_middle = new PicInfoSize();
            }
            pic_middle.setUrl(jobjPicBig.optString("url"));
            pic_middle.setWidth(jobjPicBig.optInt("width", -1));
            pic_middle.setHeight(jobjPicBig.optInt("height", -1));
        }

        return this;
    }

    private PicType getType(String type) {
        if (type == null) {
            return PicType.OTHER;
        }
        return TYPE_WEBP.equals(type) ? PicType.WEBP : PicType.OTHER;
    }

    private String getTypeStr(PicType type) {
        return PicType.WEBP == type ? TYPE_WEBP : "JPEG";// TYPE_WEBP.equals(type)
                                                         // ? PicType.WEBP :
                                                         // PicType.OTHER;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PicInfo)) {
            return false;
        }

        PicInfo picInfo = (PicInfo)o;
        return equalUrl(getThumbnailUrl(), picInfo.getThumbnailUrl())
                && equalUrl(getBmiddleUrl(), picInfo.getBmiddleUrl()) && equalUrl(getLargeUrl(), picInfo.getLargeUrl())
                && equalUrl(getOriginalUrl(), picInfo.getOriginalUrl());
    }

    private boolean equalUrl(String oriUrl, String desUrl) {
        if (oriUrl == null) {
            if (desUrl == null) {
                return true;
            } else {
                return false;
            }
        } else {
            return oriUrl.equals(desUrl);
        }
    }

    // TODO 应该是小图
    public String getThumbnailFilePath(Context context, boolean checkExist) {
        String saveDir = Utils.getPictureImgSaveDir(context);
        return saveDir;
    }

    public String getBmiddleFilePath(Context context, boolean checkExist) {
        String saveDir = Utils.getPictureImgSaveDir(context);
        return saveDir;
    }

    public String getLargeFilePath(Context context, boolean checkExist) {
        String saveDir = Utils.getPictureImgSaveDir(context);
        return saveDir;
    }

    public String getOriginalFilePath(Context context, boolean checkExist) {
        String saveDir = Utils.getPictureImgSaveDir(context);
        return saveDir;
    }
}
