package com.qianyi.modulecommon.util;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;
/**
 * 文件工具类
 */
public class UploadAndDownloadUtil {
    /**
     * 图片上传 basePath  PreReadUploadConfig.getBasePath
     */
    public static String fileUpload(String basePath,MultipartFile file) {
        if (file == null) {
            return "";
        }
        String fileName = file.getOriginalFilename();
        fileName = UploadAndDownloadUtil.renameToUUID(fileName);
        try {
            UploadAndDownloadUtil.uploadFiles(file.getBytes(), basePath, fileName);
        } catch (Exception e) {
            return "";
        }
        String url = "/public/img/" + fileName;
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
