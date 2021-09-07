package com.qianyi.casinocore.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@ApiModel("Banner图片地址表")
public class Banner {
    /**
     * ID
     */
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    private String firstMap;
    private String secondMap;
    private String thirdlyMap;
    private String fourthlyMap;
    private String fifthMap;
    /**
     * 文章链接
     */
    private String articleLink;
    /**
     * 展示端 1 web 2 app
     */
    private Integer theShowEnd;
    /**
     * 点击量
     */
    private Integer hits;
    /**
     * 最后操作时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @org.hibernate.annotations.UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdatedTime;
    /**
     * 最后操作人
     */
    private String lastUpdatedBy;

}
