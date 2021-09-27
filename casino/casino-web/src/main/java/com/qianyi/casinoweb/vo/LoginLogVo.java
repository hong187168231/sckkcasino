package com.qianyi.casinoweb.vo;

import com.qianyi.modulecommon.executor.JobSuperVo;
import lombok.Data;

@Data
public class LoginLogVo extends JobSuperVo {
    private String ip;
    private String account;
    private Long userId;
    //备注，标记，等
    private String remark;
}
