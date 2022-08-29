package com.qianyi.liveae.api;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.liveae.utils.GenerateSignUtil;
import com.qianyi.modulecommon.util.HttpClient4Util;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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

    @Value("${project.ae.cert:null}")
    private String cert;
    @Value("${project.ae.agentId:null}")
    private String agentId;
    @Value("${project.ae.currency:null}")
    private String currency;
    @Value("${project.ae.apiUrl:null}")
    private String apiUrl;

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
        /**
         * 最小及最大下注限制 minbet/maxbet
         * 下注WIN跟PLC總和各的上限值 maxBetSumPerHorse
         * major = 香港赛事，新加坡赛事，马来西亚赛事
         * minor = 其他赛事
         *
         * 最小及最大下注限制maxbet/minbet
         * TIE的最小及最大下注限制mindraw/maxdraw
         * 該局總和下注限制matchlimit
         */
        params.put("betLimit", "{\"HORSEBOOK\":{\"LIVE\":{\"minbet\":1,\"maxbet\":50,\"maxBetSumPerHorse\":500,\"minorMinbet\":1, \"minorMaxbet\":50, \"minorMaxBetSumPerHorse\":500}},\n" +
                "\"SV388\":{\"LIVE\":{\"maxbet\":50,\"minbet\":1,\"mindraw\":1,\"matchlimit\":1000,\"maxdraw\":100}}\n" +
                "}");
        if (!ObjectUtils.isEmpty(userName)) {
            params.put("userName", userName);
        }
        if (!ObjectUtils.isEmpty(language)) {
            params.put("language", language);
        }
        log.info("AE创建玩家账号参数{}", params);
        String url = apiUrl + "/wallet/createMember";
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
        String url = apiUrl + "/wallet/login";
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
        String url = apiUrl + "/wallet/doLoginAndLaunchGame";
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
        String url = apiUrl + "/wallet/logout";
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
        String url = apiUrl + "/wallet/getBalance";
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
        String url = apiUrl + "/wallet/withdraw";
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
        String url = apiUrl + "/wallet/deposit";
        String result = HttpClient4Util.doPost(url, params);
        log.info("AE玩家存款结果{}", result);
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
        String url = apiUrl + "/wallet/checkTransferOperation";
        String result = HttpClient4Util.doPost(url, params);
        log.info("AE查询转账记录结果{}", result);
        return analysisResult(result);
    }

    private Map<String, Object> getCommonParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("cert", "DlmZxZMoiXeIesH87rY");
        params.put("agentId", "kkcasino");
        params.put("currency", "USD");
        return params;
    }

    private JSONObject analysisResult(String result){
        if (ObjectUtils.isEmpty(result)) {
            return null;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
            return jsonObject;
        } catch (Exception e) {
            log.error("解析AE据时出错，result={},msg={}", result,e.getMessage());
        }
        return null;
    }
}
