package com.qianyi.casinocore.vo;

import com.qianyi.casinocore.model.UserDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserDetailVo implements Serializable {

    private static final long serialVersionUID = 1155409999902142176L;

    /**
     * 当前页面
     */
    @ApiModelProperty(value = "当前页面")
    private Integer currentPage;

    /**
     * 笔数
     */
    @ApiModelProperty(value = "笔数")
    private Integer perPage;

    /**
     * 总笔数
     */
    @ApiModelProperty(value = "总笔数")
    private Integer total;

    /**
     * 返回数据对象
     */
    @ApiModelProperty(value = "返回数据对象")
    private List<UserDetail> data;
}
