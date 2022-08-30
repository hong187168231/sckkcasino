package com.qianyi.liveae.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.liveae.constants.AeConfig;
import com.qianyi.liveae.utils.GenerateSignUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.HttpClient4Util;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * AE https://allwecan.info/api-doc/multi-wallet/PRD#operation/Login
 */
@Component
@Slf4j
public class PublicAeApi {

    @Autowired
    private AeConfig aeConfig;

    public static final String SUCCESS_CODE = "0000";


    /**
     * 创建玩家
     *
     * @param userId
     * @param userName
     * @param language
     * @return
     */
    public JSONObject createMember(String userId, String userName, String language) {
        Map<String, Object> params = getCommonParams();
        params.put("userId", userId);
        params.put("betLimit", getBetLimit());
        if (!ObjectUtils.isEmpty(userName)) {
            params.put("userName", userName);
        }
        if (!ObjectUtils.isEmpty(language)) {
            params.put("language", language);
        }
        log.info("AE创建玩家账号参数{}", params);
        String url = aeConfig.getApiUrl() + "/wallet/createMember";
        String result = HttpClient4Util.doPost(url, params);
        log.info("AE创建玩家账号结果{}", result);
        return analysisResult(result);
    }

    /**
     * 登录
     *
     * @param isMobileLogin true 行动设备登入 false 桌面设备登入
     * @param externalURL   导回您指定的网站，
     * @param platform      可选功能。指定进入游戏大厅时的分页
     * @param gameType      可选功能。指定进入游戏大厅时的分页
     * @param gameForbidden
     * @param language
     * @param betLimit
     * @param autoBetMode   1 (预设) (显示前台 "自动下注"功能)
     * @return
     */
    public JSONObject login(String userId, Long isMobileLogin, String externalURL, String platform, String gameType, String gameForbidden, String language, String betLimit, String autoBetMode) {
        Map<String, Object> params = getCommonParams();
        params.put("userId", userId);
        params.put("betLimit", getBetLimit());
        if (!ObjectUtils.isEmpty(isMobileLogin)) {
            params.put("isMobileLogin", isMobileLogin);
        }
        if (!ObjectUtils.isEmpty(externalURL)) {
            params.put("externalURL", externalURL);
        }
        if (!ObjectUtils.isEmpty(platform)) {
            params.put("platform", platform);
        }
        if (!ObjectUtils.isEmpty(gameType)) {
            params.put("gameType", gameType);
        }
        if (!ObjectUtils.isEmpty(gameForbidden)) {
            params.put("gameForbidden", gameForbidden);
        }
        if (!ObjectUtils.isEmpty(language)) {
            params.put("language", language);
        }
        if (!ObjectUtils.isEmpty(betLimit)) {
            params.put("betLimit", betLimit);
        }
        if (!ObjectUtils.isEmpty(autoBetMode)) {
            params.put("autoBetMode", autoBetMode);
        }
        log.info("AE玩家登录参数{}", params);
        String url = aeConfig.getApiUrl() + "/wallet/login";
        String result = HttpClient4Util.doPost(url, params);
        log.info("AE玩家登录结果{}", result);
        return analysisResult(result);
    }

    /**
     * doLoginAndLaunchGame 登入并进入游戏
     *
     * @param isMobileLogin
     * @param externalURL
     * @param platform
     * @param gameType
     * @param gameCode
     * @param language
     * @param hall          Hall Type 厅别类型 SEXY 性感厅 ASIA 亞洲厅 COSPLAY 主题厅
     * @param betLimit
     * @param autoBetMode
     * @return
     */
    public JSONObject doLoginAndLaunchGame(String userId, Long isMobileLogin, String externalURL, String platform, String gameType, String gameCode, String language, String hall, String betLimit, String autoBetMode) {
        Map<String, Object> params = getCommonParams();
        params.put("userId", userId);
        if (!ObjectUtils.isEmpty(isMobileLogin)) {
            params.put("isMobileLogin", isMobileLogin);
        }
        if (!ObjectUtils.isEmpty(externalURL)) {
            params.put("externalURL", externalURL);
        }
        params.put("platform", platform);
        params.put("gameType", gameType);
        params.put("gameCode", gameCode);
        Map<String, Object> betLimitMap = new HashMap<>();
        if (Constants.PLATFORM_AE_HORSEBOOK.equals(platform)) {
            betLimitMap.put(Constants.PLATFORM_AE_HORSEBOOK, getHorseBookBetLimit());
            params.put("betLimit", JSON.toJSONString(betLimitMap));
        } else if (Constants.PLATFORM_AE_SV388.equals(platform)) {
            betLimitMap.put(Constants.PLATFORM_AE_SV388, getSV388BetLimit());
            params.put("betLimit", JSON.toJSONString(betLimitMap));
        }
        if (!ObjectUtils.isEmpty(language)) {
            params.put("language", language);
        }
        if (!ObjectUtils.isEmpty(betLimit)) {
            params.put("betLimit", betLimit);
        }
        if (!ObjectUtils.isEmpty(hall)) {
            params.put("hall", hall);
        }
        if (!ObjectUtils.isEmpty(autoBetMode)) {
            params.put("autoBetMode", autoBetMode);
        }
        log.info("AE登入并进入游戏参数{}", params);
        String url = aeConfig.getApiUrl() + "/wallet/doLoginAndLaunchGame";
        String result = HttpClient4Util.doPost(url, params);
        log.info("AE登入并进入游戏结果{}", result);
        return analysisResult(result);
    }

    /**
     * 强迫登出玩家
     *
     * @param userIds 若有多个用户 ID，需用逗号隔开
     * @return
     */
    public JSONObject logout(String userIds) {
        Map<String, Object> params = getCommonParams();
        params.put("userIds", userIds);
        log.info("AE登出玩家参数{}", params);
        String url = aeConfig.getApiUrl() + "/wallet/logout";
        String result = HttpClient4Util.doPost(url, params);
        log.info("AE登出玩家结果{}", result);
        return analysisResult(result);
    }

    /**
     * 查余额
     *
     * @param userIds
     * @param alluser         1 : 回传所有玩家余额 0 : 回传參數內(userids)玩家余额
     * @param isFilterBalance 1 : 回传余额>0的资料 0 : 回传所有玩家的余额资料>=0
     * @return
     */
    public JSONObject getBalance(String userIds, Integer alluser, Integer isFilterBalance) {
        Map<String, Object> params = getCommonParams();
        if (!ObjectUtils.isEmpty(userIds)) {
            params.put("userIds", userIds);
        }
        if (!ObjectUtils.isEmpty(alluser)) {
            params.put("alluser", alluser);
        }
        if (!ObjectUtils.isEmpty(userIds)) {
            params.put("isFilterBalance", isFilterBalance);
        }
        log.info("AE查询玩家余额参数{}", params);
        String url = aeConfig.getApiUrl() + "/wallet/getBalance";
        String result = HttpClient4Util.doPost(url, params);
        log.info("AE查询玩家余额结果{}", result);
        return analysisResult(result);
    }

    /**
     * @param userId
     * @param txCode
     * @param withdrawType   1: All 全部 (default预设值)  0: Partial部份
     * @param transferAmount
     * @return
     */
    public JSONObject withdraw(String userId, String txCode, Integer withdrawType, String transferAmount) {
        Map<String, Object> params = getCommonParams();
        params.put("userId", userId);
        params.put("txCode", txCode);
        if (!ObjectUtils.isEmpty(withdrawType)) {
            params.put("withdrawType", withdrawType);
        }
        if (!ObjectUtils.isEmpty(transferAmount)) {
            params.put("transferAmount", transferAmount);
        }
        log.info("AE玩家取款参数{}", params);
        String url = aeConfig.getApiUrl() + "/wallet/withdraw";
        String result = HttpClient4Util.doPost(url, params);
        log.info("AE玩家取款结果{}", result);
        return analysisResult(result);
    }

    /**
     * 存款
     *
     * @param userId
     * @param txCode
     * @param transferAmount
     * @return
     */
    public JSONObject deposit(String userId, String txCode, String transferAmount) {
        Map<String, Object> params = getCommonParams();
        params.put("userId", userId);
        params.put("txCode", txCode);
        params.put("transferAmount", transferAmount);
        log.info("AE玩家存款参数{}", params);
        String url = aeConfig.getApiUrl() + "/wallet/deposit";
        String result = HttpClient4Util.doPost(url, params);
        log.info("AE玩家存款结果{}", result);
        return analysisResult(result);
    }

    /**
     * 捞取所有账目
     * 最多只能拉取发送时间回推 24 小时内的资料
     * 一次最多可拉 2,000 笔资料
     * Platform 为必填值，API 最快支持 20 秒呼叫一次
     * 捞取资料依交易注单更新时间排序
     * 我方回应格式使用 Content-Encoding: gzip
     * 请接续上次拉账最后一笔「交易更新时间」为搜寻起始时间
     * 注意：若某次取值无资料 或 无更新资料，则将下次取值 timeFrom 设为现在时间的前一分钟
     *
     * @param timeFrom
     * @param platform
     * @param status
     * @param currency
     * @param gameType
     * @param gameCode
     * @return
     */
    public JSONObject getTransactionByUpdateDate(String timeFrom, String platform, Integer status, String currency, String gameType, String gameCode) {
        Map<String, Object> params = getCommonParams();
        params.put("timeFrom", timeFrom);
        params.put("platform", platform);
        if (!ObjectUtils.isEmpty(status)) {
            params.put("status", status);
        }
        if (!ObjectUtils.isEmpty(currency)) {
            params.put("currency", currency);
        }
        if (!ObjectUtils.isEmpty(gameType)) {
            params.put("gameType", gameType);
        }
        if (!ObjectUtils.isEmpty(gameCode)) {
            params.put("gameCode", gameCode);
        }
        log.info("AE查询注单参数{}", params);
        String url = aeConfig.getApiUrl() + "/fetch/gzip/getTransactionByUpdateDate";
        String result = HttpClient4Util.doPost(url, params);
        log.info("AE查询注单结果{}", result);
        return analysisResult(result);
    }

    /**
     * 查询转账记录
     *
     * @param txCode
     * @return
     */
    public JSONObject checkTransferOperation(String txCode) {
        Map<String, Object> params = getCommonParams();
        params.put("txCode", txCode);
        log.info("AE查询转账记录参数{}", params);
        String url = aeConfig.getApiUrl() + "/wallet/checkTransferOperation";
        String result = HttpClient4Util.doPost(url, params);
        log.info("AE查询转账记录结果{}", result);
        return analysisResult(result);
    }

    private Map<String, Object> getCommonParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("cert", aeConfig.getCert());
        params.put("agentId", aeConfig.getAgentId());
        params.put("currency", aeConfig.getCurrency());
        return params;
    }

    /**
     * 最小及最大下注限制 minbet/maxbet
     * 下注WIN跟PLC總和各的上限值 maxBetSumPerHorse
     * major = 香港赛事，新加坡赛事，马来西亚赛事
     * minor = 其他赛事
     * <p>
     * 最小及最大下注限制maxbet/minbet
     * TIE的最小及最大下注限制mindraw/maxdraw
     * 該局總和下注限制matchlimit
     */
    public String getBetLimit() {
        Map<String, Object> betLimit = new HashMap<>();
        betLimit.put(Constants.PLATFORM_AE_HORSEBOOK, getHorseBookBetLimit());
        betLimit.put(Constants.PLATFORM_AE_SV388, getSV388BetLimit());
        return JSON.toJSONString(betLimit);
    }

    public Map<String, Object> getHorseBookBetLimit() {
        Map<String, Object> HORSEBOOK = new HashMap<>();
        Map<String, Object> HORSEBOOK_LIVE = new HashMap<>();
        HORSEBOOK_LIVE.put("minbet", aeConfig.getHORSEBOOK().getMinbet());
        HORSEBOOK_LIVE.put("maxbet", aeConfig.getHORSEBOOK().getMaxbet());
        HORSEBOOK_LIVE.put("maxBetSumPerHorse", aeConfig.getHORSEBOOK().getMaxBetSumPerHorse());
        HORSEBOOK_LIVE.put("minorMinbet", aeConfig.getHORSEBOOK().getMinorMinbet());
        HORSEBOOK_LIVE.put("minorMaxbet", aeConfig.getHORSEBOOK().getMinorMaxbet());
        HORSEBOOK_LIVE.put("minorMaxBetSumPerHorse", aeConfig.getHORSEBOOK().getMinorMaxBetSumPerHorse());
        HORSEBOOK.put("LIVE", HORSEBOOK_LIVE);
        return HORSEBOOK;
    }

    public Map<String, Object> getSV388BetLimit() {
        Map<String, Object> SV388 = new HashMap<>();
        Map<String, Object> SV388_LIVE = new HashMap<>();
        SV388_LIVE.put("minbet", aeConfig.getSV388().getMinbet());
        SV388_LIVE.put("maxbet", aeConfig.getSV388().getMaxbet());
        SV388_LIVE.put("mindraw", aeConfig.getSV388().getMindraw());
        SV388_LIVE.put("maxdraw", aeConfig.getSV388().getMaxdraw());
        SV388_LIVE.put("matchlimit", aeConfig.getSV388().getMatchlimit());
        SV388.put("LIVE", SV388_LIVE);
        return SV388;
    }

    private JSONObject analysisResult(String result) {
        if (ObjectUtils.isEmpty(result)) {
            return null;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
            return jsonObject;
        } catch (Exception e) {
            log.error("解析AE据时出错，result={},msg={}", result, e.getMessage());
        }
        return null;
    }
}
