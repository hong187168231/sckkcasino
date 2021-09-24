package com.qianyi.casinoadmin.util;

import java.math.BigDecimal;

public class CommonUtil {

    public static BigDecimal checkMoney(String money){
        BigDecimal decMoney = null;
        try {
            decMoney = new BigDecimal(money);
        }catch (Exception e){
            decMoney = new BigDecimal(-1);
        }

        return decMoney;
    }
}
