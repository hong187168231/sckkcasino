package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
@ApiModel("公司管理")
public class CompanyManagement extends BaseEntity{


    private String companyName;

}
