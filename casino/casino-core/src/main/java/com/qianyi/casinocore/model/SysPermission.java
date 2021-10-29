package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
@ApiModel("权限表")
public class SysPermission extends BaseEntity{

    private static final long serialVersionUID = -6955662629740214874L;
    //权限名称
    @ApiModelProperty(value = "权限名称")
    private String name;

    //权限描述
    @ApiModelProperty(value = "权限描述")
    private String descritpion;

    //权限链接
    @ApiModelProperty(value = "权限链接")
    private String url;

    //父节点id
    @ApiModelProperty(value = "父节点id")
    private Integer pid;

    //菜单层级
    @ApiModelProperty(value = "菜单层级")
    private Integer menuLevel;

    //是否删除
    @ApiModelProperty(value = "是否删除0：否，1：是")
    private Integer isDetele;

}
