package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class IpWhiteVo implements Serializable {

    private static final long serialVersionUID = -6845757575779L;

    @ApiModelProperty(value = "ID")
    private Long id;

    @ApiModelProperty(value = "ip")
    private String ip;

    /** 1:总控管理员 */
//    @ApiModelProperty(value = "1:总控管理员")
//    private Integer type;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty("创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("最后修改时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty("最后修改人")
    private String updateBy;
}
