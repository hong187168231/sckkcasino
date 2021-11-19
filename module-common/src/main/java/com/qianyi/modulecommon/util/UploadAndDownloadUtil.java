package com.qianyi.modulecommon.util;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
/**
 * 文件工具类
 */
public class UploadAndDownloadUtil {

    private static String urlUpload= "/minio/upload";
    private static String urlNameBucket = "/minio/createBucket";
    /**
     * 图片上传 basePath  PreReadUploadConfig.getBasePath
     */
    public static String fileUpload(String basePath,MultipartFile file,String uploadUrl) {
        if (file == null) {
            return "";
        }
        String url=null;
        String fileName = file.getOriginalFilename();
        fileName = UploadAndDownloadUtil.renameToUUID(fileName);
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("name", fileName);
            params.put("bucket", fileName);
            params.put("file", file);
            //创建bucket
            HttpClient4Util.doPost(uploadUrl+urlNameBucket, params);
            //上传
            url  = HttpClient4Util.doPost(uploadUrl+urlUpload, params);
        } catch (Exception e) {
            return "";
        }
        return url;

    }
    public static void uploadFiles(byte[] file, String filePath, String fileName) throws Exception {
        File targetFile = new File(filePath);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        FileOutputStream out = new FileOutputStream(filePath + fileName);
        out.write(file);
        out.flush();
        out.close();
    }
    /**
     * 创建新的文件名
     */
    public static String renameToUUID(String fileName) {
        return UUID.randomUUID() + "." + fileName.substring(fileName.lastIndexOf(".") + 1);
    }

}
