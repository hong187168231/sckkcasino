package com.qianyi.casinocore.util;

import com.qianyi.modulecommon.Constants;

import java.util.ArrayList;
import java.util.List;

public class DataConst {
    public static final List<String> platforms = new ArrayList<>();

    static {
        platforms.add(Constants.PLATFORM_WM_BIG);
        platforms.add(Constants.PLATFORM_PG);
        platforms.add(Constants.PLATFORM_CQ9);
        platforms.add(Constants.PLATFORM_OBDJ);
        platforms.add(Constants.PLATFORM_OBTY);
        platforms.add(Constants.PLATFORM_OBZR);
        platforms.add(Constants.PLATFORM_SABASPORT);
        platforms.add(Constants.PLATFORM_AE);
        platforms.add(Constants.PLATFORM_VNC);
        platforms.add(Constants.PLATFORM_DMC);
        platforms.add(Constants.PLATFORM_DG);
    }

    public static final List<String> platformsReport = new ArrayList<>();

    static {
        platformsReport.add(Constants.PLATFORM_WM_BIG);
        platformsReport.add(Constants.PLATFORM_PG);
        platformsReport.add(Constants.PLATFORM_CQ9);
        platformsReport.add(Constants.PLATFORM_OBDJ);
        platformsReport.add(Constants.PLATFORM_OBTY);
        platformsReport.add(Constants.PLATFORM_OBZR);
        platformsReport.add(Constants.PLATFORM_SABASPORT);
        platformsReport.add(Constants.PLATFORM_AE_HORSEBOOK);
        platformsReport.add(Constants.PLATFORM_AE_SV388);
        platformsReport.add(Constants.PLATFORM_AE_E1SPORT);
        platformsReport.add(Constants.PLATFORM_VNC);
//        platformsReport.add(Constants.PLATFORM_DMC);
        platformsReport.add(Constants.PLATFORM_DG);
    }
}