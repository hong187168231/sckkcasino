package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 *
 */
@Entity
@Data
@ApiModel("用户角色中间表")
@Table(indexes = {@Index(name="identity_index",columnList = "sysUserId",unique=true)})
public class SysUserRole extends BaseEntity {

    private static final long serialVersionUID = 5618839392019941019L;

    private Long sysUserId;

    private Long sysRoleId;
}
