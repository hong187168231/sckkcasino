package com.qianyi.casinocore.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class GameRecordDMCVo implements Serializable {

    private String referenceNumber;
    private String customerId;
    private String userName;
    private String enterpriseId;
    private String companyName;
    private String commission;
    private String numberPattern;
    private String bigBetAmount;
    private String smallBetAmount;
    private String betType;
    private String betDate;
    private String gameId;
    private String gameName;
    private String gameDate;
    private String slaveLotteryNumber;
    private String slaveAmount;
    private String betSize;
    private String slaveStatus;
    private String slaveNetAmount;
    private String lotterySlaveStatus;
    private String winningAmount;
    private String prizeType;
}
