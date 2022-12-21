package com.qianyi.liveob.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author joy
 * @date 2020/1/18 23:15
 */
@Data
@Builder
public class GameRecordRespDTO {


    /**
     * 本次请求最后结果集ID
     */
    private String requestVersionId;

    /**
     * 本次返回结果集最后的ID
     */
    private String responseVersionId;

    /**
     * 本次请求关联的日期（yyyyMMdd），requestVersionId为0时默认就是商户的开户日期
     */
    private String requestDate;

    /**
     * 本次返回结果集相关联last id的createat
     */
    private String responseDate;

    /**
     * 集合的size
     */
    private Integer size;

    /**
     * 结果集合
     */
    private List<GameRecordQueryRespDTO> result;


}
