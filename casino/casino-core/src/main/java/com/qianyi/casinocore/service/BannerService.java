package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.Banner;
import com.qianyi.casinocore.repository.BannerRepository;
import com.qianyi.modulecommon.exception.StaffPointsException;
import com.qianyi.modulecommon.util.CommonConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

@Service
public class BannerService {
    private static final Logger logger = LoggerFactory.getLogger(BannerService.class);
    @Autowired
    private BannerRepository bannerRepository;

    public List<Banner> findByBannerList(){
        return bannerRepository.findByBannerList();
    }

    public void deleteById(Integer id){
        if (id == null){
            throw new StaffPointsException(-1,"id不能为空");
        }
        bannerRepository.deleteById(id);
    }

    public void updateById(Integer id, String articleLink, HttpServletRequest request)throws Exception {
        if (id == null){
            throw new StaffPointsException(-1,"id不能为空");
        }
        Map<Integer,String> map = this.getfilePaths(request);
        bannerRepository.updateById(id,map.get(CommonConst.NUMBER_0),
                map.get(CommonConst.NUMBER_1),map.get(CommonConst.NUMBER_2),map.get(CommonConst.NUMBER_3),
                map.get(CommonConst.NUMBER_4),articleLink);
    }

    public void saveBanner(Banner banner,HttpServletRequest request) throws Exception {
        Map<Integer,String> map = this.getfilePaths(request);
        banner.setFirstMap(map.get(CommonConst.NUMBER_0));
        banner.setSecondMap(map.get(CommonConst.NUMBER_1));
        banner.setThirdlyMap(map.get(CommonConst.NUMBER_2));
        banner.setFirstMap(map.get(CommonConst.NUMBER_3));
        banner.setFifthMap(map.get(CommonConst.NUMBER_4));
        bannerRepository.saveAndFlush(banner);
    }

    private Map<Integer,String> getfilePaths(HttpServletRequest request)throws Exception {
        String filePath = "";
        Map<Integer,String> map = new HashMap<Integer,String>();
        request.setCharacterEncoding("utf-8"); //设置编码
        String realPath = request.getSession().getServletContext().getRealPath("/uploadFile/");
        File dir = new File(realPath);
        //文件目录不存在，就创建一个
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
        try {
            StandardMultipartHttpServletRequest req = (StandardMultipartHttpServletRequest) request;
            //获取formdata的值
            Iterator<String> iterator = req.getFileNames();
            while (iterator.hasNext()) {
                List<MultipartFile> files = req.getFiles(iterator.next());
                if (files == null||files.size() >= 5){
                    throw new StaffPointsException(-1,"图片最多5张");
                }
                int limit = 0;
                for(MultipartFile file:files){
                    String fileName = file.getOriginalFilename();
                    //真正写到磁盘上
                    String uuid = UUID.randomUUID().toString().replace("-", "");
                    String kzm = fileName.substring(fileName.lastIndexOf("."));
                    String filename = uuid + kzm;
                    File file1 = new File(realPath + filename);
                    OutputStream out = new FileOutputStream(file1);
                    out.write(file.getBytes());
                    out.close();
                    filePath = request.getScheme() + "://" +
                            request.getServerName() + ":"
                            + request.getServerPort()
                            + "/uploadFile/" + filename;
                    map.put(limit++,filePath);
                }
            }
        } catch (Exception e) {
            logger.error("处理图片出错", e);
            throw new StaffPointsException(-1,"处理图片出错");
        }
        return map;
    }
}
