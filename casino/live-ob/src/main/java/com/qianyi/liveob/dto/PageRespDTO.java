package com.qianyi.liveob.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

@Data
public class PageRespDTO {

    private Long pageSize = 0L;

    private Long totalRecord = 0L;

    private Integer totalPage = 0;

    private List<GameRecordQueryRespDTO> record;


}
