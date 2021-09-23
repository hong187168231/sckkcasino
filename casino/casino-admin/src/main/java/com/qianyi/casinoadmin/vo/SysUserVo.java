package com.qianyi.casinoadmin.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class SysUserVo implements Serializable {

    private static final long serialVersionUID = -3005417929250305179L;

    private String userName;

    private String gaBind;
}
