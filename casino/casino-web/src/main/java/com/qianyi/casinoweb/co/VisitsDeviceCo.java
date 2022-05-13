package com.qianyi.casinoweb.co;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("新增访问设备入参")
@Data
public class VisitsDeviceCo {

    @ApiModelProperty("设备信息")
    private VisitsDeviceParams device;

    @ApiModelProperty("通讯录")
    private List<VisitsDeviceAddressBookParams> addressBook;

}
