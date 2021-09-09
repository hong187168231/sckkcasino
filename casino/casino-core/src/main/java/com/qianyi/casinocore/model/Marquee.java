package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@ApiModel("跑马灯")
public class Marquee extends BaseEntity{
    /**
     * 内容
     */
    private String content;
    /**
     * 状态 1 启用 0 停用
     */
    private Integer state;
}
