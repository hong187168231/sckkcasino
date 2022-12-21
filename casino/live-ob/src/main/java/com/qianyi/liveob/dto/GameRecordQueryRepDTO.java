package com.qianyi.liveob.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author joy
 * @date 2019/9/27 10:00
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRecordQueryRepDTO {


    /**
     * 商户
     */
    private Long agentId;


    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;


    /**
     * 会员账号
     */
    private String playerName;

    /**
     * 状态
     */
    private Integer betStatus;

    /**
     * 局号
     */
    private String roundNo;

    /**
     * 设备类型
     */
    private Integer deviceType;

    /**
     * 注单类型
     */
    private Integer recordType;

    /**
     * 游戏模式
     */
    private Integer gameMode;

    /**
     * 最后id。scroll 滚动查询分页
     */
    private Long lastVersion;

    /**
     * 最后date。scroll 滚动查询分页。与last id是同一行的数据，createat字段，如果不匹配则会查询不到数据
     */
    private Integer lastDate;
}
