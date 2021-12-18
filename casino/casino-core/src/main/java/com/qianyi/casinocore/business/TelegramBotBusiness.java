package com.qianyi.casinocore.business;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.modulecommon.util.HttpClient4Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@Slf4j
public class TelegramBotBusiness {

    /**
     * https://blog.csdn.net/dodod2012/article/details/102685519
     * 发送消息到Telegram 机器人
     *
     * @param token
     * @param msg
     */
    public String sendMsgToTelegramBot(String token, String msg) {
        log.info("开始给TG机器人发送消息,token={},msg={}", token, msg);
        if (ObjectUtils.isEmpty(token) || ObjectUtils.isEmpty(msg)) {
            log.info("token或msg为空，token={},msg={}", token, msg);
            return null;
        }
        try {
            String getGroupUrl = "https://api.telegram.org/bot" + token + "/getUpdates";
            String groupResult = HttpClient4Util.get(getGroupUrl);
            if (ObjectUtils.isEmpty(groupResult)) {
                log.error("查询群组信息异常,groupResult为空");
                return null;
            }
            JSONObject jsonObject = JSONObject.parseObject(groupResult);
            if (jsonObject == null) {
                log.error("查询群组信息异常");
                return null;
            }
            log.info("token={},群组信息查询结果={}", jsonObject.toJSONString());
            if (!jsonObject.getBoolean("ok")) {
                log.error("查询群组信息异常,groupResult={}", jsonObject.toJSONString());
                return null;
            }
            JSONArray result = jsonObject.getJSONArray("result");
            if (result == null) {
                log.error("查询群组信息result为空");
                return null;
            }
            JSONObject chat = result.getJSONObject(0).getJSONObject("chat");
            if (chat == null) {
                log.error("查询群组信息chat为空");
                return null;
            }
            Long chatId = chat.getLong("id");
            if (chatId == null) {
                log.error("查询群组信息chatId为空");
                return null;
            }
            String sendMsgUrl = "https://api.telegram.org/bot" + token + "/sendMessage?chat_id=" + chatId + "&text=" + msg;
            String sendMsgUrlResult = HttpClient4Util.get(sendMsgUrl);
            log.info("token={},chatId={},msg={},result={}消息发送结果", token, chatId, msg, sendMsgUrlResult);
            if (ObjectUtils.isEmpty(sendMsgUrlResult)) {
                log.error("token={},chatId={},msg={}消息发送失败1", token, chatId, msg);
                return null;
            }
            JSONObject msgResult = JSONObject.parseObject(sendMsgUrlResult);
            if (msgResult == null) {
                log.error("token={},chatId={},msg={}消息发送失败2", token, chatId, msg);
                return null;
            }
            if (msgResult.getBoolean("ok")) {
                log.info("token={},chatId={},msg={}消息发送成功", token, chatId, msg);
                return sendMsgUrlResult;
            }
            log.error("token={},chatId={},msg={}消息发送失败3", token, chatId, msg);
        } catch (Exception e) {
            log.error("TelegramBot消息发送失败，msg={}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
