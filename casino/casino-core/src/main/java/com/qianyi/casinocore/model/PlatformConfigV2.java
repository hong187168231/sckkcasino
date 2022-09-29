package com.qianyi.casinocore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity
@ApiModel("平台配置表V2")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformConfigV2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ApiModelProperty("充值凭证开关 0 关闭 1 开启")
    private Integer chargeSwitch ;
}
