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
        map.put("data", "G");
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
        return analysisResult(resultString);
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
