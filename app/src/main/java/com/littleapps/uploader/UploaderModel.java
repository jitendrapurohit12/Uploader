package com.littleapps.uploader;

/**
 * Created by jitendra on 22/2/18.
 */

public class UploaderModel {
    private String uploader1,uploader2;

    public UploaderModel(String uploader1, String uploader2) {
        this.uploader1 = uploader1;
        this.uploader2 = uploader2;
    }

    public String getUploader1() {
        return uploader1;
    }

    public void setUploader1(String uploader1) {
        this.uploader1 = uploader1;
    }

    public String getUploader2() {
        return uploader2;
    }

    public void setUploader2(String uploader2) {
        this.uploader2 = uploader2;
    }
}
