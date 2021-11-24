package com.qianyi.casinocore.util;

public final class TaskConst {
    /**
     * 每三分钟一次
     */
    public final static String BACK_ORDER = "0 0/3 * * * ? ";
    /**
     * 每日凌晨一点
     */
    public final static String HOME_PAGE_REPORT = "0 0 01 * * ?";
    /**
     * 每日零点五十分
     */
    public final static String PROXY_HOME_PAGE_REPORT = "0 50 0 * * ?";

    /**
     * 每日零点四十分
     */
    public final static String USER_RUNNING_WATER = "0 40 0 * * ?";
}
