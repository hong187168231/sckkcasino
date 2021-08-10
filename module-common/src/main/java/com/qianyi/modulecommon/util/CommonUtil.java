package com.qianyi.modulecommon.util;

import java.util.Random;

public class CommonUtil {

    //返回num个随机数
    public static String random(int num) {

        String randomStr = "";
        Random random = new Random();
        for (int i = 0; i < num; i++) {
            //使用nextInt(int count)获得count以内的整数，不含count
            int j = random.nextInt(10);
            randomStr = randomStr + j;
        }

        return randomStr;
    }

}
