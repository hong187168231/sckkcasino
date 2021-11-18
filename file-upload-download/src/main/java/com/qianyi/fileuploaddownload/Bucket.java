package com.qianyi.fileuploaddownload;

public enum Bucket {

    CASINO_ADMIN("casino-admin");

    private String bucketName;


    Bucket(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return bucketName;
    }

    /**
     * 检查Bucket是否存在
     * @param bucketName
     * @return
     */
    public static boolean checkBucketExist(String bucketName) {
        for (Bucket bucket : Bucket.values()) {
            if (bucket.getBucketName().equals(bucketName)) {
                return true;
            }
        }
        return false;
    }
}
