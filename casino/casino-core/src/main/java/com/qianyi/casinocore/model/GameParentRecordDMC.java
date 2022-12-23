package com.qianyi.casinocore.model;

import com.qianyi.casinocore.util.Md5Util;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name ="game_parent_record_dmc",uniqueConstraints={@UniqueConstraint(columnNames={"betOrderNo"})})
@ApiModel("大马彩彩游戏记录")
public class GameParentRecordDMC extends BaseEntity{

    private Integer id; //大马彩客户ID
    private Integer memberId; //大马彩客户ID
    private Integer merchantId; //商户ID
    private String betNumber;  //下注号码
    private Integer gamePlayId; //下注游戏代号
    private Integer betType;   //下注方式 - 0 (Box), 1 (iBox), 2 (Reverse), 3 (Straight)
    private BigDecimal totalAmount; //下注金额
    private BigDecimal netAmount; //有效下注金额
    private BigDecimal rebateAmount; //盈利金额
    private Integer rebatePercentage; //返水百分点
    private Date bettingDate;  //下注日期
    private Date drawDate;  //开彩日期
    private String drawNumber; //开彩期号
    private String ticketStatus;   //注单状况- SETTLED (已结算)，UNSETTLED (未结算)
    private String progressStatus; //注单进展- IN_PROGRESS (进行中)，ACCEPTED (已接受)，PARTIALLY_ACCEPTED (部分接受)，REJECTED (拒绝)，DELETED (删除)
    private String createdAt;  //注单创建时间
    private String updatedAt;  //注单修改时间
    private String customerId; //客户ID
    private String customerName;   //客户名称
    private String ticketNo;   //注单号

    public String getMd5(){
        StringBuffer sb = new StringBuffer();
        sb.append(this.id).append(this.merchantId).append(this.betNumber).append(this.betType)
                .append(this.getGamePlayId()).append(this.getTotalAmount()).append(this.getNetAmount())
                .append(this.getRebateAmount()).append(this.getBettingDate()).append(this.getDrawDate())
                .append(this.getDrawNumber()).append(this.getTicketStatus()).append(this.getProgressStatus())
                .append(this.getCreatedAt()).append(this.getUpdatedAt()).append(this.getCustomerName())
                .append(this.getTicketNo());

        return Md5Util.md5(sb.toString());
    }
}
