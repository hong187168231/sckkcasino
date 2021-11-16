package com.qianyi.casinocore.util;

import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.PageVo;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

@Slf4j
public class CommonUtil {

    private static final String SET = "set";
    private static final String METHOD_FORMAT = "{0}{1}";
    private static final String FIRSTPROXY = "firstProxy";
    private static final String SECONDPROXY = "secondProxy";
    private static final String THIRDPROXY = "thirdProxy";

    public static PageResultVO<?> handlePageResult(List<?> list, PageVo pageVO) {
        // 分页组装
        PageResultVO<?> pageResult = new PageResultVO<>();
        if (list == null || list.size() == 0) {
            pageResult.setContent(Collections.emptyList());
            pageResult.setTotalPages(0);
            pageResult.setTotalElements(0L);
            return pageResult;
        }
        int startIndex = (pageVO.getPageNo() - 1) * pageVO.getPageSize(); // 开始取值
        int endIndex = pageVO.getPageSize() + startIndex; // 结束取值
        List<?> pageList = null;
        pageResult.setTotalElements(Long.parseLong(list.size()+"")); // 总条数
        pageResult.setTotalPages(list.size() % pageVO.getPageSize() == 0 ? list.size() / pageVO.getPageSize() : list.size() / pageVO.getPageSize() + 1); // 总共多少页
        pageResult.setSize(pageVO.getPageSize());
        pageResult.setNumber(pageVO.getPageNo());
        try {
            pageList = list.subList(startIndex, endIndex);
            pageResult.setContent(pageList); // 分页显示的记录
            return pageResult;
        } catch (Exception e) {
            if (pageVO.getPageNo() > 1) {
                try {
                    pageList = list.subList(startIndex, list.size());
                    pageResult.setContent(pageList); // 分页显示的记录
                    return pageResult;
                } catch (Exception e1) {
                    PageResultVO<?> pageResultnew = new PageResultVO<>();
                    pageResultnew.setContent(Collections.emptyList());
                    return pageResultnew;
                }
            } else {
                pageList = list.subList(0, list.size());
                pageResult.setContent(pageList); // 分页显示的记录
                return pageResult;
            }
        }
    }
    public static Boolean setParameter(Object object, ProxyUser proxyUser){
        if (proxyUser == null){
            return true;
        }
        if (proxyUser.getProxyRole() == CommonConst.NUMBER_1){
            return setMethod(object,FIRSTPROXY, proxyUser.getId());
        }else if(proxyUser.getProxyRole() == CommonConst.NUMBER_2){
            return setMethod(object,SECONDPROXY, proxyUser.getId());
        }else if(proxyUser.getProxyRole() == CommonConst.NUMBER_3){
            return setMethod(object,THIRDPROXY, proxyUser.getId());
        }else {
            return true;
        }
    }

    private static Boolean setMethod(Object object,String parameter,Long proxyRole){
        try {
            Method setMethod = object.getClass().getMethod(MessageFormat.format(METHOD_FORMAT, SET,
                    com.qianyi.modulecommon.util.CommonUtil.toUpperCaseFirstOne(parameter)), Long.class);
            setMethod.invoke(object, proxyRole);
        } catch (Exception e) {
            log.error("反射生成对象异常",e);
            return true;
        }
        return false;
    }
}
