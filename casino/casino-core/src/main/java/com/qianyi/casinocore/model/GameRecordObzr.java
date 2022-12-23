package com.qianyi.casinocore.model;

import com.qianyi.modulecommon.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name ="game_record_obzr",uniqueConstraints={@UniqueConstraint(columnNames={"orderNo"})},indexes = {
    @Index(columnList = "netAt"),@Index(columnList = "userId")
})
@ApiModel("OB真人游戏记录")
public class GameRecordObzr extends BaseEntity{

    @ApiModelProperty(value = "我方账号")
    private Long userId;

    @ApiModelProperty(value = "玩家账号")
    private String playerName;

    @ApiModelProperty(value = "注单号。区别唯一性")
    private String orderNo;

    @ApiModelProperty(value = "玩家编号")
    private Long playerId;

    @ApiModelProperty(value = "代理编号")
    private Long agentId;

    @ApiModelProperty(value = "投注额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal betAmount  = BigDecimal.ZERO;

    @ApiModelProperty(value = "有效投注额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal validBetAmount  = BigDecimal.ZERO;

    @ApiModelProperty(value = "输赢额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal netAmount  = BigDecimal.ZERO ;

    @ApiModelProperty(value = "派彩额", required = true)
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal payoutAmount  = BigDecimal.ZERO;

    @ApiModelProperty(value = "下注前余额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal beforeAmount;

    @ApiModelProperty(value = "投注时间")
    private Long createdAt;

    @ApiModelProperty(value = "结算时间")
    private Long netAt;


    @ApiModelProperty(value = "投注时间(13位时间戳)")
    private Long betTime;

    @ApiModelProperty(value = "投注时间(yyyy-MM-dd)")
    private String betStrTime;


    @ApiModelProperty(value = "结算时间")
    private Long settleTime;

    @ApiModelProperty(value = "结算时间(yyyy-MM-dd)")
    private String settleStrTime;



    @ApiModelProperty(value = "注单重新派彩时间，此参数会是注单拉取的关键。默认等同netTime")
    private Long recalcuAt;

    @ApiModelProperty(value = "游戏编码")
    private Long gameTypeId;

    @ApiModelProperty(value = "厅id。国际厅/多台")
    private Integer platformId;

    @ApiModelProperty(value = "厅名称。国际厅/多台")
    private String platformName;

    @ApiModelProperty(value = "注单状态 0=未结算 1=已结算")
    private Integer betStatus;

    @ApiModelProperty(value = "重算标志。0=正常结算，2=取消指定局的结算，3=取消该注单的结算 4=重算指定局 5=重算指定注单")
    private Integer betFlag;

    @ApiModelProperty(value = "玩法，下注点")
    private Long betPointId;

    @ApiModelProperty(value = "结果。")
    private String judgeResult;

    @ApiModelProperty(value = "币种")
    private String currency;

    @ApiModelProperty(value = "台桌号")
    private String tableCode;

    @ApiModelProperty(value = "局号")
    private String roundNo;

    @ApiModelProperty(value = "靴号")
    private String bootNo;

    @ApiModelProperty(value = "游戏ip")
    private String loginIp;

    @ApiModelProperty(value = "设备类型。1=网页，2=手机网页，3=Ios，4=Android，5=其他设备")
    private Integer deviceType;

    @ApiModelProperty(value = "设备id。区别设备唯一性的md5结果")
    private String deviceId;

    @ApiModelProperty(value = "注单类别。0、试玩 1、正式 2、内部测试 3、机器人。只有为1记录的才会返回给商户。")
    private Integer recordType;

    @ApiModelProperty(value = "游戏模式。0=常规 1=好路 3=多台")
    private Integer gameMode;
    @ApiModelProperty(value = "新增字段。会员昵称")
    private String nickName;
    @ApiModelProperty(value = "游戏桌台名称")
    private String tableName;
    @ApiModelProperty(value = "玩法名称")
    private String betPointName;
    @ApiModelProperty(value = "游戏名称")
    private String gameTypeName;


    @ApiModelProperty(value = "返奖金额。同派彩额，只是取消局和跳局会记录为投注额", required = true)
    private BigDecimal payAmount;



    @ApiModelProperty("总代ID")
    private Long firstProxy;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    @ApiModelProperty("基层代理ID")
    private Long thirdProxy;

    @ApiModelProperty(value = "洗码状态：1：成功")
    private Integer washCodeStatus;

    @ApiModelProperty(value = "返利状态：1：成功")
    private Integer rebateStatus;

    @ApiModelProperty(value = "抽点状态: 0: 否 1: 是")
    private Integer extractStatus;

    @ApiModelProperty(value = "等级流水状态：1：成功")
    private Integer levelWaterStatus;

    @ApiModelProperty(value = "打码状态：1：成功")
    private Integer codeNumStatus;

    @ApiModelProperty(value = "分润状态：0:失败，>0：成功")
    private Integer shareProfitStatus = Constants.no;

    @ApiModelProperty(value = "游戏报表状态：0:失败，1：成功")
    private Integer gameRecordStatus = Constants.no;

}