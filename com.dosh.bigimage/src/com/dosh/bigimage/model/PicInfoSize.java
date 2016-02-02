
package com.dosh.bigimage.model;

import java.io.Serializable;

public class PicInfoSize implements Serializable {

    private static final long serialVersionUID = 2163309030323910983L;

    private int cut_type;

    private int height;

    private String type;

    private String url;

    private int width;

    public int getCut_type() {
        return cut_type;
    }

    public void setCut_type(int cut_type) {
        this.cut_type = cut_type;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getType() {
        return type == null ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url == null ? "" : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

}
