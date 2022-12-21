package com.qianyi.liveob.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author joy
 * @date 2019/9/12 17:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRespDTO {

    @ApiModelProperty("登陆地址")
    private String url;
}
