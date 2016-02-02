
package com.dosh.bigimage.model;

import com.dosh.bigimage.model.PicInfo.CutType;
import com.dosh.bigimage.model.PicInfo.PicType;

public class Picture {
    public static final int NONE = 0;

    public static final int THUMBNAIL = 1;

    public static final int BMIDDLE = 2;

    public static final int LARGE = 3;

    public static final int ORIGINAL = 4;

    private PicInfo picInfo;

    // private String path;
    // private PicType type;
    private int urlType;

    private String localMblogId;

    public PicInfo getPicInfo() {
        return picInfo;
    }

    public void setPicInfo(PicInfo picInfo) {
        this.picInfo = picInfo;
    }

    public void setUrlType(int urlType) {
        this.urlType = urlType;
    }

    public int getUrlType() {
        return urlType;
    }

    public String getLocalPath() {
        return picInfo.getLocalPath();
    }

    public void setLocalPath(String localPath) {
        picInfo.setLocalPath(localPath);
    }

    public String getLocalMblogId() {
        return localMblogId;
    }

    public void setLocalMblogId(String localMblogId) {
        this.localMblogId = localMblogId;
    }

    public String getPath() {
        // return path;
        if (urlType == THUMBNAIL) {
            return picInfo.getThumbnailUrl();
        } else if (urlType == BMIDDLE) {
            return picInfo.getBmiddleUrl();
        } else if (urlType == LARGE) {
            return picInfo.getLargeUrl();
        } else {
            return picInfo.getOriginalUrl();
        }
    }

    // public void setPath( String path ) {
    // this.path = path;
    // }

    public PicType getType() {
        // return type;
        if (urlType == THUMBNAIL) {
            return picInfo.getThumbnailType();
        } else if (urlType == BMIDDLE) {
            return picInfo.getBmiddleType();
        } else if (urlType == LARGE) {
            return picInfo.getLargeType();
        } else {
            return picInfo.getOriginalType();
        }
    }

    public CutType getCutType() {
        // return type;
        if (urlType == THUMBNAIL) {
            return picInfo.getThumbnailCutType();
        } else if (urlType == BMIDDLE) {
            return picInfo.getBmiddleCutType();
        } else if (urlType == LARGE) {
            return picInfo.getLargeCutType();
        } else {
            return picInfo.getOriginalCutType();
        }
    }

    // public void setType( PicType type ) {
    // this.type = type;
    // }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Picture)) {
            return false;
        }

        Picture pic = (Picture)o;
        if (picInfo == null) {
            return false;
        }

        return picInfo.equals(pic.picInfo);
    }
}
