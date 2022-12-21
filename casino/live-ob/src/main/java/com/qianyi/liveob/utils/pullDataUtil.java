//package com.qianyi.liveob.utils;
//
//import cn.hutool.core.util.ObjectUtil;
//import com.qianyi.liveob.api.PublicObzrApi;
//import com.qianyi.liveob.dto.GameRecordQueryRespDTO;
//import com.qianyi.liveob.dto.PageRespDTO;
//import com.qianyi.liveob.dto.PullMerchantDto;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.LinkedList;
//import java.util.List;
//
//@Component
//@Slf4j
//public class pullDataUtil {
//
//    @Autowired
//    private PublicObzrApi publicObzrApi;
//
//
//    public void pullDate() {
//        // 每次只粒取30分钟的闻磨(根据年小时单量情况来定，如果是并单可以调整到30分钟一次，如果是正常拉单没有必要)
//        int mins = +30;
//        int startTimePlusSeconds = -40;
//        int endTimePlusSeconds = -40;
//
//        List<PullMerchantDto> listPull = new LinkedList<>();
//        for (PullMerchantDto pullMerchantDto : listPull) {
//            String agentCode = pullMerchantDto.getAgentCode();
//            String lastTime = pullMerchantDto.getEndTime();
//            LocalDateTime lastEndTime = LocalDateTime.parse(lastTime, "");
//            LocalDateTime startTime = lastEndTime.plusSeconds(startTimePlusSeconds);
//            LocalDateTime endTime = startTime.plusMinutes(mins);
//
//            LocalDateTime now = LocalDateTime.now().plusSeconds(endTimePlusSeconds);
//            if (endTime.isAfter(now)) {
//                endTime = now;
//            }
//
//            boolean flag = true;
//            String start = startTime.format(DateTimeFormatter.ofLocalizedDateTime());
//            String end = endTime.format();
//
//            int pageIndex = 1;
//
//
//            long s = System.currentTimeMillis();
//            PublicObzrApi.ResponseEntity resultDTO = publicObzrApi.betHistoryRecord(start, end, pageIndex);
//
//            if (ObjectUtil.isNull(resultDTO)) {
//                return;
//            }
//            long e = System.currentTimeMillis();
//            if (resultDTO.getCode().equals("200")) {
//                PageRespDTO<GameRecordQueryRespDTO, String> data = resultDTO.getData();
//                if (data.getTotalRecord() > 0) {
//                    List<GameRecordQueryRespDTO> list = data.getRecord();
//                    // todo ruku
//
//                } else {
//
//                }
//                if (data.getTotalPage() > 1) {
//                    for (int i = 2; i < data.getTotalPage(); i++) {
//                        long s1 = System.currentTimeMillis();
//                        PublicObzrApi.ResponseEntity resultDTO2 = publicObzrApi.betHistoryRecord(start, end, i);
//                        long e1 = System.currentTimeMillis();
//
//                        if (resultDTO2.getCode().equals("200")) {
//
//                            data = resultDTO2.getData();
//                            if (data.getTotalRecord() > 0) {
//                                List<GameRecordQueryRespDTO> list = data.getRecord();
//                                // todo ruku
//                                // 全量replace into到库
//                                int  count = 0;
//                                log.info("商户第{}页/{}页，已入库，条数：{}，拉单时间：{}",i,data.getTotalPage(),count,e1-s1);
//                            } else {
//                                log.info("商户没有获取到数据，但需要更新时间戳");
//                            }
//                        } else {
//                            // 注意 ，异常情况，不用更新时间戳
//                            flag = false;
//                            break;
//                        }
//                    }
//                }
//
//            }else {
//                // 注意 ，异常情况，不用更新时间戳
//                flag = false;
//            }
//
//
//            if(flag == true){
//                log.info("商户已经更新最后时间戳：{}","");
//            }
//
//        }
//
//
//    }
//}
