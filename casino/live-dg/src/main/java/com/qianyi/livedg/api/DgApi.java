package com.qianyi.livedg.api;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.livedg.util.RandomUtil;
import com.qianyi.modulecommon.config.LocaleConfig;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import com.qianyi.livedg.util.HttpClient4Util;
import java.math.BigDecimal;
import java.util.*;


@Component
@Slf4j
public class DgApi {


    @Value("${project.dg.apiUrl:null}")
    private String apiUrl;

    @Value("${project.dg.agentName:null}")
    private String agentName;

    @Value("${project.dg.apiKey:null}")
    private String apiKey;
    @Value("${project.dg.currencyName:null}")
    private String currencyName;

    public static final String SUCCESS_CODE = "0000";

    /**
     * 注册新会员
     * @return
     */
    public JSONObject createDgMemberGame(String username,String password) throws Exception {
        JSONObject map = new JSONObject();
        Integer random = RandomUtil.getRandomOne(6);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(agentName).append(apiKey).append(random);
        String sign = DigestUtils.md5Hex(stringBuilder.toString());
        map.put("token", sign);
        map.put("random", random);
        map.put("data", "A");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        jsonObject.put("password", password);
        jsonObject.put("currencyName", currencyName);
        jsonObject.put("winLimit", "0");
        map.put("member", jsonObject);
        StringBuilder apiUrlStr = new StringBuilder();
        apiUrlStr.append(apiUrl).append("/user/signup/").append(agentName);
        log.info("DG调用API注册新会员输入,apiUrlStr：{},map：{},apiKey：{}", apiUrlStr, map, apiKey);
        String resultString = HttpClient4Util.doPost(apiUrlStr.toString(),map);
        log.info("DG调用API注册新会员返回,result：{}", resultString);
        return analysisResult(resultString);

    }


    /**
     * 会员登入
     * @return
     */
    public JSONObject loginDgGame(String username,String lang) throws Exception {
        JSONObject map = new JSONObject();
        Integer random = RandomUtil.getRandomOne(6);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(agentName).append(apiKey).append(random);
        String sign = DigestUtils.md5Hex(stringBuilder.toString());
        map.put("random", random);
        map.put("token", sign);
        map.put("lang", lang);
        map.put("domains", "1");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        map.put("member", jsonObject);
        StringBuilder apiUrlStr = new StringBuilder();
        apiUrlStr.append(apiUrl).append("/user/login/").append(agentName);
        log.info("DG调用API会员登入输入,apiUrlStr：{},map：{},apiKey：{}", apiUrlStr, map, apiKey);
        String resultString = HttpClient4Util.doPost(apiUrlStr.toString(),map);
        log.info("DG调用API会员登入返回,result：{}", resultString);
        return analysisResult(resultString);

//        if (null != dgApiResponseData && "0".equals(dgApiResponseData.getString("codeId"))) {
//            StringBuilder builder = new StringBuilder();
//            builder.append(dgApiResponseData.getJSONArray("list").get(0)).append(dgApiResponseData.getString("token"));
//            builder.append("&language=").append(lang);
//            //登录
//            return builder.toString();
//        }else if(null == dgApiResponseData){
//            return throw
//        }else {
//            return errorCode(dgApiResponseData.getIntValue("codeId"), dgApiResponseData.getString("random"), countryCode);
//        }
    }

    /**
     * 会员试玩登入
     * @return
     */
    public JSONObject loginDgGameFree(String username, String lang) throws Exception {

        JSONObject map = new JSONObject();
        Integer random = RandomUtil.getRandomOne(6);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(agentName).append(apiKey).append(random);
        String sign = DigestUtils.md5Hex(stringBuilder.toString());
        map.put("random", random);
        map.put("token", sign);
        map.put("lang", lang);
        map.put("domains", "1");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        map.put("member", jsonObject);
        StringBuilder apiUrlStr = new StringBuilder();
        apiUrlStr.append(apiUrl).append("/user/free/").append(agentName);
        log.info("DG调用API会员试玩登入输入,apiUrlStr：{},map：{},apiKey：{}", apiUrlStr, map, apiKey);
        String resultString = HttpClient4Util.doPost(apiUrlStr.toString(),map);
        log.info("DG调用API会员试玩登入返回,result：{}", resultString);
        return analysisResult(resultString);

    }
    /**
     * 修改会员信息
     * @return
     */
    public JSONObject updateUser(String username, String password,BigDecimal winLimit,int status) throws Exception {
        JSONObject map = new JSONObject();
        Integer random = RandomUtil.getRandomOne(6);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(agentName).append(apiKey).append(random);
        String sign = DigestUtils.md5Hex(stringBuilder.toString());
        map.put("random", random);
        map.put("token", sign);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        jsonObject.put("password", password);
        jsonObject.put("winLimit", winLimit);
        jsonObject.put("status", status);
        map.put("member", jsonObject);
        StringBuilder apiUrlStr = new StringBuilder();
        apiUrlStr.append(apiUrl).append("/user/update/").append(agentName);
        log.info("DG调用API修改会员信息输入,apiUrlStr：{},map：{},apiKey：{}", apiUrlStr, map, apiKey);
        String resultString = HttpClient4Util.doPost(apiUrlStr.toString(),map);
        log.info("DG调用API修改会员信息返回,result：{}", resultString);
        return analysisResult(resultString);

    }

    /**
     * 获取会员余额
     *
     * @param username
     * @return
     */
    public JSONObject fetchWalletBalance(String username) throws Exception {
        JSONObject map = new JSONObject();
        Integer random = RandomUtil.getRandomOne(6);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(agentName).append(apiKey).append(random);
        String sign = DigestUtils.md5Hex(stringBuilder.toString());
        map.put("random", random);
        map.put("token", sign);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        map.put("member", jsonObject);
        StringBuilder apiUrlStr = new StringBuilder();
        apiUrlStr.append(apiUrl).append("/user/getBalance/").append(agentName);
        log.info("DG调用API获取会员余额输入,apiUrlStr：{},map：{},apiKey：{}", apiUrlStr, map, apiKey);
        String resultString = HttpClient4Util.doPost(apiUrlStr.toString(),map);
        log.info("DG调用API获取会员余额返回,result：{}", resultString);
        return analysisResult(resultString);
    }

    /**
     * 会员存取款
     * @param username
     * @param amount 为存取款金额，正数存款负数取款，请确保保留不超过2位小数，否则将收到错误码11
     * @return
     */
    public JSONObject transterWallet(String username, BigDecimal amount,String serialNumber) throws Exception {
        JSONObject map = new JSONObject();
        Integer random = RandomUtil.getRandomOne(6);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(agentName).append(apiKey).append(random);
        String sign = DigestUtils.md5Hex(stringBuilder.toString());
        map.put("random", random);
        map.put("token", sign);
        map.put("data", serialNumber);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        jsonObject.put("amount", amount);
        map.put("member", jsonObject);
        StringBuilder apiUrlStr = new StringBuilder();
        apiUrlStr.append(apiUrl).append("/account/transfer/").append(agentName);
        log.info("DG调用API会员存取款输入,apiUrlStr：{},map：{},apiKey：{}", apiUrlStr, map, apiKey);
        String resultString = HttpClient4Util.doPost(apiUrlStr.toString(),map);
        log.info("DG调用API会员存取款返回,result：{}", resultString);
        return analysisResult(resultString);

    }

    /**
     * 检查存取款操作是否成功
     * @return
     */
    public JSONObject checkTransfer(String serialNumber) throws Exception {
        JSONObject map = new JSONObject();
        Integer random = RandomUtil.getRandomOne(6);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(agentName).append(apiKey).append(random);
        String sign = DigestUtils.md5Hex(stringBuilder.toString());
        map.put("random", random);
        map.put("token", sign);
        map.put("data", serialNumber);//"转账流水号"
        StringBuilder apiUrlStr = new StringBuilder();
        apiUrlStr.append(apiUrl).append("/account/checkTransfer/").append(agentName);
        log.info("DG调用API检查存取款操作是否成功输入,apiUrlStr：{},map：{},apiKey：{}", apiUrlStr, map, apiKey);
        String resultString = HttpClient4Util.doPost(apiUrlStr.toString(),map);
        log.info("DG调用API检查存取款操作是否成功返回,result：{}", resultString);
        return analysisResult(resultString);

    }

    /**
     * 修改会员限红组
     *
     * @param username
     * @return
     */
    public JSONObject updateLimit(String username,String data) throws Exception {
        JSONObject map = new JSONObject();
        Integer random = RandomUtil.getRandomOne(6);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(agentName).append(apiKey).append(random);
        String sign = DigestUtils.md5Hex(stringBuilder.toString());
        map.put("random", random);
        map.put("token", sign);
        map.put("data", data);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        map.put("member", jsonObject);
        StringBuilder apiUrlStr = new StringBuilder();
        apiUrlStr.append(apiUrl).append("/game/updateLimit/").append(agentName);
        log.info("DG调用API修改会员限红组输入,apiUrlStr：{},map：{},apiKey：{}", apiUrlStr, map, apiKey);
        String resultString = HttpClient4Util.doPost(apiUrlStr.toString(),map);
        log.info("DG调用API修改会员限红组返回,result：{}", resultString);
        return analysisResult(resultString);
    }

    /**
     * 抓取注单报表
     * @return
     */
    public JSONObject getReport() throws Exception {
        JSONObject map = new JSONObject();
        Integer random = RandomUtil.getRandomOne(6);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(agentName).append(apiKey).append(random);
        String sign = DigestUtils.md5Hex(stringBuilder.toString());
        map.put("random", random);
        map.put("token", sign);
        StringBuilder apiUrlStr = new StringBuilder();
        apiUrlStr.append(apiUrl).append("/game/getReport/").append(agentName);
        log.info("DG调用API抓取注单报表输入,apiUrlStr：{},map：{},apiKey：{}", apiUrlStr, map, apiKey);
        String resultString = HttpClient4Util.doPost(apiUrlStr.toString(),map);
        log.info("DG调用API抓取注单报表返回,result：{}", resultString);
        return analysisResult("{\"codeId\":0,\"token\":\"04101d06027b80636f0b2577cc95d996\",\"random\":\"916623\",\"list\":[{\"id\":13466500070,\"tableId\":10103,\"shoeId\":35,\"playId\":25,\"lobbyId\":1,\"gameType\":1,\"gameId\":1,\"memberId\":93670590,\"parentId\":23694,\"betTime\":\"2022-12-31 22:07:57\",\"calTime\":\"2022-12-31 22:08:13\",\"winOrLoss\":0.0,\"balanceBefore\":448.0,\"betPoints\":10.0,\"betPointsz\":0.0,\"availableBet\":10.0,\"userName\":\"CESHIGEZI\",\"result\":\"{\\\"result\\\":\\\"5,1,8\\\",\\\"poker\\\":{\\\"banker\\\":\\\"27-43-0\\\",\\\"player\\\":\\\"42-44-0\\\"}}\",\"betDetail\":\"{\\\"banker\\\":10.0}\",\"ip\":\"103.139.16.132\",\"ext\":\"221231B031830\",\"isRevocation\":1,\"currencyId\":2,\"deviceType\":1,\"roadid\":0,\"pluginid\":0},{\"id\":13466503616,\"tableId\":10103,\"shoeId\":35,\"playId\":26,\"lobbyId\":1,\"gameType\":1,\"gameId\":1,\"memberId\":93670590,\"parentId\":23694,\"betTime\":\"2022-12-31 22:08:23\",\"calTime\":\"2022-12-31 22:08:40\",\"winOrLoss\":19.5,\"balanceBefore\":438.0,\"betPoints\":10.0,\"betPointsz\":0.0,\"availableBet\":9.5,\"userName\":\"CESHIGEZI\",\"result\":\"{\\\"result\\\":\\\"1,1,8\\\",\\\"poker\\\":{\\\"banker\\\":\\\"8-52-0\\\",\\\"player\\\":\\\"51-1-0\\\"}}\",\"betDetail\":\"{\\\"banker\\\":10.0,\\\"bankerW\\\":19.5}\",\"ip\":\"103.139.16.132\",\"ext\":\"221231B031831\",\"isRevocation\":1,\"currencyId\":2,\"deviceType\":1,\"roadid\":0,\"pluginid\":0},{\"id\":13466513395,\"tableId\":10103,\"shoeId\":35,\"playId\":28,\"lobbyId\":1,\"gameType\":1,\"gameId\":1,\"memberId\":93670590,\"parentId\":23694,\"betTime\":\"2022-12-31 22:09:21\",\"calTime\":\"2022-12-31 22:09:41\",\"winOrLoss\":10.0,\"balanceBefore\":447.5,\"betPoints\":10.0,\"betPointsz\":0.0,\"availableBet\":0.0,\"userName\":\"CESHIGEZI\",\"result\":\"{\\\"result\\\":\\\"9,2,6\\\",\\\"poker\\\":{\\\"banker\\\":\\\"45-38-0\\\",\\\"player\\\":\\\"4-22-3\\\"}}\",\"betDetail\":\"{\\\"banker\\\":10.0,\\\"bankerW\\\":10.0}\",\"ip\":\"103.139.16.132\",\"ext\":\"221231B031833\",\"isRevocation\":1,\"currencyId\":2,\"deviceType\":1,\"roadid\":0,\"pluginid\":0},{\"id\":13466661385,\"tableId\":10109,\"shoeId\":30,\"playId\":28,\"lobbyId\":1,\"gameType\":1,\"gameId\":1,\"memberId\":93671064,\"parentId\":23694,\"betTime\":\"2022-12-31 22:25:24\",\"calTime\":\"2022-12-31 22:25:37\",\"winOrLoss\":0.0,\"balanceBefore\":169.0,\"betPoints\":5.0,\"betPointsz\":0.0,\"availableBet\":5.0,\"userName\":\"NARITHX114\",\"result\":\"{\\\"result\\\":\\\"5,1,8\\\",\\\"poker\\\":{\\\"banker\\\":\\\"2-24-0\\\",\\\"player\\\":\\\"47-13-0\\\"}}\",\"betDetail\":\"{\\\"banker\\\":5.0}\",\"ip\":\"117.20.116.201\",\"ext\":\"221231B091568\",\"isRevocation\":1,\"currencyId\":2,\"deviceType\":5,\"roadid\":0,\"pluginid\":0},{\"id\":13466665557,\"tableId\":10109,\"shoeId\":30,\"playId\":29,\"lobbyId\":1,\"gameType\":1,\"gameId\":1,\"memberId\":93671064,\"parentId\":23694,\"betTime\":\"2022-12-31 22:25:55\",\"calTime\":\"2022-12-31 22:26:23\",\"winOrLoss\":12.0,\"balanceBefore\":164.0,\"betPoints\":6.0,\"betPointsz\":0.0,\"availableBet\":6.0,\"userName\":\"NARITHX114\",\"result\":\"{\\\"result\\\":\\\"5,2,5\\\",\\\"poker\\\":{\\\"banker\\\":\\\"8-31-51\\\",\\\"player\\\":\\\"20-42-18\\\"}}\",\"betDetail\":\"{\\\"player\\\":6.0,\\\"playerW\\\":12.0}\",\"ip\":\"117.20.116.201\",\"ext\":\"221231B091569\",\"isRevocation\":1,\"currencyId\":2,\"deviceType\":5,\"roadid\":0,\"pluginid\":0},{\"id\":13466674862,\"tableId\":10109,\"shoeId\":30,\"playId\":30,\"lobbyId\":1,\"gameType\":1,\"gameId\":1,\"memberId\":93671064,\"parentId\":23694,\"betTime\":\"2022-12-31 22:26:46\",\"calTime\":\"2022-12-31 22:27:03\",\"winOrLoss\":0.0,\"balanceBefore\":170.0,\"betPoints\":5.0,\"betPointsz\":0.0,\"availableBet\":5.0,\"userName\":\"NARITHX114\",\"result\":\"{\\\"result\\\":\\\"2,1,8\\\",\\\"poker\\\":{\\\"banker\\\":\\\"43-30-0\\\",\\\"player\\\":\\\"24-52-0\\\"}}\",\"betDetail\":\"{\\\"player\\\":5.0}\",\"ip\":\"117.20.116.201\",\"ext\":\"221231B091570\",\"isRevocation\":1,\"currencyId\":2,\"deviceType\":5,\"roadid\":0,\"pluginid\":0},{\"id\":13466875924,\"tableId\":10103,\"shoeId\":36,\"playId\":29,\"lobbyId\":1,\"gameType\":1,\"gameId\":1,\"memberId\":93670590,\"parentId\":23694,\"betTime\":\"2022-12-31 22:48:22\",\"calTime\":\"2022-12-31 22:48:48\",\"winOrLoss\":10.0,\"balanceBefore\":447.5,\"betPoints\":10.0,\"betPointsz\":0.0,\"availableBet\":0.0,\"userName\":\"CESHIGEZI\",\"result\":\"{\\\"result\\\":\\\"9,2,0\\\",\\\"poker\\\":{\\\"banker\\\":\\\"52-49-50\\\",\\\"player\\\":\\\"1-25-48\\\"}}\",\"betDetail\":\"{\\\"banker\\\":10.0,\\\"bankerW\\\":10.0}\",\"ip\":\"103.139.16.132\",\"ext\":\"221231B031891\",\"isRevocation\":1,\"currencyId\":2,\"deviceType\":1,\"roadid\":0,\"pluginid\":0},{\"id\":13466881831,\"tableId\":10103,\"shoeId\":36,\"playId\":30,\"lobbyId\":1,\"gameType\":1,\"gameId\":1,\"memberId\":93670590,\"parentId\":23694,\"betTime\":\"2022-12-31 22:49:02\",\"calTime\":\"2022-12-31 22:49:18\",\"winOrLoss\":0.0,\"balanceBefore\":447.5,\"betPoints\":10.0,\"betPointsz\":0.0,\"availableBet\":10.0,\"userName\":\"CESHIGEZI\",\"result\":\"{\\\"result\\\":\\\"7,1,8\\\",\\\"poker\\\":{\\\"banker\\\":\\\"3-38-0\\\",\\\"player\\\":\\\"35-22-0\\\"}}\",\"betDetail\":\"{\\\"banker\\\":10.0}\",\"ip\":\"103.139.16.132\",\"ext\":\"221231B031892\",\"isRevocation\":1,\"currencyId\":2,\"deviceType\":1,\"roadid\":0,\"pluginid\":0},{\"id\":13467108953,\"tableId\":10106,\"shoeId\":29,\"playId\":52,\"lobbyId\":1,\"gameType\":1,\"gameId\":1,\"memberId\":93671064,\"parentId\":23694,\"betTime\":\"2022-12-31 23:13:32\",\"calTime\":\"2022-12-31 23:13:58\",\"winOrLoss\":30.0,\"balanceBefore\":80.0,\"betPoints\":15.0,\"betPointsz\":0.0,\"availableBet\":15.0,\"userName\":\"NARITHX114\",\"result\":\"{\\\"result\\\":\\\"5,2,8\\\",\\\"poker\\\":{\\\"banker\\\":\\\"32-39-0\\\",\\\"player\\\":\\\"44-25-3\\\"}}\",\"betDetail\":\"{\\\"player\\\":15.0,\\\"playerW\\\":30.0}\",\"ip\":\"117.20.116.201\",\"ext\":\"221231B061573\",\"isRevocation\":1,\"currencyId\":2,\"deviceType\":5,\"roadid\":0,\"pluginid\":0},{\"id\":13467116553,\"tableId\":10106,\"shoeId\":29,\"playId\":53,\"lobbyId\":1,\"gameType\":1,\"gameId\":1,\"memberId\":93671064,\"parentId\":23694,\"betTime\":\"2022-12-31 23:14:15\",\"calTime\":\"2022-12-31 23:14:37\",\"winOrLoss\":0.0,\"balanceBefore\":95.0,\"betPoints\":5.0,\"betPointsz\":0.0,\"availableBet\":5.0,\"userName\":\"NARITHX114\",\"result\":\"{\\\"result\\\":\\\"1,1,7\\\",\\\"poker\\\":{\\\"banker\\\":\\\"23-46-0\\\",\\\"player\\\":\\\"45-26-0\\\"}}\",\"betDetail\":\"{\\\"player\\\":5.0}\",\"ip\":\"117.20.116.201\",\"ext\":\"221231B061574\",\"isRevocation\":1,\"currencyId\":2,\"deviceType\":5,\"roadid\":0,\"pluginid\":0},{\"id\":13467128562,\"tableId\":10106,\"shoeId\":29,\"playId\":55,\"lobbyId\":1,\"gameType\":1,\"gameId\":1,\"memberId\":93671064,\"parentId\":23694,\"betTime\":\"2022-12-31 23:15:36\",\"calTime\":\"2022-12-31 23:16:16\",\"winOrLoss\":10.0,\"balanceBefore\":90.0,\"betPoints\":5.0,\"betPointsz\":0.0,\"availableBet\":5.0,\"userName\":\"NARITHX114\",\"result\":\"{\\\"result\\\":\\\"5,2,7\\\",\\\"poker\\\":{\\\"banker\\\":\\\"37-2-41\\\",\\\"player\\\":\\\"10-46-0\\\"}}\",\"betDetail\":\"{\\\"player\\\":5.0,\\\"playerW\\\":10.0}\",\"ip\":\"117.20.116.201\",\"ext\":\"221231B061576\",\"isRevocation\":1,\"currencyId\":2,\"deviceType\":5,\"roadid\":0,\"pluginid\":0},{\"id\":13467139562,\"tableId\":10105,\"shoeId\":30,\"playId\":14,\"lobbyId\":1,\"gameType\":1,\"gameId\":1,\"memberId\":93671064,\"parentId\":23694,\"betTime\":\"2022-12-31 23:16:49\",\"calTime\":\"2022-12-31 23:17:12\",\"winOrLoss\":0.0,\"balanceBefore\":95.0,\"betPoints\":5.0,\"betPointsz\":0.0,\"availableBet\":5.0,\"userName\":\"NARITHX114\",\"result\":\"{\\\"result\\\":\\\"1,2,6\\\",\\\"poker\\\":{\\\"banker\\\":\\\"36-32-0\\\",\\\"player\\\":\\\"5-35-25\\\"}}\",\"betDetail\":\"{\\\"player\\\":5.0}\",\"ip\":\"117.20.116.201\",\"ext\":\"221231B051567\",\"isRevocation\":1,\"currencyId\":2,\"deviceType\":5,\"roadid\":0,\"pluginid\":0},{\"id\":13467147128,\"tableId\":10109,\"shoeId\":31,\"playId\":38,\"lobbyId\":1,\"gameType\":1,\"gameId\":1,\"memberId\":93671064,\"parentId\":23694,\"betTime\":\"2022-12-31 23:17:41\",\"calTime\":\"2022-12-31 23:17:58\",\"winOrLoss\":0.0,\"balanceBefore\":90.0,\"betPoints\":10.0,\"betPointsz\":0.0,\"availableBet\":10.0,\"userName\":\"NARITHX114\",\"result\":\"{\\\"result\\\":\\\"2,1,8\\\",\\\"poker\\\":{\\\"banker\\\":\\\"22-48-0\\\",\\\"player\\\":\\\"6-20-0\\\"}}\",\"betDetail\":\"{\\\"player\\\":10.0}\",\"ip\":\"117.20.116.201\",\"ext\":\"221231B091631\",\"isRevocation\":1,\"currencyId\":2,\"deviceType\":5,\"roadid\":0,\"pluginid\":0},{\"id\":13467153511,\"tableId\":10109,\"shoeId\":31,\"playId\":39,\"lobbyId\":1,\"gameType\":1,\"gameId\":1,\"memberId\":93671064,\"parentId\":23694,\"betTime\":\"2022-12-31 23:18:23\",\"calTime\":\"2022-12-31 23:18:39\",\"winOrLoss\":0.0,\"balanceBefore\":80.0,\"betPoints\":5.0,\"betPointsz\":0.0,\"availableBet\":5.0,\"userName\":\"NARITHX114\",\"result\":\"{\\\"result\\\":\\\"3,2,7\\\",\\\"poker\\\":{\\\"banker\\\":\\\"30-49-42\\\",\\\"player\\\":\\\"34-47-0\\\"}}\",\"betDetail\":\"{\\\"player\\\":5.0}\",\"ip\":\"117.20.116.201\",\"ext\":\"221231B091632\",\"isRevocation\":1,\"currencyId\":2,\"deviceType\":5,\"roadid\":0,\"pluginid\":0},{\"id\":13467158908,\"tableId\":10109,\"shoeId\":31,\"playId\":40,\"lobbyId\":1,\"gameType\":1,\"gameId\":1,\"memberId\":93671064,\"parentId\":23694,\"betTime\":\"2022-12-31 23:19:00\",\"calTime\":\"2022-12-31 23:19:16\",\"winOrLoss\":0.0,\"balanceBefore\":75.0,\"betPoints\":25.0,\"betPointsz\":0.0,\"availableBet\":25.0,\"userName\":\"NARITHX114\",\"result\":\"{\\\"result\\\":\\\"2,1,8\\\",\\\"poker\\\":{\\\"banker\\\":\\\"22-35-0\\\",\\\"player\\\":\\\"44-6-0\\\"}}\",\"betDetail\":\"{\\\"player\\\":25.0}\",\"ip\":\"117.20.116.201\",\"ext\":\"221231B091633\",\"isRevocation\":1,\"currencyId\":2,\"deviceType\":5,\"roadid\":0,\"pluginid\":0},{\"id\":13467156911,\"tableId\":10109,\"shoeId\":31,\"playId\":40,\"lobbyId\":1,\"gameType\":1,\"gameId\":1,\"memberId\":93675004,\"parentId\":23694,\"betTime\":\"2022-12-31 23:18:50\",\"calTime\":\"2022-12-31 23:19:16\",\"winOrLoss\":0.0,\"balanceBefore\":7.0,\"betPoints\":5.0,\"betPointsz\":0.0,\"availableBet\":5.0,\"userName\":\"NAT137\",\"result\":\"{\\\"result\\\":\\\"2,1,8\\\",\\\"poker\\\":{\\\"banker\\\":\\\"22-35-0\\\",\\\"player\\\":\\\"44-6-0\\\"}}\",\"betDetail\":\"{\\\"player\\\":5.0}\",\"ip\":\"27.109.114.132\",\"ext\":\"221231B091633\",\"isRevocation\":1,\"currencyId\":2,\"deviceType\":5,\"roadid\":0,\"pluginid\":0}]}");
    }

    /**
     *  标记已抓取注单
     * @return
     */
    public JSONObject markReport(List list) throws Exception {
        JSONObject map = new JSONObject();
        Integer random = RandomUtil.getRandomOne(6);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(agentName).append(apiKey).append(random);
        String sign = DigestUtils.md5Hex(stringBuilder.toString());
        map.put("random", random);
        map.put("token", sign);
        map.put("list", list);
        StringBuilder apiUrlStr = new StringBuilder();
        apiUrlStr.append(apiUrl).append("/game/markReport/").append(agentName);
        log.info("DG调用API标记已抓取注单输入,apiUrlStr：{},map：{},apiKey：{}", apiUrlStr, map, apiKey);
        String resultString = HttpClient4Util.doPost(apiUrlStr.toString(),map);
        log.info("DG调用API标记已抓取注单返回,result：{}", resultString);
        return analysisResult(resultString);
    }

    /**
     *  获取当前代理下在DG在线会员信息
     * @return
     */
    public JSONObject onlineReport() throws Exception {
        JSONObject map = new JSONObject();
        Integer random = RandomUtil.getRandomOne(6);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(agentName).append(apiKey).append(random);
        String sign = DigestUtils.md5Hex(stringBuilder.toString());
        map.put("random", random);
        map.put("token", sign);
        StringBuilder apiUrlStr = new StringBuilder();
        apiUrlStr.append(apiUrl).append("/user/onlineReport/").append(agentName);
        log.info("DG调用API获取当前代理下在DG在线会员信息输入,apiUrlStr：{},map：{},apiKey：{}", apiUrlStr, map, apiKey);
        String resultString = HttpClient4Util.doPost(apiUrlStr.toString(),map);
        log.info("DG调用API获取当前代理下在DG在线会员信息返回,result：{}", resultString);
        return analysisResult(resultString);
    }

    /**
     *  踢人
     * @return
     */
    public JSONObject offline(List list) throws Exception {
        JSONObject map = new JSONObject();
        Integer random = RandomUtil.getRandomOne(6);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(agentName).append(apiKey).append(random);
        String sign = DigestUtils.md5Hex(stringBuilder.toString());
        map.put("random", random);
        map.put("token", sign);
        map.put("list", list);
        StringBuilder apiUrlStr = new StringBuilder();
        apiUrlStr.append(apiUrl).append("/user/offline/").append(agentName);
        log.info("DG调用API踢人输入,apiUrlStr：{},map：{},apiKey：{}", apiUrlStr, map, apiKey);
        String resultString = HttpClient4Util.doPost(apiUrlStr.toString(),map);
        log.info("DG调用API踢人返回,result：{}", resultString);
        return analysisResult(resultString);
    }

    private JSONObject analysisResult(String result) {
        if (ObjectUtils.isEmpty(result)) {
            return null;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
            return jsonObject;
        } catch (Exception e) {
            log.error("解析DG据时出错，result={},msg={}", result, e.getMessage());
        }
        return null;
    }

    public ResponseEntity errorCode(int errorCode, String errorMessage) {
        switch (errorCode){
            case 0:	return ResponseUtil.custom("操作成功");
            case 1:	return ResponseUtil.custom("参数错误");
            case 2:	return ResponseUtil.custom("Token验证失败");
            case 4:	return ResponseUtil.custom("非法操作");
            case 10: return ResponseUtil.custom("日期格式错误");
            case 11: return ResponseUtil.custom("数据格式错误");
            case 97: return ResponseUtil.custom("没有权限");
            case 98: return ResponseUtil.custom("操作失败");
            case 99: return ResponseUtil.custom("未知错误");
            case 100: return ResponseUtil.custom("账号被锁定");
            case 101: return ResponseUtil.custom("账号格式错误");
            case 102: return ResponseUtil.custom("账号不存在");
            case 103: return ResponseUtil.custom("此账号被占用");
            case 104: return ResponseUtil.custom("密码格式错误");
            case 105: return ResponseUtil.custom("密码错误");
            case 106: return ResponseUtil.custom("新旧密码相同");
            case 107: return ResponseUtil.custom("会员账号不可用");
            case 108: return ResponseUtil.custom("登入失败");
            case 109: return ResponseUtil.custom("注册失败");
            case 113: return ResponseUtil.custom("传入的代理账号不是代理");
            case 114: return ResponseUtil.custom("找不到会员");
            case 116: return ResponseUtil.custom("账号已占用");
            case 117: return ResponseUtil.custom("找不到会员所属的分公司");
            case 118: return ResponseUtil.custom("找不到指定的代理");
            case 119: return ResponseUtil.custom("存取款操作时代理点数不足");
            case 120: return ResponseUtil.custom("余额不足");
            case 121: return ResponseUtil.custom("盈利限制必须大于或等于0");
            case 150: return ResponseUtil.custom("免费试玩账号用完");
            case 300: return ResponseUtil.custom("系统维护");
//            {
//                "codeId":300,
//                    "list":["2016-10-19 09:30:00","2016-10-19 11:00:00"] //[0]开始维护时间[1]结束维护时间
//            }
            case 301: return ResponseUtil.custom("代理账号找不到");
            case 320: return ResponseUtil.custom("API Key 错误");
            case 321: return ResponseUtil.custom("找不到相应的限红组");
            case 322: return ResponseUtil.custom("找不到指定的货币类型");
            case 323: return ResponseUtil.custom("转账流水号占用");
            case 324: return ResponseUtil.custom("转账失败");
            case 325: return ResponseUtil.custom("代理状态不可用");
            case 326: return ResponseUtil.custom("会员代理没有视频组");
            case 328: return ResponseUtil.custom("API 类型找不到");
            case 329: return ResponseUtil.custom("会员代理信息不完整");
            case 400: return ResponseUtil.custom("客户端IP 受限");
            case 401: return ResponseUtil.custom("网络延迟");
            case 402: return ResponseUtil.custom("连接关闭");
            case 403: return ResponseUtil.custom("客户端来源受限");
            case 404: return ResponseUtil.custom("请求的资源不存在");
            case 405: return ResponseUtil.custom("请求太频繁");
            case 406: return ResponseUtil.custom("请求超时");
            case 407: return ResponseUtil.custom("找不到游戏地址");
            case 500: return ResponseUtil.custom("空指针异常");
            case 501: return ResponseUtil.custom("系统异常");
            case 502: return ResponseUtil.custom("系统忙");
            case 503: return ResponseUtil.custom("数据操作异常");
            default:
                return ResponseUtil.custom("系统异常");
        }
    }
}
