package com.qianyi.modulecommon.util;


import com.qianyi.modulecommon.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
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
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String language = request.getHeader(Constants.LANGUAGE);
            //默认中文
            Locale locale = Locale.CHINA;
            if (Locale.US.toString().equals(language)) {
                locale = Locale.US;
            }
//            Locale locale = LocaleContextHolder.getLocale();
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