package com.qianyi.modulecommon.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 国际化工具类
 */
@Component
@Slf4j
public class MessageUtil {

    @Autowired
    private MessageSource messageSource;

    /**
     * 获取单个国际化翻译值
     */
    public String get(String msgKey) {
        try {
            //Locale locale = new Locale("en", "US");
            Locale locale = LocaleContextHolder.getLocale();
            //以中文为key,中文翻译可以不配，直接取key
            if (locale.equals(Locale.CHINA)) {
                return msgKey;
            }
            String message = messageSource.getMessage(msgKey, null, locale);
            return message;
        } catch (Exception e) {
            log.error("多语言获取失败，key:{}", msgKey);
            //e.printStackTrace();
            return msgKey;
        }
    }
}